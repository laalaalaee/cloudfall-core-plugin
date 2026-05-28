package cloud.playstorm.cloudfallcore.events.combat;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BossSpawnEvent implements ServerEvent {

    private final Random random = new Random();

    @Override
    public String getId() { return "combat.boss_spawn"; }

    @Override
    public String getDisplayName() { return "⚔ World Boss"; }

    @Override
    public EventCategory getCategory() { return EventCategory.COMBAT; }

    @Override
    public EventType getType() { return EventType.GLOBAL; }

    @Override
    public int getCooldownMinutes() { return 30; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) { }

    @Override
    public void execute() {
        try {
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            if (players.isEmpty()) return;

            Player randomPlayer = players.get(random.nextInt(players.size()));
            Location playerLoc = randomPlayer.getLocation();

            // Spawn 15 blocks away in a random direction
            double angle = random.nextDouble() * 2 * Math.PI;
            Location spawnLoc = playerLoc.clone().add(Math.cos(angle) * 15, 0, Math.sin(angle) * 15);
            spawnLoc.setY(playerLoc.getWorld().getHighestBlockYAt(spawnLoc) + 1);

            WitherSkeleton boss = (WitherSkeleton) playerLoc.getWorld().spawnEntity(spawnLoc, EntityType.WITHER_SKELETON);

            // Set max health to 100 and heal to full
            boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
            boss.setHealth(100.0);

            // Equipment
            boss.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));

            // Name and visibility
            boss.setCustomName("§4§l⚔ World Boss");
            boss.setCustomNameVisible(true);

            // Glowing effect (infinite)
            boss.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));

            // Broadcast coordinates
            MessageUtil.broadcast("<color:#ff0000>⚔ A World Boss has spawned at X: " +
                    spawnLoc.getBlockX() + " Y: " + spawnLoc.getBlockY() + " Z: " +
                    spawnLoc.getBlockZ() + "! Defeat it for glory!</color>");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() { }
}
