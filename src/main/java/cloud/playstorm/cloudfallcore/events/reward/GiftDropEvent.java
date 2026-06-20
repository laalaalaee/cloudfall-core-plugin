package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Every online player instantly receives a random useful gift. No race, no
 * losers - great for small servers where "first to reach it" feels lonely.
 */
public class GiftDropEvent extends BaseEvent {

    private final Random random = new Random();

    // material -> max stack size for the gift roll
    private final Map<Material, Integer> defaultGifts = new HashMap<>() {{
        put(Material.DIAMOND, 5);
        put(Material.EMERALD, 8);
        put(Material.GOLDEN_APPLE, 3);
        put(Material.EXPERIENCE_BOTTLE, 16);
        put(Material.IRON_BLOCK, 3);
        put(Material.ENDER_PEARL, 4);
        put(Material.GOLD_INGOT, 12);
        put(Material.NETHERITE_SCRAP, 1);
    }};

    @Override public String getId() { return "reward.gift_drop"; }
    @Override public String getDisplayName() { return "🎁 Gift Drop"; }
    @Override public EventCategory getCategory() { return EventCategory.REWARD; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 25; }
    @Override public int getWeight() { return 12; }

    @Override
    public void execute() {
        Material[] pool = defaultGifts.keySet().toArray(new Material[0]);

        MessageUtil.broadcastTitle(
                "<gradient:#ff6ec4:#7873f5>🎁 Gift Drop!</gradient>",
                "<gray>A little something for everyone...</gray>",
                15, 60, 20);

        for (Player p : Bukkit.getOnlinePlayers()) {
            Material mat = pool[random.nextInt(pool.length)];
            int max = defaultGifts.get(mat);
            int amount = 1 + random.nextInt(Math.max(1, max));
            ItemStack gift = new ItemStack(mat, amount);

            Map<Integer, ItemStack> leftover = p.getInventory().addItem(gift);
            // Drop anything that didn't fit at the player's feet.
            leftover.values().forEach(stack -> p.getWorld().dropItemNaturally(p.getLocation(), stack));

            p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
            MessageUtil.send(p, "<light_purple>🎁 You received <white>" + amount + "x "
                    + prettyName(mat) + "</white>!</light_purple>");
            payout(p); // optional currency-reward on top
        }
    }

    private String prettyName(Material mat) {
        String s = mat.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
