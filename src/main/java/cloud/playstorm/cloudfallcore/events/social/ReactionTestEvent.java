package cloud.playstorm.cloudfallcore.events.social;

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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * After a random delay the word GO appears - first to type it wins. A test of
 * pure reaction time; typing early just means you wait like everyone else.
 */
public class ReactionTestEvent extends BaseEvent implements Listener {

    private final Random random = new Random();
    private boolean active = false;
    private boolean started = false;

    @Override public String getId() { return "social.reaction_test"; }
    @Override public String getDisplayName() { return "⚡ Reaction Test"; }
    @Override public EventCategory getCategory() { return EventCategory.SOCIAL; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 20; }
    @Override public int getWeight() { return 10; }

    @Override
    public void execute() {
        if (active) return;
        active = true;
        started = false;

        MessageUtil.broadcast("<gold>⚡ <b>Reaction Test!</b> Get ready... type <yellow>go</yellow> the moment it appears!</gold>");
        Bukkit.getPluginManager().registerEvents(this, plugin());

        int delay = 60 + random.nextInt(140); // 3-10 seconds
        Bukkit.getScheduler().runTaskLater(plugin(), () -> {
            if (!active) return;
            started = true;
            MessageUtil.broadcast("<green><b>⚡ GO!! Type 'go' NOW!</b></green>");
            // Auto-expire if nobody answers.
            Bukkit.getScheduler().runTaskLater(plugin(), () -> {
                if (active) {
                    active = false;
                    HandlerList.unregisterAll(this);
                    MessageUtil.broadcast("<gray>⚡ Too slow, everyone! No winner this time.</gray>");
                }
            }, 200L);
        }, delay);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!active || !started) return;
        if (!event.getMessage().trim().equalsIgnoreCase("go")) return;
        active = false;
        Player winner = event.getPlayer();
        Bukkit.getScheduler().runTask(plugin(), () -> {
            payout(winner, settingDouble("currency-reward", 500));
            winner.getInventory().addItem(new ItemStack(Material.DIAMOND, settingInt("reward-diamonds", 3)));
            MessageUtil.broadcast("<gold>⚡ <yellow>" + winner.getName() + "</yellow> had the fastest reaction!</gold>");
        });
        HandlerList.unregisterAll(this);
    }

    @Override
    public void cleanup() {
        active = false;
        started = false;
        HandlerList.unregisterAll(this);
    }
}
