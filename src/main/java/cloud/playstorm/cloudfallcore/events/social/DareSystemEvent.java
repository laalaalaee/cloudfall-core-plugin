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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DareSystemEvent implements ServerEvent, Listener {

    private final Set<UUID> activeDares = new HashSet<>();
    private boolean listenerRegistered = false;

    @Override
    public String getId() { return "dare_system"; }
    @Override
    public String getDisplayName() { return "Dare System"; }
    @Override
    public EventCategory getCategory() { return EventCategory.SOCIAL; }
    @Override
    public EventType getType() { return EventType.PERSONAL; }
    @Override
    public int getCooldownMinutes() { return 15; }
    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player player) {
        UUID id = player.getUniqueId();
        if (activeDares.contains(id)) return;
        activeDares.add(id);

        MessageUtil.sendTitle(player, "<red><b>I DARE YOU!</b></red>", "<gold>Jump into water within 60s!</gold>", 10, 70, 20);
        MessageUtil.send(player, "<red><b>[Dare]</b></red> <gold>Jump into any water block to earn a reward!</gold>");

        Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
        if (plugin != null) {
            if (!listenerRegistered) {
                Bukkit.getPluginManager().registerEvents(this, plugin);
                listenerRegistered = true;
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (activeDares.contains(id)) {
                    activeDares.remove(id);
                    Player p = Bukkit.getPlayer(id);
                    if (p != null && p.isOnline()) {
                        MessageUtil.send(p, "<red><b>[Dare]</b></red> <gray>You failed the dare...</gray>");
                    }
                }
            }, 20 * 60L); // 60 seconds
        }
    }

    @Override
    public void execute() {}

    @Override
    public void cleanup() {
        activeDares.clear();
        HandlerList.unregisterAll(this);
        listenerRegistered = false;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (activeDares.isEmpty()) return;
        Player p = event.getPlayer();
        if (!activeDares.contains(p.getUniqueId())) return;

        Material m = p.getLocation().getBlock().getType();
        if (m == Material.WATER) {
            activeDares.remove(p.getUniqueId());
            MessageUtil.sendTitle(p, "<green><b>DARE COMPLETED!</b></green>", "<gold>Enjoy your reward!</gold>", 10, 70, 20);
            p.getInventory().addItem(new ItemStack(Material.EMERALD, 5));
        }
    }
}
