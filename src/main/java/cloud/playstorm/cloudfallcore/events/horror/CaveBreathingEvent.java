package cloud.playstorm.cloudfallcore.events.horror;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CaveBreathingEvent implements ServerEvent {

    private BukkitRunnable activeTask;

    @Override
    public String getId() { return "horror.cave_breathing"; }

    @Override
    public String getDisplayName() { return "🫁 Cave Breathing"; }

    @Override
    public EventCategory getCategory() { return EventCategory.HORROR; }

    @Override
    public EventType getType() { return EventType.PERSONAL; }

    @Override
    public int getCooldownMinutes() { return 10; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) {
        if (target.getLocation().getY() >= 50) return;

        Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
        if (plugin == null) return;

        activeTask = new BukkitRunnable() {
            int ticks = 0;
            final int duration = 400; // 20 seconds = 400 ticks

            @Override
            public void run() {
                if (!target.isOnline() || ticks >= duration) {
                    this.cancel();
                    return;
                }

                try {
                    target.playSound(target.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 0.4f, 0.8f);
                } catch (Exception e) {
                    // Fallback if ENTITY_WARDEN_HEARTBEAT is not available
                    try {
                        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_BREATH, 0.4f, 0.7f);
                    } catch (Exception ignored) {}
                }

                ticks += 40;
            }
        };
        activeTask.runTaskTimer(plugin, 0L, 40L);
    }

    @Override
    public void execute() { }

    @Override
    public void cleanup() {
        if (activeTask != null && !activeTask.isCancelled()) {
            try {
                activeTask.cancel();
            } catch (Exception ignored) {}
        }
    }
}
