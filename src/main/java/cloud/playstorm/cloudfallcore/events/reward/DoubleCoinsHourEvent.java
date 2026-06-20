package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * For a while, every mob you kill pays out currency. Turns ordinary grinding
 * into a gold rush and keeps players active for the whole window.
 */
public class DoubleCoinsHourEvent extends BaseEvent implements Listener {

    private boolean active = false;

    @Override public String getId() { return "reward.double_coins_hour"; }
    @Override public String getDisplayName() { return "🪙 Double Coins"; }
    @Override public EventCategory getCategory() { return EventCategory.REWARD; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 45; }
    @Override public int getDurationSeconds() { return 300; }
    @Override public int getWeight() { return 9; }

    @Override
    public void execute() {
        if (active) return;
        active = true;
        Bukkit.getPluginManager().registerEvents(this, plugin());

        int mins = Math.max(1, durationSeconds() / 60);
        MessageUtil.broadcastTitle("<gradient:#f9d423:#ffd700>🪙 Coin Frenzy!</gradient>",
                "<gray>Every mob kill pays out for " + mins + " min!</gray>", 15, 70, 20);
        MessageUtil.broadcast("<gold>🪙 <b>Coin Frenzy!</b> Slay mobs to earn currency for the next "
                + mins + " minutes!</gold>");
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!active) return;
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        if (event.getEntity() instanceof Player) return;
        payout(killer, settingDouble("per-kill", 25));
    }

    @Override
    public void cleanup() {
        if (!active) return;
        active = false;
        HandlerList.unregisterAll(this);
        MessageUtil.broadcast("<gold>🪙 Coin Frenzy has ended.</gold>");
    }
}
