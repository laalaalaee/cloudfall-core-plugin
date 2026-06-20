package cloud.playstorm.cloudfallcore.events.social;

import cloud.playstorm.cloudfallcore.CloudFallCore;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ServerTriviaEvent implements ServerEvent, Listener {

    private final Random random = new Random();
    private boolean active = false;
    private String currentAnswer = "";

    @Override
    public String getId() { return "social.server_trivia"; }

    @Override
    public String getDisplayName() { return "❓ Server Trivia"; }

    @Override
    public EventCategory getCategory() { return EventCategory.SOCIAL; }

    @Override
    public EventType getType() { return EventType.GLOBAL; }

    @Override
    public int getCooldownMinutes() { return 15; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player player) { execute(); }

    @Override
    public void execute() {
        if (active) return;

        // Questions come from config as "Question?|answer" strings, so server
        // owners can write their own. Falls back to a built-in set.
        List<String> bank = CloudFallCore.getInstance().getConfig()
                .getStringList("events.social.server_trivia.settings.questions");
        if (bank.isEmpty()) {
            bank = Arrays.asList(
                    "What block do you mine to make a Nether portal?|obsidian",
                    "Which mob explodes when it gets close to you?|creeper",
                    "What do you feed a horse to breed it?|golden apple",
                    "What ore is needed to make a clock?|gold",
                    "Which Nether mob drops blaze rods?|blaze",
                    "What tool do you need to mine diamonds?|iron pickaxe",
                    "What item lets you fly in survival (with rockets)?|elytra"
            );
        }

        String entry = bank.get(random.nextInt(bank.size()));
        String[] parts = entry.split("\\|", 2);
        String question = parts[0].trim();
        currentAnswer = (parts.length > 1 ? parts[1] : parts[0]).trim().toLowerCase();
        active = true;

        MessageUtil.broadcast("<yellow><b>[Trivia]</b></yellow> <gold>" + question
                + "</gold> <gray>(first correct answer in chat wins!)</gray>");

        Plugin plugin = CloudFallCore.getInstance();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        int timeout = CloudFallCore.getInstance().getConfig()
                .getInt("events.social.server_trivia.settings.timeout-seconds", 45);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (active) {
                active = false;
                MessageUtil.broadcast("<yellow><b>[Trivia]</b></yellow> <red>Time's up! The answer was: <white>"
                        + currentAnswer + "</white></red>");
                HandlerList.unregisterAll(this);
            }
        }, timeout * 20L);
    }

    @Override
    public void cleanup() {
        active = false;
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!active) return;
        if (!event.getMessage().trim().equalsIgnoreCase(currentAnswer)) return;
        active = false;
        Player winner = event.getPlayer();

        Plugin plugin = CloudFallCore.getInstance();
        Bukkit.getScheduler().runTask(plugin, () -> {
            double currency = CloudFallCore.getInstance().getConfig()
                    .getDouble("events.social.server_trivia.settings.currency-reward", 500);
            int diamonds = CloudFallCore.getInstance().getConfig()
                    .getInt("events.social.server_trivia.settings.reward-diamonds", 3);
            CloudFallCore.getInstance().getEconomy().deposit(winner, currency);
            if (diamonds > 0) winner.getInventory().addItem(new ItemStack(Material.DIAMOND, diamonds));
            MessageUtil.broadcast("<yellow><b>[Trivia]</b></yellow> <green>" + winner.getName()
                    + " got it right!</green>");
        });
        HandlerList.unregisterAll(this);
    }
}
