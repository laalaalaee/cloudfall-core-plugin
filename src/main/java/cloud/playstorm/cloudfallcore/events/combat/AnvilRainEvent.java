package cloud.playstorm.cloudfallcore.events.combat;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Anvils rain from the sky around every player - dodge them! They never place a
 * block (no grief), but they hurt. Survive the storm to earn a reward.
 */
public class AnvilRainEvent extends BaseEvent implements Listener {

    private final Random random = new Random();
    private boolean active = false;
    private BukkitTask task;

    @Override public String getId() { return "combat.anvil_rain"; }
    @Override public String getDisplayName() { return "🔨 Anvil Rain"; }
    @Override public EventCategory getCategory() { return EventCategory.COMBAT; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 40; }
    @Override public int getDurationSeconds() { return 90; }
    @Override public int getWeight() { return 6; }

    @Override
    public void execute() {
        if (active) return;
        active = true;
        Bukkit.getPluginManager().registerEvents(this, plugin());

        MessageUtil.broadcastTitle("<gray>🔨 Anvil Rain!</gray>",
                "<gray>Look up... and DODGE!</gray>", 10, 60, 20);
        MessageUtil.broadcast("<gray>🔨 <b>Anvil Rain!</b> Stay on your toes and survive the storm!</gray>");

        task = Bukkit.getScheduler().runTaskTimer(plugin(), this::rain, 20L, 15L);
    }

    private void rain() {
        if (!active) return;
        int perPlayer = Math.max(1, settingInt("anvils-per-tick", 2));
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() != GameMode.SURVIVAL) continue;
            for (int i = 0; i < perPlayer; i++) {
                Location drop = p.getLocation().clone().add(
                        random.nextInt(9) - 4, 12, random.nextInt(9) - 4);
                FallingBlock anvil = p.getWorld().spawnFallingBlock(drop, Material.ANVIL.createBlockData());
                anvil.setHurtEntities(true);
                anvil.setDropItem(false);
            }
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 0.8f);
        }
    }

    @EventHandler
    public void onLand(EntityChangeBlockEvent event) {
        if (!active) return;
        if (event.getEntity() instanceof FallingBlock fb && fb.getBlockData().getMaterial() == Material.ANVIL) {
            // Never let a raining anvil actually place a block.
            event.setCancelled(true);
            fb.remove();
        }
    }

    @Override
    public void cleanup() {
        if (!active) return;
        active = false;
        if (task != null) { task.cancel(); task = null; }
        HandlerList.unregisterAll(this);

        double reward = settingDouble("reward-currency", 800);
        List<Player> survivors = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SURVIVAL) survivors.add(p);
        }
        for (Player p : survivors) {
            payout(p, reward);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
        MessageUtil.broadcast("<green>🔨 The Anvil Rain has passed! Survivors rewarded.</green>");
    }
}
