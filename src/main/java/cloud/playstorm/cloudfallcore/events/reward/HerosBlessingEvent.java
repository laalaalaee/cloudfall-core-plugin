package cloud.playstorm.cloudfallcore.events.reward;

import cloud.playstorm.cloudfallcore.engine.BaseEvent;
import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The whole server is blessed with heroic buffs for a while. Makes players feel
 * powerful and encourages them to go do something ambitious while it lasts.
 */
public class HerosBlessingEvent extends BaseEvent implements Listener {

    private boolean active = false;
    private final List<PotionEffectType> applied = new ArrayList<>();

    @Override public String getId() { return "reward.heros_blessing"; }
    @Override public String getDisplayName() { return "✨ Hero's Blessing"; }
    @Override public EventCategory getCategory() { return EventCategory.REWARD; }
    @Override public EventType getType() { return EventType.GLOBAL; }
    @Override public int getCooldownMinutes() { return 45; }
    @Override public int getDurationSeconds() { return 240; }
    @Override public int getWeight() { return 9; }

    @Override
    public void execute() {
        if (active) return;
        active = true;

        applied.clear();
        applied.addAll(resolveEffects());

        Bukkit.getPluginManager().registerEvents(this, plugin());
        for (Player p : Bukkit.getOnlinePlayers()) bless(p);

        int mins = Math.max(1, durationSeconds() / 60);
        MessageUtil.broadcastTitle(
                "<gradient:#56ccf2:#2f80ed>✨ Hero's Blessing</gradient>",
                "<gray>The server is empowered for " + mins + " minutes!</gray>",
                15, 80, 20);
        MessageUtil.broadcast("<aqua>✨ <b>Hero's Blessing</b> washes over the realm - go be legendary!</aqua>");
    }

    /** Parses config list like ["REGENERATION:0", "SPEED:0"]; falls back to a sensible default set. */
    private List<PotionEffectType> resolveEffects() {
        List<String> raw = settingList("effects");
        if (raw.isEmpty()) {
            raw = Arrays.asList("REGENERATION:0", "SPEED:0", "RESISTANCE:0", "LUCK:0");
        }
        List<PotionEffectType> result = new ArrayList<>();
        for (String entry : raw) {
            String name = entry.split(":")[0].trim().toUpperCase();
            PotionEffectType type = PotionEffectType.getByName(name);
            if (type != null) result.add(type);
        }
        return result;
    }

    private int amplifierFor(int index) {
        List<String> raw = settingList("effects");
        if (index < raw.size() && raw.get(index).contains(":")) {
            try { return Integer.parseInt(raw.get(index).split(":")[1].trim()); }
            catch (NumberFormatException ignored) { }
        }
        return 0;
    }

    private void bless(Player p) {
        for (int i = 0; i < applied.size(); i++) {
            p.addPotionEffect(new PotionEffect(applied.get(i),
                    durationSeconds() * 20, amplifierFor(i), false, false, true));
        }
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.4f);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (active) bless(event.getPlayer());
    }

    @Override
    public void cleanup() {
        if (!active) return;
        active = false;
        HandlerList.unregisterAll(this);
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (PotionEffectType type : applied) p.removePotionEffect(type);
        }
        applied.clear();
        MessageUtil.broadcast("<aqua>✨ The Hero's Blessing fades away...</aqua>");
    }
}
