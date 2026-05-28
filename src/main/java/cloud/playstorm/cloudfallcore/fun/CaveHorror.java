package cloud.playstorm.cloudfallcore.fun;

import cloud.playstorm.cloudfallcore.CloudFallCore;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class CaveHorror {

    private final CloudFallCore plugin;
    private BukkitTask task;
    private final Random random = new Random();

    // Some creepy sounds
    private final Sound[] creepySounds = {
            Sound.AMBIENT_CAVE,
            Sound.ENTITY_CREEPER_PRIMED,
            Sound.ENTITY_ENDERMAN_STARE,
            Sound.ENTITY_ZOMBIE_VILLAGER_CURE,
            Sound.BLOCK_PORTAL_AMBIENT
    };

    public CaveHorror(CloudFallCore plugin) {
        this.plugin = plugin;
    }

    public void start() {
        int chance = plugin.getConfig().getInt("mechanics.cave-horror.chance", 15);
        int yLevel = plugin.getConfig().getInt("mechanics.cave-horror.y-level", 10);

        // Run every 10 seconds (200 ticks)
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getGameMode() != GameMode.SURVIVAL) continue;
                if (player.getLocation().getY() <= yLevel) {
                    if (random.nextInt(100) < chance) {
                        Sound sound = creepySounds[random.nextInt(creepySounds.length)];
                        // Play sound just to this player, behind them
                        player.playSound(player.getLocation().add(player.getLocation().getDirection().multiply(-2)), sound, 0.7f, 0.5f);
                    }
                }
            }
        }, 200L, 200L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }
}
