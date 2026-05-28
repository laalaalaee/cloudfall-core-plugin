package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LuckyPlayerEvent implements ServerEvent {

    @Override
    public String getId() { return "reward.lucky_player"; }

    @Override
    public String getDisplayName() { return "🍀 Lucky Player"; }

    @Override
    public EventCategory getCategory() { return EventCategory.REWARD; }

    @Override
    public EventType getType() { return EventType.PERSONAL; }

    @Override
    public int getCooldownMinutes() { return 10; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) {
        target.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        MessageUtil.sendTitle(target, "<color:#55ff55>Lucky!</color>", "<gray>A diamond appeared in your pocket...</gray>", 10, 60, 20);
    }

    @Override
    public void execute() { }

    @Override
    public void cleanup() { }
}
