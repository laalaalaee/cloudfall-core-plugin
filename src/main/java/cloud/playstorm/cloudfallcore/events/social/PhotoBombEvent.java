package cloud.playstorm.cloudfallcore.events.social;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class PhotoBombEvent implements ServerEvent {

    @Override
    public String getId() { return "social.photo_bomb"; }

    @Override
    public String getDisplayName() { return "📸 Photo Bomb"; }

    @Override
    public EventCategory getCategory() { return EventCategory.SOCIAL; }

    @Override
    public EventType getType() { return EventType.PERSONAL; }

    @Override
    public int getCooldownMinutes() { return 10; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) {
        try {
            Location center = target.getLocation();

            for (int i = 0; i < 10; i++) {
                double angle = (2 * Math.PI / 10) * i;
                Location spawnLoc = center.clone().add(Math.cos(angle) * 3, 0, Math.sin(angle) * 3);

                Chicken chicken = (Chicken) center.getWorld().spawnEntity(spawnLoc, EntityType.CHICKEN);
                chicken.setCustomName("§e📸 Photobomber");
                chicken.setCustomNameVisible(true);
            }

            MessageUtil.sendTitle(target,
                    "<color:#ffff00>📸 PHOTOBOMBED!</color>",
                    "<gray>Say cheese!</gray>",
                    10, 60, 20);

            target.playSound(target.getLocation(), Sound.ENTITY_CHICKEN_AMBIENT, 1.0f, 1.0f);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute() { }

    @Override
    public void cleanup() { }
}
