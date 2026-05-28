package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class FullHealEvent implements ServerEvent {

    private static final PotionEffectType[] NEGATIVE_EFFECTS = {
            PotionEffectType.POISON,
            PotionEffectType.WITHER,
            PotionEffectType.BLINDNESS,
            PotionEffectType.HUNGER,
            PotionEffectType.WEAKNESS,
            PotionEffectType.SLOWNESS,
            PotionEffectType.MINING_FATIGUE
    };

    @Override
    public String getId() { return "reward.full_heal"; }

    @Override
    public String getDisplayName() { return "❤ Full Heal"; }

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
                player.setHealth(20.0);
                player.setFoodLevel(20);
                player.setSaturation(20.0f);

                for (PotionEffectType effect : NEGATIVE_EFFECTS) {
                    player.removePotionEffect(effect);
                }

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }

            MessageUtil.broadcastTitle(
                    "<color:#ff5555>❤ The Gods Show Mercy</color>",
                    "<gray>All players healed and fed!</gray>",
                    10, 60, 20
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() {
        // Nothing to clean up
    }
}
