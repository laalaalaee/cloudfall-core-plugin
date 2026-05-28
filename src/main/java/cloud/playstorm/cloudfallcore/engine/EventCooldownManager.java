package cloud.playstorm.cloudfallcore.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventCooldownManager {
    private final Map<UUID, Map<String, Long>> personalCooldowns = new HashMap<>();
    private final Map<String, Long> globalCooldowns = new HashMap<>();

    public void setPersonalCooldown(UUID playerId, String eventId, int minutes) {
        personalCooldowns.computeIfAbsent(playerId, k -> new HashMap<>())
                .put(eventId, System.currentTimeMillis() + (minutes * 60_000L));
    }

    public boolean isOnPersonalCooldown(UUID playerId, String eventId) {
        Map<String, Long> playerCooldowns = personalCooldowns.get(playerId);
        if (playerCooldowns != null && playerCooldowns.containsKey(eventId)) {
            if (System.currentTimeMillis() < playerCooldowns.get(eventId)) {
                return true;
            } else {
                playerCooldowns.remove(eventId);
            }
        }
        return false;
    }

    public void setGlobalCooldown(String eventId, int minutes) {
        globalCooldowns.put(eventId, System.currentTimeMillis() + (minutes * 60_000L));
    }

    public boolean isOnGlobalCooldown(String eventId) {
        if (globalCooldowns.containsKey(eventId)) {
            if (System.currentTimeMillis() < globalCooldowns.get(eventId)) {
                return true;
            } else {
                globalCooldowns.remove(eventId);
            }
        }
        return false;
    }
}
