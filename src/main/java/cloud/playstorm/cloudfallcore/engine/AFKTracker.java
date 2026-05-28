package cloud.playstorm.cloudfallcore.engine;

import cloud.playstorm.cloudfallcore.CloudFallCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AFKTracker {

    private final CloudFallCore plugin;
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Map<UUID, Long> lastMoveTimes = new HashMap<>();
    private BukkitTask task;
    private static final long AFK_TIME_MS = 5 * 60 * 1000; // 5 minutes

    public AFKTracker(CloudFallCore plugin) {
        this.plugin = plugin;
    }

    public void start() {
        // Check every 10 seconds
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                Location current = player.getLocation();
                Location last = lastLocations.get(uuid);

                if (last == null || !last.getWorld().equals(current.getWorld()) || last.distanceSquared(current) > 0.1) {
                    lastLocations.put(uuid, current);
                    lastMoveTimes.put(uuid, now);
                }
            }
        }, 200L, 200L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }

    public boolean isAFK(Player player) {
        Long lastMove = lastMoveTimes.get(player.getUniqueId());
        if (lastMove == null) return false;
        return (System.currentTimeMillis() - lastMove) > AFK_TIME_MS;
    }
}
