package cloud.playstorm.cloudfallcore.events.mystery;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class BuriedTreasureEvent implements ServerEvent {

    private final Random random = new Random();

    @Override
    public String getId() { return "buried_treasure"; }
    @Override
    public String getDisplayName() { return "Buried Treasure"; }
    @Override
    public EventCategory getCategory() { return EventCategory.MYSTERY; }
    @Override
    public EventType getType() { return EventType.PERSONAL; }
    @Override
    public int getCooldownMinutes() { return 20; }
    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player player) {
        Location base = player.getLocation();
        
        int offsetX = (random.nextInt(60) - 30) + 30; // offset a bit further
        if (random.nextBoolean()) offsetX = -offsetX;
        int offsetZ = (random.nextInt(60) - 30) + 30;
        if (random.nextBoolean()) offsetZ = -offsetZ;

        Location treasureLoc = base.getWorld().getHighestBlockAt(base.getBlockX() + offsetX, base.getBlockZ() + offsetZ).getLocation();
        // Bury it slightly
        treasureLoc.subtract(0, random.nextInt(3) + 1, 0);

        Block block = treasureLoc.getBlock();
        block.setType(Material.CHEST);
        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            chest.getInventory().addItem(new ItemStack(Material.EMERALD, 10));
            chest.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 2));
        }

        // Give a compass if they don't have one
        player.getInventory().addItem(new ItemStack(Material.COMPASS));
        player.setCompassTarget(treasureLoc);
        
        MessageUtil.sendTitle(player, "<gold><b>TREASURE</b></gold>", "<yellow>Your compass is pointing to treasure...</yellow>", 10, 70, 20);
        MessageUtil.send(player, "<gold><b>[Treasure]</b></gold> <yellow>Follow your compass to find the buried treasure!</yellow>");
    }

    @Override
    public void execute() {}

    @Override
    public void cleanup() {}
}
