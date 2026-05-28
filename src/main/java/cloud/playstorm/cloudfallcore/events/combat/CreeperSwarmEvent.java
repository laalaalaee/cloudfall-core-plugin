package cloud.playstorm.cloudfallcore.events.combat;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CreeperSwarmEvent implements ServerEvent {

    @Override
    public String getId() { return "combat.creeper_swarm"; }

    @Override
    public String getDisplayName() { return "💥 Creeper Swarm"; }

    @Override
    public EventCategory getCategory() { return EventCategory.COMBAT; }

    @Override
    public EventType getType() { return EventType.PERSONAL; }

    @Override
    public int getCooldownMinutes() { return 20; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) {
        MessageUtil.sendTitle(target,
                "<color:#00ff00>💥 CREEPER SWARM!</color>",
                "<gray>Run for cover! (5s)</gray>",
                10, 60, 20);

        Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
        if (plugin == null) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                int mobCount = Math.max(3, Bukkit.getOnlinePlayers().size() * 2);
                Location center = target.getLocation();

                for (int i = 0; i < mobCount; i++) {
                    double angle = (2 * Math.PI / mobCount) * i;
                    Location spawnLoc = center.clone().add(Math.cos(angle) * 15, 0, Math.sin(angle) * 15);
                    spawnLoc.setY(center.getWorld().getHighestBlockYAt(spawnLoc) + 1);

                    Creeper creeper = (Creeper) center.getWorld().spawnEntity(spawnLoc, EntityType.CREEPER);
                    creeper.setPowered(true);
                    creeper.setCustomName("§aSwarm Creeper");
                    creeper.setCustomNameVisible(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 100L);
    }

    @Override
    public void execute() { }

    @Override
    public void cleanup() { }
}
