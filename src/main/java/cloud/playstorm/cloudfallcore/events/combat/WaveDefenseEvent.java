package cloud.playstorm.cloudfallcore.events.combat;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.GameMode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Waves of mobs spawn around every active player at a steady cadence. Survive to
 * the end and earn a reward. A multi-minute, adrenaline-fuelled challenge that
 * keeps everyone engaged the whole time.
 */
public class WaveDefenseEvent extends BaseEvent {

    private final Random random = new Random();
    private boolean active = false;
    private BukkitTask waveTask;
    private int wavesSpawned = 0;
    private int totalWaves;

    @Override public String getId() { return "combat.wave_defense"; }
    @Override public String getDisplayName() { return "🌊 Wave Defense"; }
    @Override public EventCategory getCategory() { return EventCategory.COMBAT; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 40; }
    @Override public int getDurationSeconds() { return 300; }
    @Override public int getWeight() { return 8; }

    @Override
    public void execute() {
        if (active) return;
        active = true;
        wavesSpawned = 0;
        totalWaves = Math.max(1, settingInt("waves", 5));

        long intervalTicks = Math.max(20L, (durationSeconds() * 20L) / totalWaves);

        MessageUtil.broadcastTitle("<dark_red>🌊 Wave Defense!</dark_red>",
                "<gray>Survive " + totalWaves + " waves of monsters!</gray>", 15, 80, 20);
        MessageUtil.broadcast("<red>🌊 <b>Wave Defense</b> has begun! Survive <yellow>" + totalWaves
                + "</yellow> waves to earn a reward!</red>");

        waveTask = Bukkit.getScheduler().runTaskTimer(plugin(), this::spawnWave, intervalTicks, intervalTicks);
    }

    private void spawnWave() {
        if (!active) return;
        wavesSpawned++;

        int perWave = Math.max(1, settingInt("mobs-per-wave", 4));
        // Each wave gets a little bigger.
        int count = perWave + (wavesSpawned - 1);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() != GameMode.SURVIVAL) continue;
            for (int i = 0; i < count; i++) {
                Location loc = p.getLocation().clone().add(
                        random.nextInt(11) - 5, 0, random.nextInt(11) - 5);
                loc = p.getWorld().getHighestBlockAt(loc).getLocation().add(0.5, 1, 0.5);
                p.getWorld().spawnEntity(loc, randomMob());
            }
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.4f);
            MessageUtil.send(p, "<red>🌊 Wave <yellow>" + wavesSpawned + "</yellow>/<yellow>"
                    + totalWaves + "</yellow> incoming!</red>");
        }

        if (wavesSpawned >= totalWaves && waveTask != null) {
            waveTask.cancel();
            waveTask = null;
        }
    }

    private EntityType randomMob() {
        List<String> names = settingList("mobs");
        if (names.isEmpty()) names = Arrays.asList("ZOMBIE", "SKELETON", "SPIDER");
        for (int attempts = 0; attempts < 4; attempts++) {
            String pick = names.get(random.nextInt(names.size()));
            try {
                return EntityType.valueOf(pick.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) { }
        }
        return EntityType.ZOMBIE;
    }

    @Override
    public void cleanup() {
        if (!active) return;
        active = false;
        if (waveTask != null) { waveTask.cancel(); waveTask = null; }

        double reward = settingDouble("reward-currency", 1200);
        List<Player> survivors = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SURVIVAL) survivors.add(p);
        }
        for (Player p : survivors) {
            payout(p, reward);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
        MessageUtil.broadcast("<green>🌊 The waves are over! <yellow>" + survivors.size()
                + "</yellow> survivors earned the reward.</green>");
    }
}
