package cloud.playstorm.cloudfallcore.events.combat;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * A buffed, named boss spawns somewhere in the world with a health bar. Whoever
 * lands the killing blow earns big loot + currency. A long, focused fight that
 * gives the whole server a target to chase.
 */
public class WorldBossEvent extends BaseEvent implements Listener {

    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Random random = new Random();

    private boolean active = false;
    private UUID bossId;
    private LivingEntity boss;
    private double maxHealth;
    private BossBar bar;
    private BukkitTask tracker;

    @Override public String getId() { return "combat.world_boss"; }
    @Override public String getDisplayName() { return "👹 World Boss"; }
    @Override public EventCategory getCategory() { return EventCategory.COMBAT; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 45; }
    @Override public int getDurationSeconds() { return 420; }
    @Override public int getWeight() { return 8; }

    @Override
    public void execute() {
        if (active) return;
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (online.isEmpty()) return;

        Player anchor = online.get(random.nextInt(online.size()));
        World world = anchor.getWorld();

        int distance = settingInt("spawn-distance", 30);
        double angle = random.nextDouble() * Math.PI * 2;
        int x = anchor.getLocation().getBlockX() + (int) (Math.cos(angle) * distance);
        int z = anchor.getLocation().getBlockZ() + (int) (Math.sin(angle) * distance);
        Location loc = world.getHighestBlockAt(x, z).getLocation().add(0.5, 1, 0.5);

        EntityType type = resolveType();
        if (!(world.spawnEntity(loc, type) instanceof LivingEntity spawned)) {
            return;
        }

        boss = spawned;
        bossId = boss.getUniqueId();
        maxHealth = settingDouble("health", 300);

        var attr = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(maxHealth);
            boss.setHealth(maxHealth);
        } else {
            maxHealth = boss.getHealth();
        }

        String name = settingString("boss-name", "<red>The Devourer</red>");
        boss.customName(mm.deserialize(name));
        boss.setCustomNameVisible(true);
        boss.setRemoveWhenFarAway(false);
        boss.setGlowing(true);
        if (boss instanceof Mob mob && anchor != null) mob.setTarget(anchor);

        active = true;
        bar = BossBar.bossBar(mm.deserialize("👹 " + name), 1f, BossBar.Color.RED, BossBar.Overlay.NOTCHED_10);
        for (Player p : Bukkit.getOnlinePlayers()) p.showBossBar(bar);

        Bukkit.getPluginManager().registerEvents(this, plugin());
        startTracker();

        MessageUtil.broadcastTitle("<dark_red>👹 A World Boss has appeared!</dark_red>",
                "<gray>X: " + (int) loc.getX() + "  Z: " + (int) loc.getZ() + "</gray>", 15, 90, 20);
        MessageUtil.broadcast("<dark_red>👹 <b>" + stripTags(name) + "</b> has spawned near <yellow>X:"
                + (int) loc.getX() + " Z:" + (int) loc.getZ() + "</yellow>! Slay it for a huge reward!</dark_red>");
    }

    private EntityType resolveType() {
        try {
            return EntityType.valueOf(settingString("entity-type", "RAVAGER").toUpperCase());
        } catch (IllegalArgumentException e) {
            return EntityType.RAVAGER;
        }
    }

    private void startTracker() {
        tracker = Bukkit.getScheduler().runTaskTimer(plugin(), () -> {
            if (!active || boss == null || boss.isDead() || !boss.isValid()) return;
            float pct = (float) Math.max(0, Math.min(1, boss.getHealth() / maxHealth));
            if (bar != null) bar.progress(pct);
        }, 20L, 20L);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!active || bossId == null) return;
        if (!event.getEntity().getUniqueId().equals(bossId)) return;

        Player killer = event.getEntity().getKiller();
        Location at = event.getEntity().getLocation();

        double reward = settingDouble("reward-currency", 3000);
        if (killer != null) {
            payout(killer, reward);
            killer.getInventory().addItem(new ItemStack(Material.DIAMOND, settingInt("reward-diamonds", 12)));
            killer.getInventory().addItem(new ItemStack(Material.NETHERITE_INGOT, settingInt("reward-netherite", 1)));
            killer.playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            MessageUtil.broadcast("<gold>👹 <yellow>" + killer.getName()
                    + "</yellow> has slain the World Boss and claimed the reward!</gold>");
        } else {
            at.getWorld().dropItemNaturally(at, new ItemStack(Material.DIAMOND, settingInt("reward-diamonds", 12)));
            MessageUtil.broadcast("<gold>👹 The World Boss has fallen!</gold>");
        }
        active = false;
        endBar();
    }

    @Override
    public void cleanup() {
        if (!active) { endBar(); return; }
        active = false;
        if (boss != null && !boss.isDead() && boss.isValid()) {
            boss.remove();
            MessageUtil.broadcast("<dark_gray>👹 The World Boss vanished back into the shadows...</dark_gray>");
        }
        endBar();
    }

    private void endBar() {
        if (tracker != null) { tracker.cancel(); tracker = null; }
        HandlerList.unregisterAll(this);
        if (bar != null) {
            for (Player p : Bukkit.getOnlinePlayers()) p.hideBossBar(bar);
            bar = null;
        }
        boss = null;
        bossId = null;
    }

    private String stripTags(String s) {
        return s.replaceAll("<[^>]+>", "");
    }
}
