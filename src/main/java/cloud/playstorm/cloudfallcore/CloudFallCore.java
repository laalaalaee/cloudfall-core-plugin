package cloud.playstorm.cloudfallcore;

import cloud.playstorm.cloudfallcore.commands.BuyKitsCommand;
import cloud.playstorm.cloudfallcore.commands.BuyRankCommand;
import cloud.playstorm.cloudfallcore.commands.CloudFallCommand;
import cloud.playstorm.cloudfallcore.commands.SupportCommand;
import cloud.playstorm.cloudfallcore.engine.EventEngine;
import cloud.playstorm.cloudfallcore.tasks.AutoBroadcaster;
import cloud.playstorm.cloudfallcore.util.ConfigUpdater;
import cloud.playstorm.cloudfallcore.util.EconomyHook;
import cloud.playstorm.cloudfallcore.util.VersionChecker;
import org.bukkit.plugin.java.JavaPlugin;

public final class CloudFallCore extends JavaPlugin {

    private static CloudFallCore instance;

    private AutoBroadcaster broadcaster;
    private EventEngine eventEngine;
    private EconomyHook economy;

    public static CloudFallCore getInstance() {
        return instance;
    }

    public EventEngine getEventEngine() {
        return eventEngine;
    }

    public EconomyHook getEconomy() {
        return economy;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        ConfigUpdater.update(this);

        // Optional economy hook (Vault).
        economy = new EconomyHook(this);
        economy.setup();

        // Commands
        getCommand("support").setExecutor(new SupportCommand(this));
        getCommand("buyrank").setExecutor(new BuyRankCommand(this));
        getCommand("buykits").setExecutor(new BuyKitsCommand(this));
        CloudFallCommand admin = new CloudFallCommand(this);
        getCommand("cloudfall").setExecutor(admin);
        getCommand("cloudfall").setTabCompleter(admin);

        broadcaster = new AutoBroadcaster(this);
        broadcaster.start();

        eventEngine = new EventEngine(this);
        eventEngine.start();

        printBanner();

        // Non-blocking update check.
        if (getConfig().getBoolean("general.check-for-updates", true)) {
            VersionChecker.check(this);
        }
    }

    @Override
    public void onDisable() {
        if (broadcaster != null) {
            broadcaster.stop();
        }
        if (eventEngine != null) {
            eventEngine.stop();
        }
        getLogger().info("CloudFall Core has been disabled. See you soon!");
    }

    private void printBanner() {
        int eventCount = eventEngine.getRegistry().size();
        String version = getPluginMeta().getVersion();
        getLogger().info("==================================================");
        getLogger().info("   CLOUDFALL CORE  v" + version);
        getLogger().info("   Living World Engine: ACTIVE");
        getLogger().info("   Events Loaded: " + eventCount);
        getLogger().info("   Economy: " + (economy.isEnabled() ? "HOOKED (Vault)" : "disabled"));
        getLogger().info("   Author: laalaalaee");
        getLogger().info("   GitHub: https://github.com/laalaalaee/cloudfall-core-plugin");
        getLogger().info("==================================================");
    }
}
