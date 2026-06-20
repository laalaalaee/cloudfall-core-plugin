package cloud.playstorm.cloudfallcore.commands;

import cloud.playstorm.cloudfallcore.CloudFallCore;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CloudFallCommand implements CommandExecutor, TabCompleter {

    private final CloudFallCore plugin;

    public CloudFallCommand(CloudFallCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("cloudfall.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            help(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage("§aCloudFall Core config reloaded.");
            }
            case "version" -> sender.sendMessage("§eCloudFall Core §fv" + plugin.getPluginMeta().getVersion()
                    + " §7| by laalaalaee");
            case "list" -> {
                List<ServerEvent> events = plugin.getEventEngine().getRegistry().getAll();
                sender.sendMessage("§e§lCloudFall Events (" + events.size() + "):");
                for (ServerEvent e : events) {
                    boolean enabled = plugin.getConfig().getBoolean("events." + e.getId() + ".enabled", e.isEnabledByDefault());
                    sender.sendMessage((enabled ? "§a✔ " : "§c✘ ") + "§7" + e.getId()
                            + " §8(" + e.getCategory().name().toLowerCase() + ")");
                }
            }
            case "trigger" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /cloudfall trigger <event-id>");
                    return true;
                }
                boolean fired = plugin.getEventEngine().trigger(args[1]);
                if (fired) {
                    sender.sendMessage("§aTriggered event: §f" + args[1]);
                } else {
                    sender.sendMessage("§cUnknown event id: §f" + args[1] + "§7 (use /cloudfall list)");
                }
            }
            default -> help(sender);
        }
        return true;
    }

    private void help(CommandSender sender) {
        sender.sendMessage("§e§l=== CloudFall Core ===");
        sender.sendMessage("§6/cloudfall reload §7- reload config.yml");
        sender.sendMessage("§6/cloudfall list §7- list all events + status");
        sender.sendMessage("§6/cloudfall trigger <id> §7- fire an event now");
        sender.sendMessage("§6/cloudfall version §7- show version info");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("cloudfall.admin")) return Collections.emptyList();

        if (args.length == 1) {
            List<String> subs = new ArrayList<>(List.of("reload", "list", "trigger", "version"));
            subs.removeIf(s -> !s.startsWith(args[0].toLowerCase()));
            return subs;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("trigger")) {
            List<String> ids = new ArrayList<>();
            for (ServerEvent e : plugin.getEventEngine().getRegistry().getAll()) {
                if (e.getId().startsWith(args[1].toLowerCase())) ids.add(e.getId());
            }
            return ids;
        }
        return Collections.emptyList();
    }
}
