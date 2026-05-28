package cloud.playstorm.cloudfallcore.events.weather;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;

public class CropBlessingEvent implements ServerEvent {

    @Override
    public String getId() { return "weather.crop_blessing"; }

    @Override
    public String getDisplayName() { return "🌾 Crop Blessing"; }

    @Override
    public EventCategory getCategory() { return EventCategory.REWARD; }

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
        // Broadcast title
        MessageUtil.broadcastTitle(
                "<color:#00cc00>🌾 Crop Blessing!</color>",
                "<gray>All nearby crops have been blessed!</gray>",
                20, 80, 20
        );
        MessageUtil.broadcast("<color:#00cc00>🌾 A divine blessing has matured all nearby crops!</color>");

        int totalCropsBlessed = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                // Play level up sound
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                Location playerLoc = player.getLocation();
                int px = playerLoc.getBlockX();
                int py = playerLoc.getBlockY();
                int pz = playerLoc.getBlockZ();

                // Scan 30-block radius around each player
                for (int x = -30; x <= 30; x++) {
                    for (int z = -30; z <= 30; z++) {
                        for (int y = -10; y <= 10; y++) {
                            try {
                                Block block = player.getWorld().getBlockAt(px + x, py + y, pz + z);
                                if (block.getBlockData() instanceof Ageable ageable) {
                                    if (ageable.getAge() < ageable.getMaximumAge()) {
                                        ageable.setAge(ageable.getMaximumAge());
                                        block.setBlockData(ageable);
                                        totalCropsBlessed++;
                                    }
                                }
                            } catch (Exception ignored) { }
                        }
                    }
                }
            } catch (Exception ignored) { }
        }

        if (totalCropsBlessed > 0) {
            MessageUtil.broadcast(String.format(
                    "<color:#00cc00>🌾 %d crops have been blessed and fully grown!</color>",
                    totalCropsBlessed
            ));
        }
    }

    @Override
    public void cleanup() {
        // Instant event - nothing to clean up
    }
}
