package cloud.playstorm.cloudfallcore.events.horror;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TheCountdownEvent implements ServerEvent {

    private BukkitRunnable activeTask;

    @Override
    public String getId() { return "horror.the_countdown"; }

    @Override
    public String getDisplayName() { return "⏳ The Countdown"; }

    @Override
    public EventCategory getCategory() { return EventCategory.HORROR; }

    @Override
    public EventType getType() { return EventType.PERSONAL; }

    @Override
    public int getCooldownMinutes() { return 10; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CloudFallCore");
        if (plugin == null) return;

        activeTask = new BukkitRunnable() {
            int counter = 5;

            @Override
            public void run() {
                if (!target.isOnline()) {
                    this.cancel();
                    return;
                }

                try {
                    if (counter > 0) {
                        String color;
                        switch (counter) {
                            case 5: color = "#ffff00"; break;
                            case 4: color = "#ffaa00"; break;
                            case 3: color = "#ff7700"; break;
                            case 2: color = "#ff3300"; break;
                            case 1: color = "#ff0000"; break;
                            default: color = "#ffffff"; break;
                        }
                        MessageUtil.sendTitle(target,
                                "<color:" + color + "><bold>" + counter + "</bold></color>",
                                "", 0, 20, 5);
                    } else {
                        // The big reveal: absolutely nothing
                        MessageUtil.sendTitle(target,
                                "<color:#ff0000>...</color>",
                                "", 0, 40, 20);
                        this.cancel();
                    }
                } catch (Exception ignored) {}

                counter--;
            }
        };
        activeTask.runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public void execute() { }

    @Override
    public void cleanup() {
        if (activeTask != null && !activeTask.isCancelled()) {
            try {
                activeTask.cancel();
            } catch (Exception ignored) {}
        }
    }
}
