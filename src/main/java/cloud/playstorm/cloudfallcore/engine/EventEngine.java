package cloud.playstorm.cloudfallcore.engine;

import cloud.playstorm.cloudfallcore.CloudFallCore;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

public class EventEngine {

    private final CloudFallCore plugin;
    private final EventRegistry registry;
    private final EventLogger logger;
    private final AFKTracker afkTracker;
    private final EventCooldownManager cooldownManager;
    private BukkitTask engineTask;
    private final Random random = new Random();

    public EventEngine(CloudFallCore plugin) {
        this.plugin = plugin;
        this.logger = new EventLogger(plugin);
        this.afkTracker = new AFKTracker(plugin);
        this.registry = new EventRegistry(plugin, logger, afkTracker);
        this.cooldownManager = new EventCooldownManager();
    }

    public void start() {
        afkTracker.start();
        scheduleNextEvent();
    }

    public void stop() {
        afkTracker.stop();
        if (engineTask != null) {
            engineTask.cancel();
        }
    }

    private void scheduleNextEvent() {
        if (!plugin.getConfig().getBoolean("engine.enabled", true)) {
            return;
        }

        int minMins = plugin.getConfig().getInt("engine.min-interval-minutes", 3);
        int maxMins = plugin.getConfig().getInt("engine.max-interval-minutes", 8);
        if (maxMins < minMins) maxMins = minMins;
        
        int minTicks = minMins * 60 * 20;
        int maxTicks = maxMins * 60 * 20;
        int delayTicks = minTicks;
        if (maxTicks > minTicks) {
            delayTicks += random.nextInt(maxTicks - minTicks);
        }

        engineTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                runRandomEvent();
            } catch (Exception e) {
                logger.log("[ERROR] Exception in EventEngine execution: " + e.getMessage());
                e.printStackTrace();
            }
            scheduleNextEvent(); // Schedule the next one
        }, delayTicks);
    }

    private EventCategory pickCategoryByWeight() {
        Map<EventCategory, Integer> weights = new HashMap<>();
        weights.put(EventCategory.WEATHER, plugin.getConfig().getInt("engine.category-weights.weather", 18));
        weights.put(EventCategory.HORROR, plugin.getConfig().getInt("engine.category-weights.horror", 20));
        weights.put(EventCategory.REWARD, plugin.getConfig().getInt("engine.category-weights.reward", 30));
        weights.put(EventCategory.COMBAT, plugin.getConfig().getInt("engine.category-weights.combat", 7));
        weights.put(EventCategory.SOCIAL, plugin.getConfig().getInt("engine.category-weights.social", 15));
        weights.put(EventCategory.MYSTERY, plugin.getConfig().getInt("engine.category-weights.mystery", 10));

        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();
        if (totalWeight <= 0) return EventCategory.REWARD; // Fallback

        int rand = random.nextInt(totalWeight);
        int currentWeight = 0;
        for (Map.Entry<EventCategory, Integer> entry : weights.entrySet()) {
            currentWeight += entry.getValue();
            if (rand < currentWeight) {
                return entry.getKey();
            }
        }
        return EventCategory.REWARD;
    }

    private void runRandomEvent() {
        List<Player> onlinePlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SURVIVAL && !afkTracker.isAFK(p)) {
                onlinePlayers.add(p);
            }
        }

        if (onlinePlayers.isEmpty()) {
            logger.log("Skipped event execution: No active survival players online.");
            return;
        }

        EventCategory category = pickCategoryByWeight();
        
        // Try up to 5 times to find an event not on cooldown
        ServerEvent event = null;
        for (int i = 0; i < 5; i++) {
            event = registry.getRandomEventByCategory(category);
            if (event != null && !cooldownManager.isOnGlobalCooldown(event.getId())) {
                break;
            }
            event = null;
        }
        
        if (event == null) {
            logger.log("Skipped event execution: All events in category " + category.name() + " were on cooldown or none available.");
            return;
        }

        try {
            if (event.getType() == EventType.GLOBAL) {
                logger.log("[GLOBAL] Firing event: " + event.getId());
                event.execute();
                cooldownManager.setGlobalCooldown(event.getId(), event.getCooldownMinutes());
            } else if (event.getType() == EventType.PERSONAL) {
                // Find a player not on personal cooldown for this event
                Player target = null;
                List<Player> shuffled = new ArrayList<>(onlinePlayers);
                java.util.Collections.shuffle(shuffled);
                for (Player p : shuffled) {
                    if (!cooldownManager.isOnPersonalCooldown(p.getUniqueId(), event.getId())) {
                        target = p;
                        break;
                    }
                }
                
                if (target == null) {
                    logger.log("[PERSONAL] Skipped " + event.getId() + " - all active players on personal cooldown.");
                    return;
                }
                
                logger.log("[PERSONAL] Firing event: " + event.getId() + " | Target: " + target.getName());
                event.execute(target);
                cooldownManager.setPersonalCooldown(target.getUniqueId(), event.getId(), event.getCooldownMinutes());
                // Set a short global cooldown to prevent same event firing twice immediately for others
                cooldownManager.setGlobalCooldown(event.getId(), Math.max(1, event.getCooldownMinutes() / 3));
            }
        } catch (Exception e) {
            logger.log("[ERROR] Event " + event.getId() + " threw an exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
