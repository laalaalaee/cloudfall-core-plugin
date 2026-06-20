package cloud.playstorm.cloudfallcore.events.mystery;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Glowing relics scatter across the area. Track them down and pick them up - when
 * the last relic is found, everyone who helped is rewarded for their share.
 */
public class RelicHuntEvent extends BaseEvent implements Listener {

    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Random random = new Random();

    private boolean active = false;
    private int total;
    private final Set<UUID> relicIds = new HashSet<>();
    private final Map<UUID, Integer> collectors = new HashMap<>();

    @Override public String getId() { return "mystery.relic_hunt"; }
    @Override public String getDisplayName() { return "🔮 Relic Hunt"; }
    @Override public EventCategory getCategory() { return EventCategory.MYSTERY; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 35; }
    @Override public int getDurationSeconds() { return 240; }
    @Override public int getWeight() { return 8; }

    @Override
    public void execute() {
        if (active) return;
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (online.isEmpty()) return;

        Player anchor = online.get(random.nextInt(online.size()));
        World world = anchor.getWorld();
        total = Math.max(1, settingInt("relic-count", 6));
        int spread = settingInt("spread", 40);
        relicIds.clear();
        collectors.clear();
        active = true;

        for (int i = 0; i < total; i++) {
            int x = anchor.getLocation().getBlockX() + random.nextInt(spread * 2) - spread;
            int z = anchor.getLocation().getBlockZ() + random.nextInt(spread * 2) - spread;
            Location loc = world.getHighestBlockAt(x, z).getLocation().add(0.5, 1.2, 0.5);

            ItemStack relic = new ItemStack(Material.NETHER_STAR);
            ItemMeta meta = relic.getItemMeta();
            meta.displayName(mm.deserialize("<gradient:#a000ff:#00e5ff>🔮 Ancient Relic</gradient>"));
            relic.setItemMeta(meta);

            Item dropped = world.dropItem(loc, relic);
            dropped.setGlowing(true);
            dropped.setCustomNameVisible(true);
            dropped.customName(mm.deserialize("<light_purple>🔮 Relic</light_purple>"));
            dropped.setVelocity(dropped.getVelocity().zero());
            relicIds.add(dropped.getUniqueId());
        }

        Bukkit.getPluginManager().registerEvents(this, plugin());
        MessageUtil.broadcastTitle("<light_purple>🔮 Relic Hunt!</light_purple>",
                "<gray>" + total + " relics are hidden nearby!</gray>", 15, 80, 20);
        MessageUtil.broadcast("<light_purple>🔮 <b>Relic Hunt!</b> <yellow>" + total
                + "</yellow> glowing relics scattered near <yellow>" + anchor.getName()
                + "</yellow>. Find them all - finders get rewarded!</light_purple>");
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!active) return;
        Item item = event.getItem();
        if (!relicIds.remove(item.getUniqueId())) return;
        if (!(event.getEntity() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }

        collectors.merge(player.getUniqueId(), 1, Integer::sum);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);
        int found = total - relicIds.size();
        MessageUtil.broadcast("<light_purple>🔮 <yellow>" + player.getName() + "</yellow> found a relic! ("
                + found + "/" + total + ")</light_purple>");

        if (relicIds.isEmpty()) complete();
    }

    private void complete() {
        active = false;
        double perRelic = settingDouble("currency-per-relic", 400);
        for (Map.Entry<UUID, Integer> entry : collectors.entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p == null || !p.isOnline()) continue;
            payout(p, perRelic * entry.getValue());
            p.getInventory().addItem(new ItemStack(Material.DIAMOND, entry.getValue()));
        }
        MessageUtil.broadcast("<green>🔮 All relics recovered! The finders have been rewarded.</green>");
        HandlerList.unregisterAll(this);
    }

    @Override
    public void cleanup() {
        if (!active && relicIds.isEmpty()) { HandlerList.unregisterAll(this); return; }
        active = false;
        HandlerList.unregisterAll(this);
        // Remove any relics that were never found.
        if (!relicIds.isEmpty()) {
            for (World world : Bukkit.getWorlds()) {
                for (Item item : world.getEntitiesByClass(Item.class)) {
                    if (relicIds.contains(item.getUniqueId())) item.remove();
                }
            }
            MessageUtil.broadcast("<gray>🔮 The remaining relics faded back into legend...</gray>");
            relicIds.clear();
        }
    }
}
