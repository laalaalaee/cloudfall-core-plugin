package cloud.playstorm.cloudfallcore.events.horror;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InventoryShuffleEvent implements ServerEvent {

    @Override
    public String getId() { return "horror.inventory_shuffle"; }

    @Override
    public String getDisplayName() { return "🔀 Inventory Shuffle"; }

    @Override
    public EventCategory getCategory() { return EventCategory.HORROR; }

    @Override
    public EventType getType() { return EventType.PERSONAL; }

    @Override
    public int getCooldownMinutes() { return 10; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) {
        try {
            // Collect hotbar items (slots 0-8)
            List<ItemStack> hotbarItems = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                hotbarItems.add(target.getInventory().getItem(i));
            }

            // Shuffle the items
            Collections.shuffle(hotbarItems);

            // Set them back in shuffled order
            for (int i = 0; i < 9; i++) {
                target.getInventory().setItem(i, hotbarItems.get(i));
            }

            target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.0f);
            MessageUtil.sendActionBar(target, "<color:#ff00ff>Your inventory feels... different</color>");
        } catch (Exception ignored) {}
    }

    @Override
    public void execute() { }

    @Override
    public void cleanup() { }
}
