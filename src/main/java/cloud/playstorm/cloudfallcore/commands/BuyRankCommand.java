package cloud.playstorm.cloudfallcore.commands;

import cloud.playstorm.cloudfallcore.CloudFallCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BuyRankCommand implements CommandExecutor {

    private final CloudFallCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BuyRankCommand(CloudFallCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> messages = plugin.getConfig().getStringList("messages.buyrank");
        for (String msg : messages) {
            if (msg.isEmpty()) {
                sender.sendMessage(Component.empty());
            } else {
                sender.sendMessage(miniMessage.deserialize(msg));
            }
        }
        return true;
    }
}
