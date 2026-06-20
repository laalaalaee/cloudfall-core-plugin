package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A server-wide objective: mine X ores together before the timer runs out.
 * Everyone who contributes shares the reward. The boss bar gives a constant,
 * visible goal that keeps people online and working together.
 */
public class GoldRushEvent extends BaseEvent implements Listener {

    private final MiniMessage mm = MiniMessage.miniMessage();

    private boolean active = false;
    private boolean completed = false;
    private int goal;
    private int progress;
    private BossBar bar;
    private final Set<UUID> contributors = new HashSet<>();

    private static final Set<Material> TARGETS = EnumSet.of(
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.NETHER_GOLD_ORE,
            Material.RAW_GOLD_BLOCK, Material.GOLD_BLOCK
    );

    @Override public String getId() { return "reward.gold_rush"; }
    @Override public String getDisplayName() { return "🏆 Gold Rush"; }
    @Override public EventCategory getCategory() { return EventCategory.REWARD; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 50; }
    @Override public int getDurationSeconds() { return 360; }
    @Override public int getWeight() { return 7; }

    @Override
    public void execute() {
        if (active) return;
        active = true;
        completed = false;
        progress = 0;
        contributors.clear();
        goal = Math.max(1, settingInt("goal", 150));

        bar = BossBar.bossBar(barName(), 0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
        for (Player p : Bukkit.getOnlinePlayers()) p.showBossBar(bar);

        Bukkit.getPluginManager().registerEvents(this, plugin());

        int mins = Math.max(1, durationSeconds() / 60);
        MessageUtil.broadcastTitle(
                "<gradient:#f7971e:#ffd200>🏆 Gold Rush!</gradient>",
                "<gray>Mine " + goal + " gold as a server in " + mins + " min!</gray>",
                15, 80, 20);
        MessageUtil.broadcast("<gold>🏆 <b>Gold Rush!</b> The server must mine <yellow>" + goal
                + "</yellow> gold ore/blocks in <yellow>" + mins + " minutes</yellow>. Everyone who helps gets paid!</gold>");
    }

    private net.kyori.adventure.text.Component barName() {
        return mm.deserialize("<gold>🏆 Gold Rush: <yellow>" + progress + "</yellow> / <yellow>" + goal + "</yellow></gold>");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (active && bar != null) event.getPlayer().showBossBar(bar);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!active || completed) return;
        if (!TARGETS.contains(event.getBlock().getType())) return;

        progress++;
        contributors.add(event.getPlayer().getUniqueId());

        if (bar != null) {
            bar.name(barName());
            bar.progress(Math.min(1f, (float) progress / goal));
        }

        if (progress >= goal) complete();
    }

    private void complete() {
        completed = true;
        double reward = settingDouble("reward-currency", 1500);
        int diamonds = settingInt("reward-diamonds", 8);

        MessageUtil.broadcastTitle("<gradient:#11998e:#38ef7d>🏆 Gold Rush Complete!</gradient>",
                "<gray>The server hit the goal!</gray>", 15, 90, 20);

        for (UUID id : contributors) {
            Player p = Bukkit.getPlayer(id);
            if (p == null || !p.isOnline()) continue;
            payout(p, reward);
            if (diamonds > 0) p.getInventory().addItem(new ItemStack(Material.DIAMOND, diamonds));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
        MessageUtil.broadcast("<green>🏆 Gold Rush complete! <yellow>" + contributors.size()
                + "</yellow> miners shared the reward.</green>");
        endBar();
        active = false;
    }

    @Override
    public void cleanup() {
        if (!active && completed) { endBar(); return; }
        if (!active) return;
        active = false;
        if (!completed) {
            MessageUtil.broadcast("<gray>🏆 Gold Rush ended - the server mined <yellow>" + progress
                    + "</yellow>/<yellow>" + goal + "</yellow>. So close!</gray>");
        }
        endBar();
    }

    private void endBar() {
        HandlerList.unregisterAll(this);
        if (bar != null) {
            for (Player p : Bukkit.getOnlinePlayers()) p.hideBossBar(bar);
            bar = null;
        }
    }
}
