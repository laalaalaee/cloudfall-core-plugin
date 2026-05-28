package cloud.playstorm.cloudfallcore.fun;

import cloud.playstorm.cloudfallcore.CloudFallCore;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class BloodMoonEvent implements Listener {

    private final CloudFallCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private BukkitTask task;
    private final Random random = new Random();

    private boolean isBloodMoonActive = false;
    private boolean checkedToday = false;

    public BloodMoonEvent(CloudFallCore plugin) {
        this.plugin = plugin;
    }

    public void start() {
        int chance = plugin.getConfig().getInt("mechanics.blood-moon.chance", 10);

        // Check time every 5 seconds (100 ticks)
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            World world = Bukkit.getWorlds().get(0); // Main overworld
            long time = world.getTime();

            // Dusk is around 13000
            if (time >= 13000 && time <= 14000) {
                if (!checkedToday) {
                    checkedToday = true;
                    if (random.nextInt(100) < chance) {
                        isBloodMoonActive = true;
                        Bukkit.getServer().sendMessage(miniMessage.deserialize("<color:#aa0000><bold>The Blood Moon rises... The night belongs to the monsters.</bold></color>"));
                    }
                }
            } else if (time >= 0 && time < 1000) {
                // Morning resets
                if (isBloodMoonActive) {
                    Bukkit.getServer().sendMessage(miniMessage.deserialize("<color:#55ff55>The Blood Moon has set... You have survived.</color>"));
                }
                isBloodMoonActive = false;
                checkedToday = false;
            }
        }, 100L, 100L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        if (!isBloodMoonActive) return;

        if (event.getEntity() instanceof Monster) {
            Monster monster = (Monster) event.getEntity();
            // Buff the monster
            monster.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 0));
            monster.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 999999, 0));
        }
    }
}
