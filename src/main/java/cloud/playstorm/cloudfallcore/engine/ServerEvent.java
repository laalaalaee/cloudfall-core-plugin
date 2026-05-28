package cloud.playstorm.cloudfallcore.engine;

import org.bukkit.entity.Player;

public interface ServerEvent {
    String getId();
    String getDisplayName();
    EventCategory getCategory();
    EventType getType();
    int getCooldownMinutes();
    boolean isEnabledByDefault();
    void execute(Player player);
    void execute();
    void cleanup();
}
