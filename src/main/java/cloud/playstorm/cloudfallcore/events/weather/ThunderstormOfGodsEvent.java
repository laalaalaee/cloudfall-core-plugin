package cloud.playstorm.cloudfallcore.events.weather;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ThunderstormOfGodsEvent implements ServerEvent {

    private BukkitTask task;

    @Override
    public String getId() { return "weather.thunderstorm_of_gods"; }

    @Override
    public String getDisplayName() { return "⚡ Thunderstorm of the Gods"; }

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

        // Set all worlds to thunderstorm
        for (World world : Bukkit.getWorlds()) {
            world.setStorm(true);
            world.setThundering(true);
            world.setWeatherDuration(20 * 60 * 2); // 2 minutes
            world.setThunderDuration(20 * 60 * 2);
        }

        // Broadcast title
        MessageUtil.broadcastTitle(
                "<color:#ffff00>⚡ Thunderstorm of the Gods!</color>",
                "<gray>The gods are angry!</gray>",
                20, 60, 20
        );
        MessageUtil.broadcast("<color:#ffff00>⚡ The gods have unleashed their fury! Lightning strikes everywhere!</color>");

        // Strike lightning near random players every 100 ticks for 2 minutes
        // 2 minutes = 2400 ticks / 100 ticks = 24 iterations
        task = new BukkitRunnable() {
            private int count = 0;

            @Override
            public void run() {
                if (count >= 24) {
                    cleanup();
                    return;
                }

                try {
                    List<? extends Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                    if (players.isEmpty()) return;

                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    Player target = players.get(random.nextInt(players.size()));
                    Location loc = target.getLocation().clone();

                    // Random offset 3-10 blocks away
                    double offsetX = random.nextInt(3, 11) * (random.nextBoolean() ? 1 : -1);
                    double offsetZ = random.nextInt(3, 11) * (random.nextBoolean() ? 1 : -1);
                    loc.add(offsetX, 0, offsetZ);

                    // Strike lightning at the offset location
                    loc.getWorld().strikeLightning(loc);
                } catch (Exception ignored) { }

                count++;
            }
        }.runTaskTimer(plugin, 20L, 100L);
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

        MessageUtil.broadcast("<color:#ffff00>⚡ The Thunderstorm of the Gods has subsided.</color>");
    }
}
