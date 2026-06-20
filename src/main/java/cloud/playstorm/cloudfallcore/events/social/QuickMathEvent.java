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
 * A maths problem appears in chat; the first player to type the correct answer
 * wins. Pure skill/speed, and it works with as few as one player online.
 */
public class QuickMathEvent extends BaseEvent implements Listener {

    private final Random random = new Random();
    private boolean active = false;
    private int answer;

    @Override public String getId() { return "social.quick_math"; }
    @Override public String getDisplayName() { return "🧮 Quick Maths"; }
    @Override public EventCategory getCategory() { return EventCategory.SOCIAL; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 20; }
    @Override public int getWeight() { return 10; }

    @Override
    public void execute() {
        if (active) return;
        active = true;

        int a = 2 + random.nextInt(18);
        int b = 2 + random.nextInt(18);
        boolean multiply = random.nextBoolean();
        String question;
        if (multiply) { answer = a * b; question = a + " × " + b; }
        else { answer = a + b; question = a + " + " + b; }

        MessageUtil.broadcast("<aqua>🧮 <b>Quick Maths!</b> First to answer in chat wins: <yellow>"
                + question + "</yellow> = ?</aqua>");

        Bukkit.getPluginManager().registerEvents(this, plugin());
        int timeout = settingInt("timeout-seconds", 30);
        Bukkit.getScheduler().runTaskLater(plugin(), () -> {
            if (active) {
                active = false;
                HandlerList.unregisterAll(this);
                MessageUtil.broadcast("<gray>🧮 Time's up! The answer was <yellow>" + answer + "</yellow>.</gray>");
            }
        }, timeout * 20L);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!active) return;
        String msg = event.getMessage().trim();
        if (!msg.equals(String.valueOf(answer))) return;
        active = false;
        Player winner = event.getPlayer();
        Bukkit.getScheduler().runTask(plugin(), () -> {
            payout(winner, settingDouble("currency-reward", 500));
            winner.getInventory().addItem(new ItemStack(Material.DIAMOND, settingInt("reward-diamonds", 3)));
            MessageUtil.broadcast("<aqua>🧮 <yellow>" + winner.getName() + "</yellow> solved it first! ("
                    + answer + ")</aqua>");
        });
        HandlerList.unregisterAll(this);
    }

    @Override
    public void cleanup() {
        active = false;
        HandlerList.unregisterAll(this);
    }
}
