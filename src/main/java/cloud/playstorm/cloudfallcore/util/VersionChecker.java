package cloud.playstorm.cloudfallcore.util;

import cloud.playstorm.cloudfallcore.CloudFallCore;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Asynchronously checks the latest GitHub release and warns the console if the
 * running version is out of date. Never blocks startup; fails silently on errors.
 */
public class VersionChecker {

    private static final String RELEASES_API =
            "https://api.github.com/repos/laalaalaee/cloudfall-core-plugin/releases/latest";
    private static final String RELEASES_PAGE =
            "https://github.com/laalaalaee/cloudfall-core-plugin/releases";

    private static final Pattern TAG_PATTERN =
            Pattern.compile("\"tag_name\"\\s*:\\s*\"v?([^\"]+)\"");

    public static void check(CloudFallCore plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String current = plugin.getPluginMeta().getVersion();
            try {
                HttpURLConnection conn = (HttpURLConnection) URI.create(RELEASES_API).toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github+json");
                conn.setRequestProperty("User-Agent", "CloudFallCore");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() != 200) return;

                StringBuilder body = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) body.append(line);
                }

                Matcher matcher = TAG_PATTERN.matcher(body.toString());
                if (!matcher.find()) return;
                String latest = matcher.group(1).trim();

                if (!isOutdated(current, latest)) return;

                // Hop back to main thread just for clean logging.
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getLogger().warning("==================================================");
                    plugin.getLogger().warning(" A new version of CloudFall Core is available!");
                    plugin.getLogger().warning("   Current: " + current + "  ->  Latest: " + latest);
                    plugin.getLogger().warning("   Download: " + RELEASES_PAGE);
                    plugin.getLogger().warning("==================================================");
                });
            } catch (Exception ignored) {
                // Offline / rate-limited / no releases yet - stay quiet.
            }
        });
    }

    /** Simple semantic-ish comparison; treats any difference where latest &gt; current as outdated. */
    private static boolean isOutdated(String current, String latest) {
        if (current == null || latest == null) return false;
        if (current.equalsIgnoreCase(latest)) return false;

        int[] c = parse(current);
        int[] l = parse(latest);
        for (int i = 0; i < Math.max(c.length, l.length); i++) {
            int cv = i < c.length ? c[i] : 0;
            int lv = i < l.length ? l[i] : 0;
            if (lv > cv) return true;
            if (lv < cv) return false;
        }
        return false;
    }

    private static int[] parse(String version) {
        String[] parts = version.replaceAll("[^0-9.]", "").split("\\.");
        int[] nums = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try { nums[i] = parts[i].isEmpty() ? 0 : Integer.parseInt(parts[i]); }
            catch (NumberFormatException e) { nums[i] = 0; }
        }
        return nums;
    }
}
