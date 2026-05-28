package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FreeFoodEvent implements ServerEvent {

    @Override
    public String getId() { return "reward.free_food"; }

    @Override
    public String getDisplayName() { return "🥩 Free Food"; }

    @Override
    public EventCategory getCategory() { return EventCategory.REWARD; }

    @Override
    public EventType getType() { return EventType.GLOBAL; }

    @Override
    public int getCooldownMinutes() { return 10; }

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
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 16));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
            }

            MessageUtil.broadcastTitle(
                    "<color:#ff8800>🥩 Free Food!</color>",
                    "<gray>The server gods provide a feast!</gray>",
                    10, 60, 20
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() {
        // Nothing to clean up
    }
}
