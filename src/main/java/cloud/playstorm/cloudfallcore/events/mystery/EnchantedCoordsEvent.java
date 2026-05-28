package cloud.playstorm.cloudfallcore.events.mystery;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnchantedCoordsEvent implements ServerEvent {

    private final Random random = new Random();

    @Override
    public String getId() { return "mystery.enchanted_coords"; }

    @Override
    public String getDisplayName() { return "🔮 Enchanted Coords"; }

    @Override
    public EventCategory getCategory() { return EventCategory.MYSTERY; }

    @Override
    public EventType getType() { return EventType.GLOBAL; }

    @Override
    public int getCooldownMinutes() { return 15; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) { }

    @Override
    public void execute() {
        try {
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            if (players.isEmpty()) return;

            Player randomPlayer = players.get(random.nextInt(players.size()));
            World world = randomPlayer.getWorld();

            // Random coords 100-200 blocks away
            int offsetX = (random.nextBoolean() ? 1 : -1) * (100 + random.nextInt(101));
            int offsetZ = (random.nextBoolean() ? 1 : -1) * (100 + random.nextInt(101));
            int x = randomPlayer.getLocation().getBlockX() + offsetX;
            int z = randomPlayer.getLocation().getBlockZ() + offsetZ;
            int y = world.getHighestBlockYAt(x, z);

            // Place chest at surface
            Location chestLoc = new Location(world, x, y + 1, z);
            world.getBlockAt(chestLoc).setType(Material.CHEST);

            // Fill the chest
            Chest chest = (Chest) world.getBlockAt(chestLoc).getState();

            // Random high-level enchanted book
            ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) enchantedBook.getItemMeta();

            // Pick a random high-level enchantment
            Object[][] enchantOptions = {
                    {Enchantment.PROTECTION, 4},
                    {Enchantment.SHARPNESS, 5},
                    {Enchantment.EFFICIENCY, 5},
                    {Enchantment.FORTUNE, 3},
                    {Enchantment.UNBREAKING, 3}
            };

            Object[] chosen = enchantOptions[random.nextInt(enchantOptions.length)];
            meta.addStoredEnchant((Enchantment) chosen[0], (int) chosen[1], true);
            enchantedBook.setItemMeta(meta);

            chest.getInventory().addItem(enchantedBook);
            chest.getInventory().addItem(new ItemStack(Material.DIAMOND, 5));

            // Broadcast coords
            MessageUtil.broadcast("<color:#aa00ff>🔮 Enchanted coordinates revealed: X: " +
                    x + ", Z: " + z + "! First to arrive claims the prize!</color>");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() { }
}
