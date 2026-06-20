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

    /**
     * How long this event stays "active" before the engine automatically calls
     * {@link #cleanup()}. Return 0 for instant events that need no teardown.
     * Server owners can override this per-event via {@code duration-seconds} in config.
     */
    default int getDurationSeconds() { return 0; }

    /**
     * Relative chance of this event being picked within its category.
     * Higher = more frequent. Owners can override via {@code weight} in config.
     */
    default int getWeight() { return 10; }
}
