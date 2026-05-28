package cloud.playstorm.cloudfallcore.engine;

import cloud.playstorm.cloudfallcore.CloudFallCore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EventLogger {

    private final File logFile;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public EventLogger(CloudFallCore plugin) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.logFile = new File(plugin.getDataFolder(), "events.log");
    }

    public void log(String message) {
        String timestamp = dateFormat.format(new Date());
        String logLine = "[" + timestamp + "] " + message;
        
        try (PrintWriter out = new PrintWriter(new FileWriter(logFile, true))) {
            out.println(logLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
