package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumSet;
import java.util.Set;

/**
 * For a few minutes everyone gets Haste and ores drop bonus loot. Rewards the
 * thing players already love doing, and pulls them into the caves together.
 */
public class MiningFrenzyEvent extends BaseEvent implements Listener {

    private boolean active = false;

    private static final Set<Material> ORES = EnumSet.of(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE,
            Material.ANCIENT_DEBRIS
    );

    @Override public String getId() { return "reward.mining_frenzy"; }
    @Override public String getDisplayName() { return "⛏️ Mining Frenzy"; }
    @Override public EventCategory getCategory() { return EventCategory.REWARD; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 35; }
    @Override public int getDurationSeconds() { return 300; }
    @Override public int getWeight() { return 10; }

    @Override
    public void execute() {
        if (active) return;
        active = true;

        Bukkit.getPluginManager().registerEvents(this, plugin());
        for (Player p : Bukkit.getOnlinePlayers()) applyHaste(p);

        int mins = Math.max(1, durationSeconds() / 60);
        MessageUtil.broadcastTitle(
                "<gradient:#ffd700:#ff8c00>⛏️ Mining Frenzy!</gradient>",
                "<gray>Haste + double ore drops for " + mins + " min!</gray>",
                15, 70, 20);
        MessageUtil.broadcast("<gold>⛏️ <b>Mining Frenzy</b> has begun! Get to the caves - ores drop extra for "
                + mins + " minutes!</gold>");
    }

    private void applyHaste(Player p) {
        int level = Math.max(1, settingInt("haste-level", 2));
        p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE,
                durationSeconds() * 20, level - 1, false, false, true));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (active) applyHaste(event.getPlayer());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!active) return;
        Material type = event.getBlock().getType();
        if (!ORES.contains(type)) return;

        int extra = Math.max(1, settingInt("bonus-multiplier", 2) - 1);
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        for (int i = 0; i < extra; i++) {
            for (ItemStack drop : event.getBlock().getDrops(tool)) {
                event.getBlock().getWorld().dropItemNaturally(
                        event.getBlock().getLocation().add(0.5, 0.5, 0.5), drop.clone());
            }
        }
    }

    @Override
    public void cleanup() {
        if (!active) return;
        active = false;
        HandlerList.unregisterAll(this);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.removePotionEffect(PotionEffectType.HASTE);
        }
        MessageUtil.broadcast("<gold>⛏️ Mining Frenzy has ended. Nice haul!</gold>");
    }
}
