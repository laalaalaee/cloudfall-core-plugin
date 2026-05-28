package cloud.playstorm.cloudfallcore.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class BuyRankCommand implements CommandExecutor {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Ranks based on user request
    private final List<String> rankMessages = Arrays.asList(
            "",
            "<white>☁️</white> <color:#aaaaaa><bold>SMP Member</bold></color> <gray>| 30 days</gray> <color:#55ff55>₹99</color>",
            "<gray>- SMP Member tag (in-game + Discord)</gray>",
            "<gray>- Exclusive rank join screen</gray>",
            "<gray>- Render distance: 8</gray>",
            "<gray>- Chat Cooldown: 10s</gray>",
            "<click:open_url:'https://playstorm.cloud/minecraft'><hover:show_text:'Click to buy SMP Member'><color:#55ff55>[ Get SMP Member ]</color></hover></click>",
            "",
            "<color:#ffaa00>🧡</color> <color:#ffaa00><bold>Supporter</bold></color> <gray>| 30 days</gray> <color:#55ff55>₹179</color>",
            "<gray>- Supporter tag (in-game + Discord)</gray>",
            "<gray>- Exclusive rank join screen</gray>",
            "<gray>- 1 home using /home</gray>",
            "<gray>- Render distance: 10</gray>",
            "<gray>- Chat Cooldown: 5s</gray>",
            "<gray>- Colored chat messages</gray>",
            "<click:open_url:'https://playstorm.cloud/minecraft'><hover:show_text:'Click to buy Supporter'><color:#ffaa00>[ Get Supporter ]</color></hover></click>",
            "",
            "<color:#ff55ff>💜</color> <color:#ff55ff><bold>CloudPass Elite</bold></color> <color:#ffff55>(Most Popular)</color> <gray>| 30 days</gray> <color:#55ff55>₹349</color>",
            "<gray>- CloudPass Elite tag (in-game + Discord)</gray>",
            "<gray>- Exclusive rank join screen</gray>",
            "<gray>- Custom entry sound</gray>",
            "<gray>- /nick custom nickname</gray>",
            "<gray>- 3 homes using /home (switch freely)</gray>",
            "<gray>- Render distance: 14</gray>",
            "<gray>- No chat cooldown</gray>",
            "<gray>- Colored chat messages</gray>",
            "<click:open_url:'https://playstorm.cloud/minecraft'><hover:show_text:'Click to buy CloudPass Elite'><color:#ff55ff>[ Get CloudPass Elite ]</color></hover></click>",
            "",
            "<color:#5555ff>💙</color> <color:#5555ff><bold>VIP Supporter</bold></color> <gray>| 45 days</gray> <color:#55ff55>₹699</color>",
            "<gray>- VIP Supporter tag (in-game + Discord)</gray>",
            "<gray>- Exclusive rank join screen</gray>",
            "<gray>- Custom entry sound</gray>",
            "<gray>- /nick custom nickname</gray>",
            "<gray>- 5 homes using /home (switch freely)</gray>",
            "<gray>- Render distance: 18</gray>",
            "<gray>- No chat cooldown</gray>",
            "<gray>- Special chat style</gray>",
            "<gray>- Colored chat messages</gray>",
            "<gray>- Update-vote access</gray>",
            "<click:open_url:'https://playstorm.cloud/minecraft'><hover:show_text:'Click to buy VIP Supporter'><color:#5555ff>[ Get VIP Supporter ]</color></hover></click>",
            "",
            "<color:#ff5555>❤️</color> <color:#ff5555><bold>Tip & Support</bold></color> <gray>| Lifetime Karma</gray> <color:#55ff55>₹49</color>",
            "<gray>- Support the server developers</gray>",
            "<gray>- Permanent ❤️ emoji on Tab</gray>",
            "<gray>- Our eternal gratitude</gray>",
            "<click:open_url:'https://playstorm.cloud/minecraft'><hover:show_text:'Click to Tip'><color:#ff5555>[ Support Us ]</color></hover></click>",
            ""
    );

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        for (String msg : rankMessages) {
            if (msg.isEmpty()) {
                sender.sendMessage(Component.empty());
            } else {
                sender.sendMessage(miniMessage.deserialize(msg));
            }
        }
        return true;
    }
}
