package cloud.playstorm.cloudfallcore.tasks;

import cloud.playstorm.cloudfallcore.CloudFallCore;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class AutoBroadcaster {

    private final CloudFallCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private BukkitTask task;
    private int currentIndex = 0;

    public AutoBroadcaster(CloudFallCore plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("broadcaster.enabled")) {
            return;
        }

        int intervalMinutes = plugin.getConfig().getInt("broadcaster.interval", 5);
        long intervalTicks = intervalMinutes * 60L * 20L;

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            List<String> messages = plugin.getConfig().getStringList("broadcaster.messages");
            if (messages.isEmpty()) return;

            if (currentIndex >= messages.size()) {
                currentIndex = 0;
            }

            String msg = messages.get(currentIndex);
            Bukkit.getServer().sendMessage(miniMessage.deserialize(msg));

            currentIndex++;
        }, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }
}
