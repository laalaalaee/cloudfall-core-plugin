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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A scrambled word appears in chat; first to unscramble it wins. Word list is
 * fully configurable so you can theme it to your server.
 */
public class UnscrambleEvent extends BaseEvent implements Listener {

    private final Random random = new Random();
    private boolean active = false;
    private String word;

    @Override public String getId() { return "social.unscramble"; }
    @Override public String getDisplayName() { return "🔤 Unscramble"; }
    @Override public EventCategory getCategory() { return EventCategory.SOCIAL; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 20; }
    @Override public int getWeight() { return 10; }

    @Override
    public void execute() {
        if (active) return;
        active = true;

        List<String> words = settingList("words");
        if (words.isEmpty()) {
            words = Arrays.asList("diamond", "creeper", "minecraft", "redstone", "enderman",
                    "village", "netherite", "obsidian", "skeleton", "treasure");
        }
        word = words.get(random.nextInt(words.size())).trim().toLowerCase();

        MessageUtil.broadcast("<light_purple>🔤 <b>Unscramble!</b> First to solve wins: <yellow>"
                + scramble(word) + "</yellow></light_purple>");

        Bukkit.getPluginManager().registerEvents(this, plugin());
        int timeout = settingInt("timeout-seconds", 30);
        Bukkit.getScheduler().runTaskLater(plugin(), () -> {
            if (active) {
                active = false;
                HandlerList.unregisterAll(this);
                MessageUtil.broadcast("<gray>🔤 Nobody solved it! The word was <yellow>" + word + "</yellow>.</gray>");
            }
        }, timeout * 20L);
    }

    private String scramble(String input) {
        for (int attempt = 0; attempt < 10; attempt++) {
            List<Character> chars = new ArrayList<>();
            for (char c : input.toCharArray()) chars.add(c);
            Collections.shuffle(chars, random);
            StringBuilder sb = new StringBuilder();
            for (char c : chars) sb.append(c);
            String result = sb.toString();
            if (!result.equals(input)) return result;
        }
        return new StringBuilder(input).reverse().toString();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!active) return;
        if (!event.getMessage().trim().equalsIgnoreCase(word)) return;
        active = false;
        Player winner = event.getPlayer();
        Bukkit.getScheduler().runTask(plugin(), () -> {
            payout(winner, settingDouble("currency-reward", 500));
            winner.getInventory().addItem(new ItemStack(Material.DIAMOND, settingInt("reward-diamonds", 3)));
            MessageUtil.broadcast("<light_purple>🔤 <yellow>" + winner.getName()
                    + "</yellow> unscrambled <white>" + word + "</white> first!</light_purple>");
        });
        HandlerList.unregisterAll(this);
    }

    @Override
    public void cleanup() {
        active = false;
        HandlerList.unregisterAll(this);
    }
}
