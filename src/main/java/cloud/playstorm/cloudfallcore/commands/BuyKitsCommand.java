package cloud.playstorm.cloudfallcore.commands;

import cloud.playstorm.cloudfallcore.CloudFallCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BuyKitsCommand implements CommandExecutor {

    private final CloudFallCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BuyKitsCommand(CloudFallCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        boolean enabled = plugin.getConfig().getBoolean("commands.buykits.enabled", true);
        if (!enabled) {
            String disabledMsg = plugin.getConfig().getString("commands.buykits.disabled-message", "<red>This command is currently disabled.</red>");
            sender.sendMessage(miniMessage.deserialize(disabledMsg));
            return true;
        }

        List<String> messages = plugin.getConfig().getStringList("commands.buykits.messages");
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
