package cloud.playstorm.cloudfallcore.events.mystery;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TheMerchantEvent implements ServerEvent {

    private final Random random = new Random();

    @Override
    public String getId() { return "mystery.the_merchant"; }

    @Override
    public String getDisplayName() { return "✦ The Wandering Merchant"; }

    @Override
    public EventCategory getCategory() { return EventCategory.MYSTERY; }

    @Override
    public EventType getType() { return EventType.GLOBAL; }

    @Override
    public int getCooldownMinutes() { return 20; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) { }

    @Override
    public void execute() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
        if (plugin == null) return;

        try {
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            if (players.isEmpty()) return;

            Player randomPlayer = players.get(random.nextInt(players.size()));
            Location playerLoc = randomPlayer.getLocation();

            // Spawn 10 blocks away in a random direction
            double angle = random.nextDouble() * 2 * Math.PI;
            Location spawnLoc = playerLoc.clone().add(Math.cos(angle) * 10, 0, Math.sin(angle) * 10);
            spawnLoc.setY(playerLoc.getWorld().getHighestBlockYAt(spawnLoc) + 1);

            Villager villager = (Villager) playerLoc.getWorld().spawnEntity(spawnLoc, EntityType.VILLAGER);
            villager.setProfession(Villager.Profession.WEAPONSMITH);
            villager.setCustomName("§6§l✦ The Wandering Merchant");
            villager.setCustomNameVisible(true);
            villager.setAI(false);
            villager.setInvulnerable(true);

            // Trade 1: 1 Emerald -> 5 Diamonds
            MerchantRecipe trade1 = new MerchantRecipe(new ItemStack(Material.DIAMOND, 5), 999);
            trade1.addIngredient(new ItemStack(Material.EMERALD, 1));

            // Trade 2: 1 Gold Ingot -> 3 Emeralds
            MerchantRecipe trade2 = new MerchantRecipe(new ItemStack(Material.EMERALD, 3), 999);
            trade2.addIngredient(new ItemStack(Material.GOLD_INGOT, 1));

            // Trade 3: 1 Iron Ingot -> 1 Golden Apple
            MerchantRecipe trade3 = new MerchantRecipe(new ItemStack(Material.GOLDEN_APPLE, 1), 999);
            trade3.addIngredient(new ItemStack(Material.IRON_INGOT, 1));

            List<MerchantRecipe> recipes = new ArrayList<>();
            recipes.add(trade1);
            recipes.add(trade2);
            recipes.add(trade3);
            villager.setRecipes(recipes);

            // Schedule removal after 5 minutes (6000 ticks)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (villager.isValid()) {
                    villager.remove();
                    MessageUtil.broadcast("<color:#ffaa00>✦ The Wandering Merchant has vanished into thin air...</color>");
                }
            }, 6000L);

            // Broadcast coordinates
            MessageUtil.broadcast("<color:#ffaa00>✦ A Wandering Merchant has appeared at X: " +
                    spawnLoc.getBlockX() + " Y: " + spawnLoc.getBlockY() + " Z: " +
                    spawnLoc.getBlockZ() + "! Hurry before they vanish!</color>");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() { }
}
