package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BeaconBuffEvent implements ServerEvent {

    private static final int DURATION_TICKS = 3600; // 3 minutes

    private static final PotionEffect[] BEACON_EFFECTS = {
            new PotionEffect(PotionEffectType.SPEED, DURATION_TICKS, 0, true, true, true),
            new PotionEffect(PotionEffectType.HASTE, DURATION_TICKS, 0, true, true, true),
            new PotionEffect(PotionEffectType.RESISTANCE, DURATION_TICKS, 0, true, true, true),
            new PotionEffect(PotionEffectType.JUMP_BOOST, DURATION_TICKS, 0, true, true, true),
            new PotionEffect(PotionEffectType.STRENGTH, DURATION_TICKS, 0, true, true, true),
            new PotionEffect(PotionEffectType.REGENERATION, DURATION_TICKS, 0, true, true, true)
    };

    @Override
    public String getId() { return "reward.beacon_buff"; }

    @Override
    public String getDisplayName() { return "🔷 Beacon Blessing"; }

    @Override
    public EventCategory getCategory() { return EventCategory.REWARD; }

    @Override
    public EventType getType() { return EventType.GLOBAL; }

    @Override
    public int getCooldownMinutes() { return 15; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) {
        // GLOBAL event — delegate to execute()
        execute();
    }

    @Override
    public void execute() {
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (PotionEffect effect : BEACON_EFFECTS) {
                    player.addPotionEffect(effect);
                }
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
            }

            MessageUtil.broadcastTitle(
                    "<color:#55ffff>🔷 Beacon Blessing!</color>",
                    "<gray>All beacon effects for 3 minutes!</gray>",
                    10, 60, 20
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() {
        // Effects expire naturally after 3 minutes
    }
}
