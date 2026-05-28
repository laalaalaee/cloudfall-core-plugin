package cloud.playstorm.cloudfallcore.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;

public class MessageUtil {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static void broadcast(String message) {
        Bukkit.getServer().sendMessage(miniMessage.deserialize(message));
    }

    public static void send(Player player, String message) {
        player.sendMessage(miniMessage.deserialize(message));
    }

    public static void sendActionBar(Player player, String message) {
        player.sendActionBar(miniMessage.deserialize(message));
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        Title.Times times = Title.Times.times(Duration.ofMillis(fadeInTicks * 50L), Duration.ofMillis(stayTicks * 50L), Duration.ofMillis(fadeOutTicks * 50L));
        Title t = Title.title(miniMessage.deserialize(title), miniMessage.deserialize(subtitle), times);
        player.showTitle(t);
    }

    public static void broadcastTitle(String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        Title.Times times = Title.Times.times(Duration.ofMillis(fadeInTicks * 50L), Duration.ofMillis(stayTicks * 50L), Duration.ofMillis(fadeOutTicks * 50L));
        Title t = Title.title(miniMessage.deserialize(title), miniMessage.deserialize(subtitle), times);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(t);
        }
    }
}
