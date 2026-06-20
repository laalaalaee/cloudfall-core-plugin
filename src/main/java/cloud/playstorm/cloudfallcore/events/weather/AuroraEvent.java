package cloud.playstorm.cloudfallcore.events.weather;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

/**
 * Shimmering lights paint the sky and bless everyone with Night Vision and
 * gentle regeneration. A calm, beautiful event - the opposite of the horror set.
 */
public class AuroraEvent extends BaseEvent implements Listener {

    private final Random random = new Random();
    private boolean active = false;
    private BukkitTask particleTask;

    private static final Color[] COLORS = {
            Color.fromRGB(0, 255, 140), Color.fromRGB(0, 200, 255),
            Color.fromRGB(160, 0, 255), Color.fromRGB(255, 0, 180)
    };

    @Override public String getId() { return "weather.aurora"; }
    @Override public String getDisplayName() { return "🌌 Aurora"; }
    @Override public EventCategory getCategory() { return EventCategory.WEATHER; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 30; }
    @Override public int getDurationSeconds() { return 180; }
    @Override public int getWeight() { return 10; }

    @Override
    public void execute() {
        if (active) return;
        active = true;
        Bukkit.getPluginManager().registerEvents(this, plugin());
        for (Player p : Bukkit.getOnlinePlayers()) bless(p);

        MessageUtil.broadcastTitle("<gradient:#00ff8c:#a000ff>🌌 Aurora</gradient>",
                "<gray>The sky comes alive with light...</gray>", 20, 80, 20);
        MessageUtil.broadcast("<aqua>🌌 An <b>Aurora</b> shimmers overhead - bask in its glow.</aqua>");

        particleTask = Bukkit.getScheduler().runTaskTimer(plugin(), this::drawAurora, 0L, 10L);
    }

    private void bless(Player p) {
        int ticks = durationSeconds() * 20;
        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, ticks, 0, false, false, true));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, ticks, 0, false, false, true));
    }

    private void drawAurora() {
        if (!active) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            Location base = p.getLocation().add(
                    random.nextInt(21) - 10, 12 + random.nextInt(6), random.nextInt(21) - 10);
            Color color = COLORS[random.nextInt(COLORS.length)];
            p.getWorld().spawnParticle(Particle.DUST, base, 12, 3, 1, 3,
                    new Particle.DustOptions(color, 2.0f));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (active) bless(event.getPlayer());
    }

    @Override
    public void cleanup() {
        if (!active) return;
        active = false;
        if (particleTask != null) { particleTask.cancel(); particleTask = null; }
        HandlerList.unregisterAll(this);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
        MessageUtil.broadcast("<aqua>🌌 The aurora fades from the sky.</aqua>");
    }
}
