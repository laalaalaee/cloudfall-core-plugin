package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A bounty is placed on one type of mob. Hunt it down during the window for a
 * cash reward per kill. Gives players a clear, focused goal.
 */
public class MobBountyEvent extends BaseEvent implements Listener {

    private final Random random = new Random();
    private boolean active = false;
    private EntityType bountyTarget;

    @Override public String getId() { return "reward.mob_bounty"; }
    @Override public String getDisplayName() { return "🎯 Mob Bounty"; }
    @Override public EventCategory getCategory() { return EventCategory.REWARD; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 35; }
    @Override public int getDurationSeconds() { return 300; }
    @Override public int getWeight() { return 9; }

    @Override
    public void execute() {
        if (active) return;
        bountyTarget = pickTarget();
        active = true;
        Bukkit.getPluginManager().registerEvents(this, plugin());

        String pretty = prettyName(bountyTarget);
        MessageUtil.broadcastTitle("<gold>🎯 Bounty: " + pretty + "</gold>",
                "<gray>Hunt them for a reward!</gray>", 15, 70, 20);
        MessageUtil.broadcast("<gold>🎯 <b>Mob Bounty!</b> Every <yellow>" + pretty
                + "</yellow> you kill pays out. Happy hunting!</gold>");
    }

    private EntityType pickTarget() {
        List<String> names = settingList("targets");
        if (names.isEmpty()) names = Arrays.asList("ZOMBIE", "SKELETON", "CREEPER", "SPIDER", "ENDERMAN");
        for (int i = 0; i < 5; i++) {
            try { return EntityType.valueOf(names.get(random.nextInt(names.size())).trim().toUpperCase()); }
            catch (IllegalArgumentException ignored) { }
        }
        return EntityType.ZOMBIE;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!active) return;
        if (event.getEntityType() != bountyTarget) return;
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        payout(killer, settingDouble("per-kill", 50));
    }

    @Override
    public void cleanup() {
        if (!active) return;
        active = false;
        HandlerList.unregisterAll(this);
        MessageUtil.broadcast("<gold>🎯 The bounty has expired.</gold>");
    }

    private String prettyName(EntityType type) {
        String s = type.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
