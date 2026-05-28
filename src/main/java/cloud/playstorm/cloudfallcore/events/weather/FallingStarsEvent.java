package cloud.playstorm.cloudfallcore.events.weather;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ThreadLocalRandom;

public class FallingStarsEvent implements ServerEvent {

    private static final Material[] STAR_ITEMS = {
            Material.DIAMOND,
            Material.EMERALD,
            Material.GOLD_INGOT,
            Material.GLOWSTONE_DUST
    };

    private BukkitTask task;

    @Override
    public String getId() { return "weather.falling_stars"; }

    @Override
    public String getDisplayName() { return "✨ Falling Stars"; }

    @Override
    public EventCategory getCategory() { return EventCategory.REWARD; }

    @Override
    public EventType getType() { return EventType.GLOBAL; }

    @Override
    public int getCooldownMinutes() { return 10; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) { }

    @Override
    public void execute() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
        if (plugin == null) return;

        // Broadcast title
        MessageUtil.broadcastTitle(
                "<color:#ffcc00>✨ Falling Stars!</color>",
                "<gray>Look up and grab them!</gray>",
                20, 60, 20
        );
        MessageUtil.broadcast("<color:#ffcc00>✨ Stars are falling from the sky! Grab them before they disappear!</color>");

        // Spawn items from the sky every 30 ticks (1.5 seconds) for 10 iterations
        task = new BukkitRunnable() {
            private int count = 0;

            @Override
            public void run() {
                if (count >= 10) {
                    cancel();
                    task = null;
                    MessageUtil.broadcast("<color:#ffcc00>✨ The falling stars have faded away.</color>");
                    return;
                }

                ThreadLocalRandom random = ThreadLocalRandom.current();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    try {
                        Location loc = player.getLocation().clone();
                        double offsetX = random.nextDouble(-5, 5);
                        double offsetZ = random.nextDouble(-5, 5);
                        Location dropLoc = new Location(
                                loc.getWorld(),
                                loc.getX() + offsetX,
                                loc.getY() + 30,
                                loc.getZ() + offsetZ
                        );

                        Material mat = STAR_ITEMS[random.nextInt(STAR_ITEMS.length)];
                        ItemStack item = new ItemStack(mat, 1);
                        player.getWorld().dropItem(dropLoc, item);
                    } catch (Exception ignored) { }
                }

                count++;
            }
        }.runTaskTimer(plugin, 20L, 30L);
    }

    @Override
    public void cleanup() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            task = null;
        }
    }
}
