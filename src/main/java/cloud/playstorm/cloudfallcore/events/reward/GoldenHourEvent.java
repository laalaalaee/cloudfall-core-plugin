package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class GoldenHourEvent implements ServerEvent, Listener {

    private boolean active = false;
    private BukkitTask timeoutTask;

    @Override
    public String getId() { return "golden_hour"; }
    @Override
    public String getDisplayName() { return "Golden Hour"; }
    @Override
    public EventCategory getCategory() { return EventCategory.REWARD; }
    @Override
    public EventType getType() { return EventType.GLOBAL; }
    @Override
    public int getCooldownMinutes() { return 60; }
    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player player) { execute(); }

    @Override
    public void execute() {
        if (active) return;
        active = true;

        MessageUtil.broadcastTitle("<gold><b>GOLDEN HOUR</b></gold>", "<yellow>Mob drops are doubled for 10 minutes!</yellow>", 20, 100, 20);
        MessageUtil.broadcast("<gold><b>[Event]</b></gold> <yellow>Golden Hour has started! All mob drops are doubled.</yellow>");

        Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
        if (plugin != null) {
            Bukkit.getPluginManager().registerEvents(this, plugin);

            timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (active) {
                    MessageUtil.broadcast("<gold><b>[Event]</b></gold> <yellow>Golden Hour has ended.</yellow>");
                    cleanup();
                }
            }, 20 * 60 * 10L); // 10 minutes
        }
    }

    @Override
    public void cleanup() {
        active = false;
        if (timeoutTask != null) timeoutTask.cancel();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!active) return;
        if (event.getEntity() instanceof Player) return; // Don't double player drops

        List<ItemStack> additionalDrops = new ArrayList<>();
        for (ItemStack drop : event.getDrops()) {
            additionalDrops.add(drop.clone());
        }
        event.getDrops().addAll(additionalDrops);
    }
}
