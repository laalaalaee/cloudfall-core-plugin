package cloud.playstorm.cloudfallcore.events.weather;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SolarEclipseEvent implements ServerEvent {

    @Override
    public String getId() { return "weather.solar_eclipse"; }

    @Override
    public String getDisplayName() { return "☀️ Solar Eclipse"; }

    @Override
    public EventCategory getCategory() { return EventCategory.WEATHER; }

    @Override
    public EventType getType() { return EventType.GLOBAL; }

    @Override
    public int getCooldownMinutes() { return 10; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) { }

    @Override
    public void execute() {
        // Set all worlds to midnight
        for (World world : Bukkit.getWorlds()) {
            world.setTime(18000);
        }

        // Apply Darkness to all online players for 5 minutes (6000 ticks)
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.DARKNESS, 6000, 0, false, false, true
                ));
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.5f, 1.0f);
            } catch (Exception ignored) { }
        }

        // Broadcast title
        MessageUtil.broadcastTitle(
                "<color:#4a0080>☀️ Solar Eclipse!</color>",
                "<gray>The sky grows dark...</gray>",
                20, 80, 20
        );
        MessageUtil.broadcast("<color:#4a0080>☀️ A Solar Eclipse has darkened the skies!</color>");
    }

    @Override
    public void cleanup() {
        // Set all worlds back to day
        for (World world : Bukkit.getWorlds()) {
            world.setTime(6000);
        }

        // Remove Darkness from all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.removePotionEffect(PotionEffectType.DARKNESS);
        }

        MessageUtil.broadcast("<color:#4a0080>☀️ The Solar Eclipse has ended. Light returns!</color>");
    }
}
