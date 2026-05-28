package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class TreasureMapEvent implements ServerEvent {

    private final Random random = new Random();
    private org.bukkit.Location chestLocation;

    @Override
    public String getId() { return "reward.treasure_map"; }

    @Override
    public String getDisplayName() { return "🗺️ Treasure Map"; }

    @Override
    public EventCategory getCategory() { return EventCategory.REWARD; }

    @Override
    public EventType getType() { return EventType.PERSONAL; }

    @Override
    public int getCooldownMinutes() { return 15; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) {
        try {
            if (target == null) return;

            World world = target.getWorld();
            org.bukkit.Location base = target.getLocation();

            // Random offset 50-100 blocks in X and Z
            int offsetX = (random.nextBoolean() ? 1 : -1) * (50 + random.nextInt(51));
            int offsetZ = (random.nextBoolean() ? 1 : -1) * (50 + random.nextInt(51));

            int chestX = base.getBlockX() + offsetX;
            int chestZ = base.getBlockZ() + offsetZ;
            int chestY = world.getHighestBlockYAt(chestX, chestZ) + 1;

            // Place the chest
            Block block = world.getBlockAt(chestX, chestY, chestZ);
            block.setType(Material.CHEST);
            chestLocation = block.getLocation();

            // Fill with loot
            if (block.getState() instanceof Chest chest) {
                Inventory inv = chest.getBlockInventory();
                inv.addItem(new ItemStack(Material.DIAMOND, 5));
                inv.addItem(new ItemStack(Material.EMERALD, 2));
                inv.addItem(new ItemStack(Material.IRON_INGOT, 32));
                inv.addItem(new ItemStack(Material.GOLDEN_APPLE, 1));
                chest.update();
            }

            // Send personal message with coordinates
            MessageUtil.send(target, String.format(
                    "<color:#ffaa00>🗺️ A treasure has been hidden at X: %d, Y: %d, Z: %d! Only you know this location!</color>",
                    chestX, chestY, chestZ
            ));
            MessageUtil.sendTitle(target,
                    "<color:#ffaa00>🗺️ Treasure Map!</color>",
                    "<gray>Check chat for the secret location!</gray>",
                    10, 60, 20
            );
            target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute() {
        // PERSONAL event — requires a target player
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
