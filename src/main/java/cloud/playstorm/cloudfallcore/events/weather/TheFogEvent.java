package cloud.playstorm.cloudfallcore.events.weather;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TheFogEvent implements ServerEvent {

    @Override
    public String getId() { return "weather.the_fog"; }

    @Override
    public String getDisplayName() { return "🌫️ The Fog"; }

    @Override
    public EventCategory getCategory() { return EventCategory.WEATHER; }

    @Override
    public EventType getType() { return EventType.GLOBAL; }

    @Override
    public int getCooldownMinutes() { return 15; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) { }

    @Override
    public void execute() {
        // Apply Blindness and Slowness to all online players for 3 minutes (3600 ticks)
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.BLINDNESS, 3600, 0, false, false, true
                ));
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOWNESS, 3600, 0, false, false, true
                ));
                player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 0.5f);
            } catch (Exception ignored) { }
        }

        // Broadcast title
        MessageUtil.broadcastTitle(
                "<color:#666666>🌫️ The Fog rolls in...</color>",
                "<gray>You can barely see...</gray>",
                20, 80, 20
        );
        MessageUtil.broadcast("<color:#888888>A thick fog has rolled in... visibility is near zero.</color>");
    }

    @Override
    public void cleanup() {
        // Remove Blindness and Slowness from all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.removePotionEffect(PotionEffectType.SLOWNESS);
            } catch (Exception ignored) { }
        }

        MessageUtil.broadcast("<color:#aaaaaa>🌫️ The fog has lifted. Visibility is restored.</color>");
    }
}
