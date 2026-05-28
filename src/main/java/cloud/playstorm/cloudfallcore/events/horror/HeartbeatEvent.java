package cloud.playstorm.cloudfallcore.events.horror;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class HeartbeatEvent implements ServerEvent {

    private BukkitRunnable activeTask;

    @Override
    public String getId() { return "horror.heartbeat"; }

    @Override
    public String getDisplayName() { return "💓 Heartbeat"; }

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
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
        if (plugin == null) return;

        activeTask = new BukkitRunnable() {
            int ticksElapsed = 0;
            final int duration = 400; // 20 seconds = 400 ticks
            int ticksSinceLastBeat = 0;

            @Override
            public void run() {
                if (!target.isOnline() || ticksElapsed >= duration) {
                    this.cancel();
                    return;
                }

                try {
                    int lightLevel = target.getLocation().getBlock().getLightLevel();
                    int interval = (lightLevel < 7) ? 10 : 20;

                    if (ticksSinceLastBeat >= interval) {
                        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.7f, 0.6f);
                        ticksSinceLastBeat = 0;
                    }
                } catch (Exception ignored) {}

                ticksElapsed++;
                ticksSinceLastBeat++;
            }
        };
        // Run every tick to allow dynamic interval changes
        activeTask.runTaskTimer(plugin, 0L, 1L);
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
