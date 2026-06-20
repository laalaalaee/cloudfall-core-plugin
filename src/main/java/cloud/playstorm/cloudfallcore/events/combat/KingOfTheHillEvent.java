package cloud.playstorm.cloudfallcore.events.combat;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * A control point appears. Stand on it - alone - to bank control time. First to
 * hold it long enough wins. A tense, competitive objective that pulls everyone
 * to one spot (great with 2+ players; solo players can still claim it).
 */
public class KingOfTheHillEvent extends BaseEvent {

    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Random random = new Random();

    private boolean active = false;
    private boolean completed = false;
    private Location point;
    private int goalSeconds;
    private double radius;
    private final Map<UUID, Integer> control = new HashMap<>();
    private BossBar bar;
    private BukkitTask task;

    @Override public String getId() { return "combat.king_of_the_hill"; }
    @Override public String getDisplayName() { return "👑 King of the Hill"; }
    @Override public EventCategory getCategory() { return EventCategory.COMBAT; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 45; }
    @Override public int getDurationSeconds() { return 300; }
    @Override public int getWeight() { return 7; }

    @Override
    public void execute() {
        if (active) return;
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (online.isEmpty()) return;

        Player anchor = online.get(random.nextInt(online.size()));
        point = anchor.getWorld().getHighestBlockAt(anchor.getLocation()).getLocation().add(0.5, 1, 0.5);
        goalSeconds = Math.max(10, settingInt("control-seconds", 45));
        radius = Math.max(2, settingInt("radius", 5));
        control.clear();
        active = true;
        completed = false;

        bar = BossBar.bossBar(mm.deserialize("<gold>👑 King of the Hill</gold>"), 0f,
                BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
        for (Player p : Bukkit.getOnlinePlayers()) p.showBossBar(bar);

        MessageUtil.broadcastTitle("<gold>👑 King of the Hill!</gold>",
                "<gray>X: " + (int) point.getX() + "  Z: " + (int) point.getZ() + "</gray>", 15, 80, 20);
        MessageUtil.broadcast("<gold>👑 <b>King of the Hill!</b> Hold the point at <yellow>X:"
                + (int) point.getX() + " Z:" + (int) point.getZ() + "</yellow> for <yellow>" + goalSeconds
                + "s</yellow> to win!</gold>");

        task = Bukkit.getScheduler().runTaskTimer(plugin(), this::tick, 20L, 20L);
    }

    private void tick() {
        if (!active || point == null) return;

        point.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, point.clone().add(0, 1, 0), 15, 0.6, 1, 0.6, 0);

        List<Player> onPoint = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld().equals(point.getWorld())
                    && p.getLocation().distanceSquared(point) <= radius * radius) {
                onPoint.add(p);
            }
        }

        // Only an uncontested holder banks time.
        if (onPoint.size() == 1) {
            Player holder = onPoint.get(0);
            int secs = control.merge(holder.getUniqueId(), 1, Integer::sum);
            holder.playSound(holder.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.4f, 1.6f);
            if (bar != null) {
                bar.name(mm.deserialize("<gold>👑 " + holder.getName() + ": <yellow>" + secs + "</yellow>/<yellow>"
                        + goalSeconds + "</yellow>s</gold>"));
                bar.progress(Math.min(1f, (float) secs / goalSeconds));
            }
            if (secs >= goalSeconds) win(holder);
        } else if (onPoint.size() > 1 && bar != null) {
            bar.name(mm.deserialize("<red>👑 Contested! (" + onPoint.size() + " fighters)</red>"));
        }
    }

    private void win(Player holder) {
        completed = true;
        active = false;
        payout(holder, settingDouble("reward-currency", 2000));
        holder.getInventory().addItem(new ItemStack(Material.DIAMOND, settingInt("reward-diamonds", 10)));
        holder.playSound(holder.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        MessageUtil.broadcastTitle("<gold>👑 " + holder.getName() + " is King!</gold>", "", 15, 70, 20);
        MessageUtil.broadcast("<gold>👑 <yellow>" + holder.getName() + "</yellow> held the hill and claimed the crown!</gold>");
        end();
    }

    @Override
    public void cleanup() {
        if (!active && completed) { end(); return; }
        if (!active) return;
        active = false;
        if (!completed) {
            MessageUtil.broadcast("<gray>👑 King of the Hill ended with no clear winner.</gray>");
        }
        end();
    }

    private void end() {
        if (task != null) { task.cancel(); task = null; }
        if (bar != null) {
            for (Player p : Bukkit.getOnlinePlayers()) p.hideBossBar(bar);
            bar = null;
        }
        point = null;
    }
}
