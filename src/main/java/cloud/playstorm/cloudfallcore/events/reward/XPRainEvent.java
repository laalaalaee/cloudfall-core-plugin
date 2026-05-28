package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class XPRainEvent implements ServerEvent {

    private BukkitRunnable task;

    @Override
    public String getId() { return "reward.xp_rain"; }

    @Override
    public String getDisplayName() { return "✨ XP Rain"; }

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
            Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
            if (plugin == null) return;

            MessageUtil.broadcastTitle(
                    "<color:#55ff55>✨ XP Rain!</color>",
                    "<gray>Look up! Experience is falling from the sky!</gray>",
                    10, 60, 20
            );

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            }

            // 30 seconds = 600 ticks, run every 20 ticks = 30 iterations
            task = new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (ticks >= 600) {
                        cancel();
                        return;
                    }

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Location loc = player.getLocation().add(0, 3, 0);
                        for (int i = 0; i < 10; i++) {
                            try {
                                player.getWorld().spawn(loc, ExperienceOrb.class, orb -> orb.setExperience(10));
                            } catch (Exception e) {
                                // Silently skip if spawn fails
                            }
                        }
                    }

                    ticks += 20;
                }
            };
            task.runTaskTimer(plugin, 0L, 20L);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() {
        if (task != null) {
            try {
                task.cancel();
            } catch (Exception e) {
                // Already cancelled
            }
            task = null;
        }
    }
}
