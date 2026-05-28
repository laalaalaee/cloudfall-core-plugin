package cloud.playstorm.cloudfallcore.events.combat;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PhantomFlockEvent implements ServerEvent {

    @Override
    public String getId() { return "combat.phantom_flock"; }

    @Override
    public String getDisplayName() { return "🦇 Phantom Flock"; }

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
                "<color:#9900ff>🦇 PHANTOM FLOCK!</color>",
                "<gray>They're coming from above! (5s)</gray>",
                10, 60, 20);

        Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
        if (plugin == null) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                int mobCount = Math.max(3, Bukkit.getOnlinePlayers().size() * 2);
                int phantomCount = Math.max(2, mobCount / 2);
                Location center = target.getLocation();

                for (int i = 0; i < phantomCount; i++) {
                    double angle = (2 * Math.PI / phantomCount) * i;
                    Location spawnLoc = center.clone().add(Math.cos(angle) * 8, 10, Math.sin(angle) * 8);

                    Phantom phantom = (Phantom) center.getWorld().spawnEntity(spawnLoc, EntityType.PHANTOM);
                    phantom.setCustomName("§5Flock Phantom");
                    phantom.setCustomNameVisible(true);
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
