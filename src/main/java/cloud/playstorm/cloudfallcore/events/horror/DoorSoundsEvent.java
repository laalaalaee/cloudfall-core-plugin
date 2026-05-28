package cloud.playstorm.cloudfallcore.events.horror;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DoorSoundsEvent implements ServerEvent {

    private BukkitRunnable activeTask;

    @Override
    public String getId() { return "horror.door_sounds"; }

    @Override
    public String getDisplayName() { return "🚪 Door Sounds"; }

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
            int tick = 0;
            // Each cycle: open at 0, close at 40, next cycle at 80
            // 3 cycles = ticks 0, 40, 80, 120, 160, 200
            final int totalTicks = 240; // 3 full cycles

            @Override
            public void run() {
                if (!target.isOnline() || tick >= totalTicks) {
                    this.cancel();
                    return;
                }

                try {
                    int cyclePosition = tick % 80;
                    if (cyclePosition == 0) {
                        target.playSound(target.getLocation(), Sound.BLOCK_WOODEN_DOOR_OPEN, 0.7f, 0.8f);
                    } else if (cyclePosition == 40) {
                        target.playSound(target.getLocation(), Sound.BLOCK_WOODEN_DOOR_CLOSE, 0.7f, 0.8f);
                    }
                } catch (Exception ignored) {}

                tick++;
            }
        };
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
