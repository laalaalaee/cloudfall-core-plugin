package cloud.playstorm.cloudfallcore.util;

import cloud.playstorm.cloudfallcore.CloudFallCore;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Optional Vault economy bridge. If Vault + an economy plugin (EssentialsX, etc.)
 * are present, events can grant currency. If not, every call is a safe no-op so
 * the plugin works fine on servers with no economy at all.
 */
public class EconomyHook {

    private final CloudFallCore plugin;
    private Economy economy;
    private boolean hooked = false;

    public EconomyHook(CloudFallCore plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("Vault not found - currency rewards are disabled.");
            return;
        }
        try {
            RegisteredServiceProvider<Economy> rsp =
                    plugin.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                plugin.getLogger().info("Vault found but no economy provider is registered - currency rewards disabled.");
                return;
            }
            economy = rsp.getProvider();
            hooked = economy != null;
            if (hooked) {
                plugin.getLogger().info("Hooked into Vault economy: " + economy.getName());
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to hook Vault economy: " + t.getMessage());
        }
    }

    /** True only if Vault is hooked AND economy is enabled in config. */
    public boolean isEnabled() {
        return hooked && plugin.getConfig().getBoolean("economy.enabled", true);
    }

    public void deposit(OfflinePlayer player, double amount) {
        if (!isEnabled() || amount <= 0 || player == null) return;
        try {
            economy.depositPlayer(player, amount);
        } catch (Throwable ignored) { }
    }

    public String format(double amount) {
        if (hooked) {
            try { return economy.format(amount); } catch (Throwable ignored) { }
        }
        String symbol = plugin.getConfig().getString("economy.currency-symbol", "$");
        return symbol + ((long) amount);
    }
}
