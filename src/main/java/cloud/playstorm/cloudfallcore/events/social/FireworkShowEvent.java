package cloud.playstorm.cloudfallcore.events.social;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class FireworkShowEvent implements ServerEvent {

    private final Random random = new Random();

    @Override
    public String getId() { return "social.firework_show"; }

    @Override
    public String getDisplayName() { return "🎆 Firework Show"; }

    @Override
    public EventCategory getCategory() { return EventCategory.SOCIAL; }

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

        try {
            World world = Bukkit.getWorlds().get(0);
            Location spawnLoc = world.getSpawnLocation();

            MessageUtil.broadcastTitle(
                    "<color:#ff55ff>🎆 Firework Show!</color>",
                    "<gray>Enjoy the spectacle!</gray>",
                    10, 60, 20);

            new BukkitRunnable() {
                int count = 0;

                @Override
                public void run() {
                    if (count >= 15) {
                        this.cancel();
                        return;
                    }

                    try {
                        Location fwLoc = spawnLoc.clone().add(
                                random.nextInt(20) - 10, 0, random.nextInt(20) - 10);

                        Firework fw = (Firework) world.spawnEntity(fwLoc, EntityType.FIREWORK_ROCKET);
                        FireworkMeta meta = fw.getFireworkMeta();

                        FireworkEffect effect = FireworkEffect.builder()
                                .withColor(Color.fromRGB(
                                        random.nextInt(256), random.nextInt(256), random.nextInt(256)))
                                .withFade(Color.fromRGB(
                                        random.nextInt(256), random.nextInt(256), random.nextInt(256)))
                                .with(FireworkEffect.Type.values()[
                                        random.nextInt(FireworkEffect.Type.values().length)])
                                .trail(random.nextBoolean())
                                .flicker(random.nextBoolean())
                                .build();

                        meta.addEffect(effect);
                        meta.setPower(random.nextInt(2) + 1);
                        fw.setFireworkMeta(meta);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    count++;
                }
            }.runTaskTimer(plugin, 0L, 7L);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() { }
}
