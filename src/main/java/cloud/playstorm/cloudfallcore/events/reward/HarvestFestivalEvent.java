package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;

/**
 * Instantly matures crops around every player. Farmers feel like wizards, and
 * it's a generous, friendly event that never disrupts anyone.
 */
public class HarvestFestivalEvent extends BaseEvent {

    @Override public String getId() { return "reward.harvest_festival"; }
    @Override public String getDisplayName() { return "🌾 Harvest Festival"; }
    @Override public EventCategory getCategory() { return EventCategory.REWARD; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 30; }
    @Override public int getWeight() { return 9; }

    @Override
    public void execute() {
        int radius = settingInt("radius", 8);
        int grown = 0;

        for (Player p : Bukkit.getOnlinePlayers()) {
            Location center = p.getLocation();
            // Crops sit on a flat plane, so a thin vertical band (feet ±2) is
            // enough — this keeps the scan small instead of a big 3D box.
            for (int x = -radius; x <= radius; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block block = center.clone().add(x, y, z).getBlock();
                        if (block.getBlockData() instanceof Ageable ageable
                                && ageable.getAge() < ageable.getMaximumAge()) {
                            ageable.setAge(ageable.getMaximumAge());
                            block.setBlockData(ageable);
                            grown++;
                        }
                    }
                }
            }
            p.playSound(p.getLocation(), Sound.BLOCK_GRASS_BREAK, 1.0f, 1.2f);
        }

        MessageUtil.broadcastTitle("<green>🌾 Harvest Festival!</green>",
                "<gray>The crops spring to life!</gray>", 15, 60, 20);
        MessageUtil.broadcast("<green>🌾 <b>Harvest Festival!</b> Crops everywhere have instantly ripened ("
                + grown + " grown)!</green>");
    }
}
