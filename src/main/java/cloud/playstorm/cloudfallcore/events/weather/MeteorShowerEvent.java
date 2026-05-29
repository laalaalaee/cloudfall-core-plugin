package cloud.playstorm.cloudfallcore.events.weather;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MeteorShowerEvent implements ServerEvent, Listener {

    private BukkitTask task;
    private boolean active = false;

    @Override
    public String getId() { return "weather.meteor_shower"; }

    @Override
    public String getDisplayName() { return "☄️ Meteor Shower"; }

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

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (active && event.getEntity() instanceof Fireball) {
            // Prevent meteor fireballs from breaking blocks
            event.blockList().clear();
        }
    }

    @Override
    public void execute() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
        if (plugin == null) return;

        List<? extends Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) return;

        active = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Pick a random player's location as the center
        Player chosen = players.get(ThreadLocalRandom.current().nextInt(players.size()));
        Location center = chosen.getLocation().clone();

        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();

        // Broadcast announcement
        MessageUtil.broadcast(String.format(
                "<color:#ff6600>☄️ A Meteor Shower has been spotted near X: %d, Z: %d!</color>",
                centerX, centerZ
        ));
        MessageUtil.broadcastTitle(
                "<color:#ff6600>☄️ Meteor Shower!</color>",
                "<gray>Take cover! (No block damage)</gray>",
                20, 60, 20
        );

        // Spawn 10 fireballs over 5 seconds (every 10 ticks)
        task = new BukkitRunnable() {
            private int count = 0;

            @Override
            public void run() {
                if (count >= 10) {
                    cancel();
                    // Schedule chest placement after 60 ticks
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        spawnLootChest(center);
                        cleanup();
                    }, 60L);
                    return;
                }

                try {
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    double offsetX = random.nextDouble(-15, 15);
                    double offsetZ = random.nextDouble(-15, 15);
                    Location spawnLoc = center.clone().add(offsetX, 40, offsetZ);

                    center.getWorld().spawn(spawnLoc, Fireball.class, fireball -> {
                        fireball.setDirection(new Vector(0, -1, 0));
                        fireball.setYield(3.0f);
                        fireball.setIsIncendiary(false);
                    });
                } catch (Exception ignored) { }

                count++;
            }
        }.runTaskTimer(plugin, 20L, 10L);
    }

    private void spawnLootChest(Location center) {
        try {
            // Place chest at the highest block at center
            int highestY = center.getWorld().getHighestBlockYAt(center) + 1;
            Location chestLoc = new Location(center.getWorld(), center.getBlockX(), highestY, center.getBlockZ());
            Block block = chestLoc.getBlock();
            block.setType(Material.CHEST);

            // Fill with loot
            if (block.getState() instanceof Chest chest) {
                Inventory inv = chest.getBlockInventory();
                ThreadLocalRandom random = ThreadLocalRandom.current();

                inv.addItem(new ItemStack(Material.DIAMOND, random.nextInt(3, 6)));
                inv.addItem(new ItemStack(Material.EMERALD, random.nextInt(5, 9)));
                inv.addItem(new ItemStack(Material.IRON_INGOT, random.nextInt(10, 16)));

                MessageUtil.broadcast(String.format(
                        "<color:#ff6600>☄️ A meteor has left a treasure chest at X: %d, Y: %d, Z: %d!</color>",
                        chestLoc.getBlockX(), chestLoc.getBlockY(), chestLoc.getBlockZ()
                ));
            }
        } catch (Exception ignored) { }
    }

    @Override
    public void cleanup() {
        active = false;
        HandlerList.unregisterAll(this);
        if (task != null && !task.isCancelled()) {
            task.cancel();
            task = null;
        }
    }
}
