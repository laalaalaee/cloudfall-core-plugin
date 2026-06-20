package cloud.playstorm.cloudfallcore.engine;

import cloud.playstorm.cloudfallcore.CloudFallCore;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EventEngine {

    private final CloudFallCore plugin;
    private final EventRegistry registry;
    private final EventLogger logger;
    private final AFKTracker afkTracker;
    private final EventCooldownManager cooldownManager;
    private BukkitTask engineTask;
    private final Random random = new Random();

    // Tracks events that are currently "running" so we can clean them up on a
    // timer, on reload, or on shutdown.
    private final Map<String, BukkitTask> cleanupTasks = new HashMap<>();
    private final List<ServerEvent> activeEvents = new ArrayList<>();

    public EventEngine(CloudFallCore plugin) {
        this.plugin = plugin;
        this.logger = new EventLogger(plugin);
        this.afkTracker = new AFKTracker(plugin);
        this.registry = new EventRegistry(plugin, logger, afkTracker);
        this.cooldownManager = new EventCooldownManager();
    }

    public EventRegistry getRegistry() {
        return registry;
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
        // Force-clean anything still active so the world isn't left altered.
        cleanupAll();
    }

    /** Cleans up every active event immediately (used on reload/shutdown). */
    public void cleanupAll() {
        for (BukkitTask task : new ArrayList<>(cleanupTasks.values())) {
            task.cancel();
        }
        cleanupTasks.clear();
        for (ServerEvent event : new ArrayList<>(activeEvents)) {
            safeCleanup(event);
        }
        activeEvents.clear();
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
        List<Player> onlinePlayers = activeSurvivalPlayers();
        if (onlinePlayers.isEmpty()) {
            logger.log("Skipped event execution: No active survival players online.");
            return;
        }

        EventCategory category = pickCategoryByWeight();

        // Try up to 5 times to find an event not on cooldown.
        ServerEvent event = null;
        for (int i = 0; i < 5; i++) {
            ServerEvent candidate = registry.getWeightedEventByCategory(category);
            if (candidate != null && !cooldownManager.isOnGlobalCooldown(candidate.getId())) {
                event = candidate;
                break;
            }
        }

        if (event == null) {
            logger.log("Skipped event execution: All events in category " + category.name() + " were on cooldown or none available.");
            return;
        }

        fire(event, onlinePlayers, false);
    }

    /**
     * Manually fire an event by id (used by the admin command). Bypasses cooldown.
     * @return true if the event was found and fired.
     */
    public boolean trigger(String id) {
        ServerEvent event = registry.getById(id);
        if (event == null) return false;
        List<Player> players = activeSurvivalPlayers();
        if (players.isEmpty()) players = new ArrayList<>(Bukkit.getOnlinePlayers());
        logger.log("[MANUAL] Triggering event: " + id);
        fire(event, players, true);
        return true;
    }

    /** Shared firing logic for both automatic and manual triggers. */
    private void fire(ServerEvent event, List<Player> onlinePlayers, boolean manual) {
        int cooldown = cooldownFor(event);
        int duration = durationFor(event);

        try {
            if (event.getType() == EventType.GLOBAL) {
                logger.log("[GLOBAL] Firing event: " + event.getId());
                event.execute();
                if (!manual) cooldownManager.setGlobalCooldown(event.getId(), cooldown);
            } else {
                // PERSONAL / TARGETED - pick a player not on personal cooldown.
                Player target = null;
                List<Player> shuffled = new ArrayList<>(onlinePlayers);
                Collections.shuffle(shuffled);
                for (Player p : shuffled) {
                    if (manual || !cooldownManager.isOnPersonalCooldown(p.getUniqueId(), event.getId())) {
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
                if (!manual) {
                    cooldownManager.setPersonalCooldown(target.getUniqueId(), event.getId(), cooldown);
                    cooldownManager.setGlobalCooldown(event.getId(), Math.max(1, cooldown / 3));
                }
            }

            // Schedule automatic cleanup for timed events.
            if (duration > 0) {
                scheduleCleanup(event, duration);
            }
        } catch (Exception e) {
            logger.log("[ERROR] Event " + event.getId() + " threw an exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void scheduleCleanup(ServerEvent event, int durationSeconds) {
        // If this event is somehow already active, clean the old instance first.
        BukkitTask existing = cleanupTasks.remove(event.getId());
        if (existing != null) existing.cancel();

        if (!activeEvents.contains(event)) activeEvents.add(event);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            safeCleanup(event);
            activeEvents.remove(event);
            cleanupTasks.remove(event.getId());
        }, durationSeconds * 20L);

        cleanupTasks.put(event.getId(), task);
    }

    private void safeCleanup(ServerEvent event) {
        try {
            event.cleanup();
            logger.log("[CLEANUP] " + event.getId());
        } catch (Exception e) {
            logger.log("[ERROR] Cleanup for " + event.getId() + " failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int cooldownFor(ServerEvent event) {
        return plugin.getConfig().getInt("events." + event.getId() + ".cooldown-minutes", event.getCooldownMinutes());
    }

    private int durationFor(ServerEvent event) {
        return plugin.getConfig().getInt("events." + event.getId() + ".duration-seconds", event.getDurationSeconds());
    }

    private List<Player> activeSurvivalPlayers() {
        List<Player> players = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SURVIVAL && !afkTracker.isAFK(p)) {
                players.add(p);
            }
        }
        return players;
    }
}
