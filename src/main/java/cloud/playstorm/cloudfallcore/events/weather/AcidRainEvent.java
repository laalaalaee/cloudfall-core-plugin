package cloud.playstorm.cloudfallcore.events.weather;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class AcidRainEvent implements ServerEvent {

    private BukkitTask task;

    @Override
    public String getId() { return "weather.acid_rain"; }

    @Override
    public String getDisplayName() { return "☢️ Acid Rain"; }

    @Override
    public EventCategory getCategory() { return EventCategory.WEATHER; }

    @Override
    public EventType getType() { return EventType.GLOBAL; }

    @Override
    public int getCooldownMinutes() { return 15; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) { }

    @Override
    public void execute() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
        if (plugin == null) return;

        // Set weather to storm in all worlds
        for (World world : Bukkit.getWorlds()) {
            world.setStorm(true);
            world.setThundering(false);
            world.setWeatherDuration(20 * 60 * 3); // 3 minutes
        }

        // Broadcast title
        MessageUtil.broadcastTitle(
                "<color:#00ff00>☢️ Acid Rain!</color>",
                "<gray>Get under cover!</gray>",
                20, 60, 20
        );
        MessageUtil.broadcast("<color:#00ff00>☢️ Acid Rain is falling! Get under shelter immediately!</color>");

        // Start repeating task - check every 40 ticks for exposed players
        task = new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                ticks += 40;
                // Run for 3 minutes (3600 ticks)
                if (ticks >= 3600) {
                    cleanup();
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    try {
                        int playerY = player.getLocation().getBlockY();
                        int highestY = player.getWorld().getHighestBlockYAt(player.getLocation());
                        // If player is at or above the highest block, they are exposed
                        if (playerY >= highestY) {
                            player.addPotionEffect(new PotionEffect(
                                    PotionEffectType.POISON, 200, 0, false, true, true
                            ));
                            MessageUtil.sendActionBar(player, "<color:#00ff00>☢️ The acid rain burns!</color>");
                        }
                    } catch (Exception ignored) { }
                }
            }
        }.runTaskTimer(plugin, 0L, 40L);
    }

    @Override
    public void cleanup() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            task = null;
        }

        // Clear weather
        for (World world : Bukkit.getWorlds()) {
            world.setStorm(false);
            world.setThundering(false);
        }

        // Remove poison from all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.removePotionEffect(PotionEffectType.POISON);
        }

        MessageUtil.broadcast("<color:#00ff00>☢️ The acid rain has stopped.</color>");
    }
}
