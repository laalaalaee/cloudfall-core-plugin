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

public class TheScreamerEvent implements ServerEvent {

    @Override
    public String getId() { return "horror.the_screamer"; }

    @Override
    public String getDisplayName() { return "😱 The Screamer"; }

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
        if (target.getLocation().getY() >= 50) return;

        Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
        if (plugin == null) return;

        try {
            target.playSound(target.getLocation(), Sound.ENTITY_GHAST_SCREAM, 0.8f, 1.0f);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!target.isOnline()) return;
                    try {
                        target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 0.8f, 0.7f);
                        MessageUtil.sendActionBar(target, "<color:#ff0000>What was that?!</color>");
                    } catch (Exception ignored) {}
                }
            }.runTaskLater(plugin, 20L);
        } catch (Exception ignored) {}
    }

    @Override
    public void execute() { }

    @Override
    public void cleanup() { }
}
