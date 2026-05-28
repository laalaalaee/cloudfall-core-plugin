package cloud.playstorm.cloudfallcore.events.combat;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ZombieSiegeEvent implements ServerEvent {

    @Override
    public String getId() { return "combat.zombie_siege"; }

    @Override
    public String getDisplayName() { return "⚔️ Zombie Siege"; }

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
                "<color:#ff0000>⚔️ ZOMBIE SIEGE!</color>",
                "<gray>Prepare yourself! (5s)</gray>",
                10, 60, 20);

        Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
        if (plugin == null) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                int mobCount = Math.max(3, Bukkit.getOnlinePlayers().size() * 2);
                Location center = target.getLocation();

                for (int i = 0; i < mobCount; i++) {
                    double angle = (2 * Math.PI / mobCount) * i;
                    Location spawnLoc = center.clone().add(Math.cos(angle) * 12, 0, Math.sin(angle) * 12);
                    spawnLoc.setY(center.getWorld().getHighestBlockYAt(spawnLoc) + 1);

                    Zombie zombie = (Zombie) center.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
                    zombie.setCustomName("§cSiege Zombie");
                    zombie.setCustomNameVisible(true);

                    // Last zombie drops 3 diamonds
                    if (i == mobCount - 1) {
                        zombie.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND, 3));
                        zombie.getEquipment().setItemInMainHandDropChance(1.0f);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 100L); // 5 seconds = 100 ticks
    }

    @Override
    public void execute() { }

    @Override
    public void cleanup() { }
}
