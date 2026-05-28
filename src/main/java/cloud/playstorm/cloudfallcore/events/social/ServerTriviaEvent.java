package cloud.playstorm.cloudfallcore.events.social;

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

public class ServerTriviaEvent implements ServerEvent, Listener {

    private boolean active = false;
    private String currentAnswer = "";

    @Override
    public String getId() { return "server_trivia"; }

    @Override
    public String getDisplayName() { return "Server Trivia"; }

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
        active = true;
        currentAnswer = "minecraft"; // A very simple static trivia question for demonstration
        
        MessageUtil.broadcast("<yellow><b>[Trivia]</b></yellow> <gold>First to type</gold> <green>" + currentAnswer + "</green> <gold>in chat wins diamonds!</gold>");
        
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
        if (plugin != null) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (active) {
                    active = false;
                    MessageUtil.broadcast("<yellow><b>[Trivia]</b></yellow> <red>Time's up! No one got the answer.</red>");
                    HandlerList.unregisterAll(this);
                }
            }, 20 * 60L); // 60 seconds
        }
    }

    @Override
    public void cleanup() {
        active = false;
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!active) return;
        if (event.getMessage().equalsIgnoreCase(currentAnswer)) {
            active = false;
            Player winner = event.getPlayer();
            
            // Dispatch inventory update on main thread
            Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
            if (plugin != null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    MessageUtil.broadcast("<yellow><b>[Trivia]</b></yellow> <green>" + winner.getName() + " got the answer first!</green>");
                    winner.getInventory().addItem(new ItemStack(Material.DIAMOND, 3));
                });
            }
            
            HandlerList.unregisterAll(this);
        }
    }
}
