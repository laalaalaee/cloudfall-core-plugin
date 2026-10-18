package cloud.playstorm.cloudfallcore.util;

import cloud.playstorm.cloudfallcore.CloudFallCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Adds keys that exist in the bundled default config.yml but are missing from the
 * server owner's config.yml. Existing values are never changed. A backup
 * (config.yml.bak) is written before the file is touched.
 */
public final class ConfigUpdater {

    private ConfigUpdater() {
    }

    public static void update(CloudFallCore plugin) {
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            return;
        }

        try (InputStream in = plugin.getResource("config.yml")) {
            if (in == null) {
                return;
            }
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
            YamlConfiguration current = YamlConfiguration.loadConfiguration(file);

            int added = 0;
            boolean backedUp = false;
            for (String key : defaults.getKeys(true)) {
                if (defaults.isConfigurationSection(key) || current.contains(key, true)) {
                    continue;
                }
                if (!backedUp) {
                    Files.copy(file.toPath(), new File(plugin.getDataFolder(), "config.yml.bak").toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    backedUp = true;
                }
                current.set(key, defaults.get(key));
                current.setComments(key, defaults.getComments(key));
                current.setInlineComments(key, defaults.getInlineComments(key));
                added++;
            }

            if (added > 0) {
                current.save(file);
                plugin.reloadConfig();
                plugin.getLogger().info("Config updated: added " + added
                        + " new option(s). Backup saved as config.yml.bak.");
            }
        } catch (IOException | RuntimeException e) {
            plugin.getLogger().warning("Could not update config.yml automatically: " + e.getMessage());
        }
    }
}
