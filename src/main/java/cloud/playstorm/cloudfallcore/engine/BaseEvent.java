package cloud.playstorm.cloudfallcore.engine;

import cloud.playstorm.cloudfallcore.CloudFallCore;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Convenience base for new (2.0+) events. Gives every event easy, config-driven
 * access to its own {@code events.<id>.settings.*} block and the economy hook,
 * so server owners can tune every number without touching code.
 */
public abstract class BaseEvent implements ServerEvent {

    protected CloudFallCore plugin() {
        return CloudFallCore.getInstance();
    }

    protected FileConfiguration cfg() {
        return plugin().getConfig();
    }

    private String base() {
        return "events." + getId() + ".settings.";
    }

    protected int settingInt(String key, int def) {
        return cfg().getInt(base() + key, def);
    }

    protected double settingDouble(String key, double def) {
        return cfg().getDouble(base() + key, def);
    }

    protected boolean settingBool(String key, boolean def) {
        return cfg().getBoolean(base() + key, def);
    }

    protected String settingString(String key, String def) {
        return cfg().getString(base() + key, def);
    }

    protected List<String> settingList(String key) {
        return cfg().getStringList(base() + key);
    }

    /** Effective duration in seconds, honouring a config override of {@code duration-seconds}. */
    protected int durationSeconds() {
        return cfg().getInt("events." + getId() + ".duration-seconds", getDurationSeconds());
    }

    /** Pays the configured {@code currency-reward} (if any) and notifies the player. */
    protected void payout(Player player) {
        payout(player, settingDouble("currency-reward", 0));
    }

    protected void payout(Player player, double amount) {
        if (amount <= 0 || player == null) return;
        if (!plugin().getEconomy().isEnabled()) return;
        plugin().getEconomy().deposit(player, amount);
        MessageUtil.send(player, "<gradient:#f9d423:#ffd700>+ " + plugin().getEconomy().format(amount) + "</gradient> <gray>added to your balance.</gray>");
    }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player player) { execute(); }

    @Override
    public void cleanup() { }
}
