package cloud.playstorm.cloudfallcore.events.horror;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class StalkerNotificationEvent implements ServerEvent {

    private BukkitRunnable activeTask;

    @Override
    public String getId() { return "horror.stalker_notification"; }

    @Override
    public String getDisplayName() { return "👁 Stalker Notification"; }

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
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
        if (plugin == null) return;

        try {
            target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 0.3f, 1.0f);
        } catch (Exception ignored) {}

        activeTask = new BukkitRunnable() {
            int iterations = 0;
            final int maxIterations = 30;

            @Override
            public void run() {
                if (!target.isOnline() || iterations >= maxIterations) {
                    this.cancel();
                    return;
                }

                try {
                    MessageUtil.sendActionBar(target, "<color:#ff0000>⚠ Someone is watching you...</color>");
                } catch (Exception ignored) {}

                iterations++;
            }
        };
        activeTask.runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public void execute() { }

    @Override
    public void cleanup() {
        if (activeTask != null && !activeTask.isCancelled()) {
            try {
                activeTask.cancel();
            } catch (Exception ignored) {}
        }
    }
}
