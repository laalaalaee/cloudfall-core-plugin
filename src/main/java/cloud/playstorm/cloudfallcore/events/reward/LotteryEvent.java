package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Every online player is automatically entered into a lottery and one lucky
 * winner takes the jackpot. Zero effort to join - pure suspense.
 */
public class LotteryEvent extends BaseEvent {

    private final Random random = new Random();

    @Override public String getId() { return "reward.lottery"; }
    @Override public String getDisplayName() { return "🎰 Server Lottery"; }
    @Override public EventCategory getCategory() { return EventCategory.REWARD; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 40; }
    @Override public int getWeight() { return 8; }

    @Override
    public void execute() {
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (online.isEmpty()) return;

        MessageUtil.broadcast("<gold>🎰 <b>Server Lottery!</b> Drawing a winner...</gold>");
        for (Player p : online) p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

        Player winner = online.get(random.nextInt(online.size()));
        double jackpot = settingDouble("jackpot", 5000);
        int diamonds = settingInt("jackpot-diamonds", 10);

        payout(winner, jackpot);
        if (diamonds > 0) winner.getInventory().addItem(new ItemStack(Material.DIAMOND, diamonds));
        winner.playSound(winner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

        MessageUtil.broadcastTitle("<gradient:#f7971e:#ffd200>🎰 " + winner.getName() + " won!</gradient>",
                "<gray>The lottery jackpot is theirs!</gray>", 15, 80, 20);
        MessageUtil.broadcast("<gold>🎰 <yellow>" + winner.getName()
                + "</yellow> won the lottery jackpot! Better luck next time, everyone else.</gold>");
    }
}
