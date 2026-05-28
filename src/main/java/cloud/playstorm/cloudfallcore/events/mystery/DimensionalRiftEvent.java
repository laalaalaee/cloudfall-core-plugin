package cloud.playstorm.cloudfallcore.events.mystery;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DimensionalRiftEvent implements ServerEvent, Listener {

    private boolean active = false;
    private Location riftLocation;
    private BukkitTask particleTask;
    private BukkitTask timeoutTask;
    private final Random random = new Random();

    @Override
    public String getId() { return "dimensional_rift"; }
    @Override
    public String getDisplayName() { return "Dimensional Rift"; }
    @Override
    public EventCategory getCategory() { return EventCategory.MYSTERY; }
    @Override
    public EventType getType() { return EventType.GLOBAL; }
    @Override
    public int getCooldownMinutes() { return 30; }
    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player player) { execute(); }

    @Override
    public void execute() {
        if (active) return;
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) return;

        Player target = players.get(random.nextInt(players.size()));
        Location base = target.getLocation();
        
        int offsetX = random.nextInt(40) - 20;
        int offsetZ = random.nextInt(40) - 20;
        riftLocation = base.getWorld().getHighestBlockAt(base.getBlockX() + offsetX, base.getBlockZ() + offsetZ).getLocation().add(0, 1, 0);

        // Spawn obsidian base
        riftLocation.clone().add(0, -1, 0).getBlock().setType(Material.OBSIDIAN);

        active = true;
        MessageUtil.broadcast("<light_purple><b>[Rift]</b></light_purple> <gray>A dimensional rift has opened at X:" + riftLocation.getBlockX() + " Z:" + riftLocation.getBlockZ() + "!</gray>");

        Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
        if (plugin != null) {
            Bukkit.getPluginManager().registerEvents(this, plugin);

            particleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (active && riftLocation != null) {
                    riftLocation.getWorld().spawnParticle(Particle.PORTAL, riftLocation.clone().add(0.5, 1, 0.5), 50, 0.5, 1, 0.5, 0.1);
                }
            }, 0L, 10L);

            timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (active) {
                    MessageUtil.broadcast("<light_purple><b>[Rift]</b></light_purple> <gray>The dimensional rift has closed.</gray>");
                    cleanup();
                }
            }, 20 * 60 * 3L); // 3 minutes
        }
    }

    @Override
    public void cleanup() {
        active = false;
        if (particleTask != null) particleTask.cancel();
        if (timeoutTask != null) timeoutTask.cancel();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!active || riftLocation == null) return;

        Player p = event.getPlayer();
        if (p.getWorld().equals(riftLocation.getWorld()) && p.getLocation().distanceSquared(riftLocation) < 4.0) {
            active = false;
            MessageUtil.broadcast("<light_purple><b>[Rift]</b></light_purple> <green>" + p.getName() + " entered the rift!</green>");
            
            Location safeSky = riftLocation.clone();
            safeSky.setY(200);
            
            // Generate a glass platform
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    safeSky.clone().add(x, -1, z).getBlock().setType(Material.GLASS);
                }
            }
            safeSky.clone().add(0, 0, 0).getBlock().setType(Material.CHEST);
            Block block = safeSky.clone().add(0, 0, 0).getBlock();
            if (block.getState() instanceof Chest) {
                Chest chest = (Chest) block.getState();
                chest.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.DIAMOND, 5));
                chest.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.GOLD_INGOT, 10));
            }
            
            p.teleport(safeSky.clone().add(0.5, 0, 0.5));
            cleanup();
        }
    }
}
