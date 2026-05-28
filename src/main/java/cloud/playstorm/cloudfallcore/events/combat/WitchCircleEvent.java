package cloud.playstorm.cloudfallcore.events.combat;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Witch;
import org.bukkit.plugin.Plugin;

public class WitchCircleEvent implements ServerEvent {

    @Override
    public String getId() { return "combat.witch_circle"; }

    @Override
    public String getDisplayName() { return "🧙 Witch Circle"; }

    @Override
    public EventCategory getCategory() { return EventCategory.COMBAT; }

    @Override
    public EventType getType() { return EventType.PERSONAL; }

    @Override
    public int getCooldownMinutes() { return 15; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) {
        MessageUtil.sendTitle(target,
                "<color:#aa00aa>🧙 WITCH CIRCLE!</color>",
                "<gray>Dark magic approaches! (5s)</gray>",
                10, 60, 20);

        Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
        if (plugin == null) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                int mobCount = Math.max(3, Bukkit.getOnlinePlayers().size() * 2);
                int witchCount = Math.max(3, mobCount / 2);
                Location center = target.getLocation();

                for (int i = 0; i < witchCount; i++) {
                    double angle = (2 * Math.PI / witchCount) * i;
                    Location spawnLoc = center.clone().add(Math.cos(angle) * 8, 0, Math.sin(angle) * 8);
                    spawnLoc.setY(center.getWorld().getHighestBlockYAt(spawnLoc) + 1);

                    Witch witch = (Witch) center.getWorld().spawnEntity(spawnLoc, EntityType.WITCH);
                    witch.setCustomName("§5Circle Witch");
                    witch.setCustomNameVisible(true);
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
