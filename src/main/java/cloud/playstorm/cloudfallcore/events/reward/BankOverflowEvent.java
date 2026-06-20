package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * The server treasury "overflows" and pays every online player a chunk of
 * currency. Pure feel-good moment; works on a server of any size.
 * Requires Vault economy to actually pay out.
 */
public class BankOverflowEvent extends BaseEvent {

    @Override public String getId() { return "reward.bank_overflow"; }
    @Override public String getDisplayName() { return "💰 Bank Overflow"; }
    @Override public EventCategory getCategory() { return EventCategory.REWARD; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 40; }
    @Override public int getWeight() { return 8; }

    @Override
    public void execute() {
        double amount = settingDouble("amount", 2000);

        if (!plugin().getEconomy().isEnabled()) {
            plugin().getLogger().info("Skipped Bank Overflow: no economy is hooked.");
            return;
        }

        MessageUtil.broadcastTitle(
                "<gradient:#f9d423:#ffd700>💰 Bank Overflow!</gradient>",
                "<gray>The treasury spills into everyone's pockets!</gray>",
                15, 70, 20);

        for (Player p : Bukkit.getOnlinePlayers()) {
            payout(p, amount);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        }
        MessageUtil.broadcast("<gradient:#f9d423:#ffd700>💰 Bank Overflow!</gradient> <gray>Everyone received</gray> <yellow>"
                + plugin().getEconomy().format(amount) + "</yellow><gray>!</gray>");
    }
}
