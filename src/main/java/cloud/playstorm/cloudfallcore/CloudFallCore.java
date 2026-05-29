package cloud.playstorm.cloudfallcore;

import cloud.playstorm.cloudfallcore.commands.BuyKitsCommand;
import cloud.playstorm.cloudfallcore.commands.BuyRankCommand;
import cloud.playstorm.cloudfallcore.commands.SupportCommand;
import cloud.playstorm.cloudfallcore.engine.EventEngine;
import cloud.playstorm.cloudfallcore.tasks.AutoBroadcaster;
import org.bukkit.plugin.java.JavaPlugin;

public final class CloudFallCore extends JavaPlugin {

    private AutoBroadcaster broadcaster;
    private EventEngine eventEngine;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getCommand("support").setExecutor(new SupportCommand(this));
        getCommand("buyrank").setExecutor(new BuyRankCommand(this));
        getCommand("buykits").setExecutor(new BuyKitsCommand(this));

        broadcaster = new AutoBroadcaster(this);
        broadcaster.start();

        eventEngine = new EventEngine(this);
        eventEngine.start();

        getLogger().info("=========================================");
        getLogger().info("   ☁️  CLOUDFALL CORE  ☁️");
        getLogger().info(" Living World Engine: ACTIVE");
        getLogger().info(" Events Loaded: 35");
        getLogger().info(" Auto-Broadcaster: ACTIVE");
        getLogger().info(" Commands: LOADED");
        getLogger().info("=========================================");
    }

    @Override
    public void onDisable() {
        if (broadcaster != null) {
            broadcaster.stop();
        }
        if (eventEngine != null) {
            eventEngine.stop();
        }
        getLogger().info("CloudFallCore has been disabled!");
    }
}
