package cloud.playstorm.cloudfallcore.events.mystery;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A meteor slams into the world, leaving a crater of magma and a loot vault
 * guarded by monsters. The coordinates are broadcast - reach it, fight through,
 * and claim the prize before it cools and vanishes.
 */
public class LootMeteorEvent extends BaseEvent {

    private final Random random = new Random();
    private Location chestLocation;

    @Override public String getId() { return "mystery.loot_meteor"; }
    @Override public String getDisplayName() { return "☄️ Loot Meteor"; }
    @Override public EventCategory getCategory() { return EventCategory.MYSTERY; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 40; }
    @Override public int getDurationSeconds() { return 600; }
    @Override public int getWeight() { return 9; }

    @Override
    public void execute() {
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (online.isEmpty()) return;

        Player anchor = online.get(random.nextInt(online.size()));
        World world = anchor.getWorld();

        int min = settingInt("min-distance", 80);
        int max = Math.max(min + 40, settingInt("max-distance", 200));
        int distance = min + random.nextInt(max - min);
        double angle = random.nextDouble() * Math.PI * 2;
        int x = anchor.getLocation().getBlockX() + (int) (Math.cos(angle) * distance);
        int z = anchor.getLocation().getBlockZ() + (int) (Math.sin(angle) * distance);

        // Load the impact chunk OFF the main thread to avoid a chunk-generation
        // freeze (ping spike); the callback runs back on the main thread.
        world.getChunkAtAsync(x >> 4, z >> 4).thenAccept(chunk -> {
            Block surface = world.getHighestBlockAt(x, z);
            Location impact = surface.getLocation().add(0.5, 1, 0.5);

            // Scorched crater.
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx * dx + dz * dz > 5) continue;
                    Block b = world.getHighestBlockAt(x + dx, z + dz);
                    if (random.nextBoolean()) b.setType(Material.MAGMA_BLOCK);
                }
            }

            // Loot chest.
            Block chestBlock = surface.getRelative(0, 1, 0);
            chestBlock.setType(Material.CHEST);
            chestLocation = chestBlock.getLocation();
            if (chestBlock.getState() instanceof Chest chest) {
                chest.getBlockInventory().addItem(new ItemStack(Material.DIAMOND, 4 + random.nextInt(6)));
                chest.getBlockInventory().addItem(new ItemStack(Material.GOLD_BLOCK, 1 + random.nextInt(3)));
                chest.getBlockInventory().addItem(new ItemStack(Material.NETHERITE_SCRAP, random.nextInt(2)));
                chest.update();
            }

            // Guardians.
            EntityType guard = pickGuard();
            int guards = settingInt("guards", 3);
            for (int i = 0; i < guards; i++) {
                Location gl = impact.clone().add(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
                world.spawnEntity(gl, guard);
            }

            world.strikeLightningEffect(impact);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 0.8f);
            }

            MessageUtil.broadcastTitle("<gold>☄️ Meteor Impact!</gold>",
                    "<gray>X: " + (int) impact.getX() + "  Z: " + (int) impact.getZ() + "</gray>", 15, 90, 20);
            MessageUtil.broadcast("<gold>☄️ A <b>Loot Meteor</b> crashed at <yellow>X:" + (int) impact.getX()
                    + " Z:" + (int) impact.getZ() + "</yellow>! It's guarded - claim the loot before it vanishes!</gold>");
        });
    }

    private EntityType pickGuard() {
        try {
            return EntityType.valueOf(settingString("guard-type", "BLAZE").toUpperCase());
        } catch (IllegalArgumentException e) {
            return EntityType.BLAZE;
        }
    }

    @Override
    public void cleanup() {
        if (chestLocation != null) {
            Block block = chestLocation.getBlock();
            if (block.getType() == Material.CHEST) {
                block.setType(Material.AIR);
                MessageUtil.broadcast("<gray>☄️ The meteor's remains have cooled and crumbled away.</gray>");
            }
            chestLocation = null;
        }
    }
}
