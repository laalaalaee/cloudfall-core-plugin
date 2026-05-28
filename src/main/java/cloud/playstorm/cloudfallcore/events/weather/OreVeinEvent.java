package cloud.playstorm.cloudfallcore.events.weather;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class OreVeinEvent implements ServerEvent {

    @Override
    public String getId() { return "weather.ore_vein"; }

    @Override
    public String getDisplayName() { return "💎 Ore Vein Discovery"; }

    @Override
    public EventCategory getCategory() { return EventCategory.REWARD; }

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
        List<? extends Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) return;

        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Pick a random player
        Player chosen = players.get(random.nextInt(players.size()));
        World world = chosen.getWorld();

        // Generate random offset 50-100 blocks away
        int xOffset = random.nextInt(50, 101) * (random.nextBoolean() ? 1 : -1);
        int zOffset = random.nextInt(50, 101) * (random.nextBoolean() ? 1 : -1);

        int targetX = chosen.getLocation().getBlockX() + xOffset;
        int targetZ = chosen.getLocation().getBlockZ() + zOffset;
        int targetY = random.nextInt(5, 21); // y between 5 and 20

        // Place 8-15 DIAMOND_ORE blocks in a cluster - only replace STONE
        int oreCount = random.nextInt(8, 16);
        int placedCount = 0;

        for (int i = 0; i < oreCount; i++) {
            try {
                int ox = targetX + random.nextInt(-2, 3);
                int oy = targetY + random.nextInt(-2, 3);
                int oz = targetZ + random.nextInt(-2, 3);

                // Ensure Y is valid
                if (oy < 1 || oy > 255) continue;

                Block block = world.getBlockAt(ox, oy, oz);

                // Only replace STONE blocks
                if (block.getType() == Material.STONE) {
                    block.setType(Material.DIAMOND_ORE);
                    placedCount++;
                }
            } catch (Exception ignored) { }
        }

        // Broadcast announcement
        MessageUtil.broadcast(String.format(
                "<color:#00ffff>💎 An ancient ore vein has been discovered at X: %d, Z: %d!</color>",
                targetX, targetZ
        ));
        MessageUtil.broadcastTitle(
                "<color:#00ffff>💎 Ore Vein Discovered!</color>",
                "<gray>Rush to the coordinates!</gray>",
                20, 60, 20
        );

        // Play sound to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            } catch (Exception ignored) { }
        }
    }

    @Override
    public void cleanup() {
        // Instant event - nothing to clean up
    }
}
