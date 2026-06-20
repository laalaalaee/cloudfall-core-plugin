package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * For a while every catch comes with bonus treasure. Sends players running for
 * the nearest lake or coast.
 */
public class FishingFrenzyEvent extends BaseEvent implements Listener {

    private final Random random = new Random();
    private boolean active = false;

    private static final Material[] TREASURE = {
            Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT,
            Material.NAUTILUS_SHELL, Material.HEART_OF_THE_SEA, Material.EXPERIENCE_BOTTLE
    };

    @Override public String getId() { return "reward.fishing_frenzy"; }
    @Override public String getDisplayName() { return "🎣 Fishing Frenzy"; }
    @Override public EventCategory getCategory() { return EventCategory.REWARD; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 35; }
    @Override public int getDurationSeconds() { return 240; }
    @Override public int getWeight() { return 9; }

    @Override
    public void execute() {
        if (active) return;
        active = true;
        Bukkit.getPluginManager().registerEvents(this, plugin());

        int mins = Math.max(1, durationSeconds() / 60);
        MessageUtil.broadcastTitle("<aqua>🎣 Fishing Frenzy!</aqua>",
                "<gray>Bonus treasure on every catch for " + mins + " min!</gray>", 15, 70, 20);
        MessageUtil.broadcast("<aqua>🎣 <b>Fishing Frenzy!</b> Grab a rod - every catch has bonus loot!</aqua>");
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (!active) return;
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        Player p = event.getPlayer();
        if (random.nextInt(100) >= settingInt("treasure-chance", 60)) return;

        Material loot = TREASURE[random.nextInt(TREASURE.length)];
        p.getInventory().addItem(new ItemStack(loot, 1));
        payout(p, settingDouble("per-catch-currency", 0));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.5f);
        MessageUtil.send(p, "<aqua>🎣 Lucky catch! <white>+1 " + pretty(loot) + "</white></aqua>");
    }

    private String pretty(Material m) {
        String s = m.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @Override
    public void cleanup() {
        if (!active) return;
        active = false;
        HandlerList.unregisterAll(this);
        MessageUtil.broadcast("<aqua>🎣 The Fishing Frenzy has ended.</aqua>");
    }
}
