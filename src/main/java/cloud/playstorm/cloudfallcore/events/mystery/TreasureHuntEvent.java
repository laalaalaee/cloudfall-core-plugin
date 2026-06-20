package cloud.playstorm.cloudfallcore.events.mystery;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Buries a loot vault far out in the world and hands everyone a treasure map.
 * Players have to actually travel to claim it - a long, exploration-driven
 * objective that can take many minutes and rewards the first to arrive.
 */
public class TreasureHuntEvent extends BaseEvent implements Listener {

    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Random random = new Random();

    private boolean active = false;
    private boolean found = false;
    private Location chestLocation;

    @Override public String getId() { return "mystery.treasure_hunt"; }
    @Override public String getDisplayName() { return "🗺️ Treasure Hunt"; }
    @Override public EventCategory getCategory() { return EventCategory.MYSTERY; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 45; }
    @Override public int getDurationSeconds() { return 600; }
    @Override public int getWeight() { return 8; }

    @Override
    public void execute() {
        if (active) return;
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (online.isEmpty()) return;

        Player anchor = online.get(random.nextInt(online.size()));
        World world = anchor.getWorld();

        int min = settingInt("min-distance", 200);
        int max = Math.max(min + 50, settingInt("max-distance", 500));
        int distance = min + random.nextInt(max - min);
        double angle = random.nextDouble() * Math.PI * 2;

        int x = anchor.getLocation().getBlockX() + (int) (Math.cos(angle) * distance);
        int z = anchor.getLocation().getBlockZ() + (int) (Math.sin(angle) * distance);

        active = true;
        found = false;

        // Load the far-away chunk OFF the main thread, then place the vault.
        // Doing this synchronously would freeze the server (ping spike) while
        // distant terrain generates. The callback runs back on the main thread.
        world.getChunkAtAsync(x >> 4, z >> 4).thenAccept(chunk -> {
            Block surface = world.getHighestBlockAt(x, z);
            Block chestBlock = surface.getRelative(0, -2, 0);
            chestBlock.setType(Material.CHEST);
            chestLocation = chestBlock.getLocation();

            if (chestBlock.getState() instanceof Chest chest) {
                chest.getBlockInventory().addItem(new ItemStack(Material.DIAMOND, 8 + random.nextInt(8)));
                chest.getBlockInventory().addItem(new ItemStack(Material.NETHERITE_SCRAP, 1 + random.nextInt(2)));
                chest.getBlockInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 4));
                ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                book.addUnsafeEnchantment(Enchantment.MENDING, 1);
                chest.getBlockInventory().addItem(book);
                chest.update();
            }

            Bukkit.getPluginManager().registerEvents(this, plugin());
            for (Player p : Bukkit.getOnlinePlayers()) giveMap(p, chestLocation);

            MessageUtil.broadcastTitle("<gold>🗺️ Treasure Hunt!</gold>",
                    "<gray>A vault is buried far away - find it first!</gray>", 15, 90, 20);
            MessageUtil.broadcast("<gold>🗺️ <b>Treasure Hunt!</b> A vault is buried near <yellow>X:"
                    + chestLocation.getBlockX() + " Z:" + chestLocation.getBlockZ()
                    + "</yellow>. First to dig it up wins the loot" + currencyHint() + "!</gold>");
        });
    }

    private String currencyHint() {
        double reward = settingDouble("currency-reward", 0);
        if (reward > 0 && plugin().getEconomy().isEnabled()) {
            return " <gray>+</gray> <yellow>" + plugin().getEconomy().format(reward) + "</yellow>";
        }
        return "";
    }

    private void giveMap(Player p, Location loc) {
        ItemStack map = new ItemStack(Material.PAPER);
        ItemMeta meta = map.getItemMeta();
        meta.displayName(mm.deserialize("<gold>🗺️ Treasure Map</gold>").decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(mm.deserialize("<gray>The vault lies near:</gray>"));
        lore.add(mm.deserialize("<yellow>X: " + loc.getBlockX() + "  Z: " + loc.getBlockZ() + "</yellow>"));
        lore.add(mm.deserialize("<dark_gray>Dig down to claim it.</dark_gray>"));
        meta.lore(lore);
        map.setItemMeta(meta);
        p.getInventory().addItem(map);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!active || found || chestLocation == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;

        if (event.getClickedBlock().getLocation().equals(chestLocation)) {
            found = true;
            Player p = event.getPlayer();
            payout(p, settingDouble("currency-reward", 0));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            MessageUtil.broadcast("<gold>🗺️ <yellow>" + p.getName()
                    + "</yellow> found the buried treasure first!</gold>");
            HandlerList.unregisterAll(this);
        }
    }

    @Override
    public void cleanup() {
        if (!active) return;
        active = false;
        HandlerList.unregisterAll(this);
        if (!found && chestLocation != null) {
            Block block = chestLocation.getBlock();
            if (block.getType() == Material.CHEST) block.setType(Material.AIR);
            MessageUtil.broadcast("<gray>🗺️ The buried treasure was never found... it crumbles to dust.</gray>");
        }
        chestLocation = null;
    }
}
