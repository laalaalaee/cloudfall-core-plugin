package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A herd of animals wanders into the area near a player. Easy food and resources
 * - especially handy for small or newly-started servers.
 */
public class MigratingHerdEvent extends BaseEvent {

    private final Random random = new Random();

    @Override public String getId() { return "reward.migrating_herd"; }
    @Override public String getDisplayName() { return "🐄 Migrating Herd"; }
    @Override public EventCategory getCategory() { return EventCategory.REWARD; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 30; }
    @Override public int getWeight() { return 9; }

    @Override
    public void execute() {
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (online.isEmpty()) return;

        Player anchor = online.get(random.nextInt(online.size()));
        int count = settingInt("herd-size", 8);
        EntityType type = pickAnimal();

        for (int i = 0; i < count; i++) {
            Location loc = anchor.getLocation().clone().add(
                    random.nextInt(11) - 5, 0, random.nextInt(11) - 5);
            loc = anchor.getWorld().getHighestBlockAt(loc).getLocation().add(0.5, 1, 0.5);
            anchor.getWorld().spawnEntity(loc, type);
        }
        anchor.playSound(anchor.getLocation(), Sound.ENTITY_COW_AMBIENT, 1.0f, 1.0f);

        MessageUtil.broadcast("<green>🐄 A herd of <yellow>" + pretty(type)
                + "</yellow> has migrated near <yellow>" + anchor.getName() + "</yellow>!</green>");
    }

    private EntityType pickAnimal() {
        List<String> names = settingList("animals");
        if (names.isEmpty()) names = Arrays.asList("COW", "SHEEP", "PIG", "CHICKEN");
        for (int i = 0; i < 5; i++) {
            try { return EntityType.valueOf(names.get(random.nextInt(names.size())).trim().toUpperCase()); }
            catch (IllegalArgumentException ignored) { }
        }
        return EntityType.COW;
    }

    private String pretty(EntityType t) {
        String s = t.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
