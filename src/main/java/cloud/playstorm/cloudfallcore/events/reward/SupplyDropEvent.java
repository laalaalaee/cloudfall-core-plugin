package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.CloudFallCore;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SupplyDropEvent implements ServerEvent {

    private final Random random = new Random();
    private Location chestLocation;

    @Override
    public String getId() { return "reward.supply_drop"; }

    @Override
    public String getDisplayName() { return "📦 Supply Drop"; }

    @Override
    public EventCategory getCategory() { return EventCategory.REWARD; }

    @Override
    public EventType getType() { return EventType.GLOBAL; }

    @Override
    public int getCooldownMinutes() { return 15; }

    @Override
    public int getDurationSeconds() { return 600; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) {
        // GLOBAL event — delegate to execute()
        execute();
    }

    @Override
    public void execute() {
        try {
            List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
            if (online.isEmpty()) return;

            Player chosen = online.get(random.nextInt(online.size()));
            World world = chosen.getWorld();
            Location base = chosen.getLocation();

            // Spread the drop out a fair, configurable distance in a random
            // direction so it never lands on top of a player's base.
            int min = CloudFallCore.getInstance().getConfig()
                    .getInt("events.reward.supply_drop.settings.min-distance", 120);
            int max = CloudFallCore.getInstance().getConfig()
                    .getInt("events.reward.supply_drop.settings.max-distance", 300);
            if (max <= min) max = min + 50;
            int distance = min + random.nextInt(max - min);
            double angle = random.nextDouble() * Math.PI * 2;

            int dropX = base.getBlockX() + (int) (Math.cos(angle) * distance);
            int dropZ = base.getBlockZ() + (int) (Math.sin(angle) * distance);

            // Load the drop chunk OFF the main thread so distant terrain
            // generation never freezes the server (ping spike). The callback
            // runs back on the main thread.
            world.getChunkAtAsync(dropX >> 4, dropZ >> 4).thenAccept(chunk -> {
                try {
                    int dropY = world.getHighestBlockYAt(dropX, dropZ) + 1;

                    // Place the chest
                    Block block = world.getBlockAt(dropX, dropY, dropZ);
                    block.setType(Material.CHEST);
                    chestLocation = block.getLocation();

                    // Fill the chest with loot
                    if (block.getState() instanceof Chest chest) {
                        Inventory inv = chest.getBlockInventory();

                        inv.addItem(new ItemStack(Material.DIAMOND, 2 + random.nextInt(3)));
                        inv.addItem(new ItemStack(Material.EMERALD, 1 + random.nextInt(3)));
                        inv.addItem(new ItemStack(Material.IRON_INGOT, 16 + random.nextInt(17)));
                        inv.addItem(new ItemStack(Material.GOLD_INGOT, 8 + random.nextInt(9)));
                        inv.addItem(new ItemStack(Material.GOLDEN_APPLE, 4));

                        // Enchanted book with a random enchantment
                        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
                        Enchantment[] enchants = Enchantment.values();
                        Enchantment chosen_enchant = enchants[random.nextInt(enchants.length)];
                        int level = 1 + random.nextInt(chosen_enchant.getMaxLevel());
                        meta.addStoredEnchant(chosen_enchant, level, true);
                        book.setItemMeta(meta);
                        inv.addItem(book);

                        chest.update();
                    }

                    // Broadcast
                    MessageUtil.broadcast(String.format(
                            "<color:#ffaa00>📦 SUPPLY DROP at X: %d, Z: %d! First to reach it wins!</color>",
                            dropX, dropZ
                    ));
                    MessageUtil.broadcastTitle(
                            "<color:#ffaa00>📦 Supply Drop!</color>",
                            "<gray>Check chat for coordinates!</gray>",
                            10, 60, 20
                    );

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() {
        if (chestLocation != null) {
            try {
                Block block = chestLocation.getBlock();
                if (block.getType() == Material.CHEST) {
                    block.setType(Material.AIR);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            chestLocation = null;
        }
    }
}
