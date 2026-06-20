package cloud.playstorm.cloudfallcore.events.combat;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A server-wide kill quota with a live boss bar: slay enough monsters together
 * before the timer ends and every hunter who helped shares the reward.
 */
public class HuntQuotaEvent extends BaseEvent implements Listener {

    private final MiniMessage mm = MiniMessage.miniMessage();
    private boolean active = false;
    private boolean completed = false;
    private int goal;
    private int progress;
    private BossBar bar;
    private final Set<UUID> hunters = new HashSet<>();

    @Override public String getId() { return "combat.hunt_quota"; }
    @Override public String getDisplayName() { return "⚔️ Hunt Quota"; }
    @Override public EventCategory getCategory() { return EventCategory.COMBAT; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 40; }
    @Override public int getDurationSeconds() { return 300; }
    @Override public int getWeight() { return 8; }

    @Override
    public void execute() {
        if (active) return;
        active = true;
        completed = false;
        progress = 0;
        hunters.clear();
        goal = Math.max(1, settingInt("goal", 100));

        bar = BossBar.bossBar(barName(), 0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
        for (Player p : Bukkit.getOnlinePlayers()) p.showBossBar(bar);
        Bukkit.getPluginManager().registerEvents(this, plugin());

        int mins = Math.max(1, durationSeconds() / 60);
        MessageUtil.broadcastTitle("<red>⚔️ Hunt Quota!</red>",
                "<gray>Slay " + goal + " monsters in " + mins + " min!</gray>", 15, 80, 20);
        MessageUtil.broadcast("<red>⚔️ <b>Hunt Quota!</b> The server must slay <yellow>" + goal
                + "</yellow> monsters together. Every hunter who helps gets paid!</red>");
    }

    private net.kyori.adventure.text.Component barName() {
        return mm.deserialize("<red>⚔️ Hunt Quota: <yellow>" + progress + "</yellow> / <yellow>" + goal + "</yellow></red>");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (active && bar != null) event.getPlayer().showBossBar(bar);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!active || completed) return;
        if (!(event.getEntity() instanceof Monster)) return;
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        progress++;
        hunters.add(killer.getUniqueId());
        if (bar != null) {
            bar.name(barName());
            bar.progress(Math.min(1f, (float) progress / goal));
        }
        if (progress >= goal) complete();
    }

    private void complete() {
        completed = true;
        active = false;
        double reward = settingDouble("reward-currency", 1500);
        int diamonds = settingInt("reward-diamonds", 6);
        for (UUID id : hunters) {
            Player p = Bukkit.getPlayer(id);
            if (p == null || !p.isOnline()) continue;
            payout(p, reward);
            if (diamonds > 0) p.getInventory().addItem(new ItemStack(Material.DIAMOND, diamonds));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
        MessageUtil.broadcast("<green>⚔️ Hunt Quota complete! <yellow>" + hunters.size()
                + "</yellow> hunters shared the reward.</green>");
        end();
    }

    @Override
    public void cleanup() {
        if (!active && completed) { end(); return; }
        if (!active) return;
        active = false;
        if (!completed) {
            MessageUtil.broadcast("<gray>⚔️ Hunt Quota ended at <yellow>" + progress + "</yellow>/<yellow>"
                    + goal + "</yellow>. The monsters won this time.</gray>");
        }
        end();
    }

    private void end() {
        HandlerList.unregisterAll(this);
        if (bar != null) {
            for (Player p : Bukkit.getOnlinePlayers()) p.hideBossBar(bar);
            bar = null;
        }
    }
}
