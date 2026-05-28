package cloud.playstorm.cloudfallcore.events.horror;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class PhantomFootstepsEvent implements ServerEvent {

    private BukkitRunnable activeTask;

    @Override
    public String getId() { return "horror.phantom_footsteps"; }

    @Override
    public String getDisplayName() { return "👣 Phantom Footsteps"; }

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
            int ticks = 0;
            final int duration = 600; // 30 seconds = 600 ticks

            @Override
            public void run() {
                if (!target.isOnline() || ticks >= duration) {
                    this.cancel();
                    return;
                }

                try {
                    Vector direction = target.getLocation().getDirection().normalize().multiply(-2);
                    Location behind = target.getLocation().add(direction);
                    target.playSound(behind, Sound.BLOCK_STONE_STEP, 0.6f, 0.8f);
                } catch (Exception ignored) {}

                ticks += 30;
            }
        };
        activeTask.runTaskTimer(plugin, 0L, 30L);
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
