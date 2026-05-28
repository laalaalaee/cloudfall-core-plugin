package cloud.playstorm.cloudfallcore.engine;

import cloud.playstorm.cloudfallcore.CloudFallCore;
// Horror
import cloud.playstorm.cloudfallcore.events.horror.WhispersEvent;
import cloud.playstorm.cloudfallcore.events.horror.PhantomFootstepsEvent;
import cloud.playstorm.cloudfallcore.events.horror.TheScreamerEvent;
import cloud.playstorm.cloudfallcore.events.horror.StalkerNotificationEvent;
import cloud.playstorm.cloudfallcore.events.horror.HeartbeatEvent;
import cloud.playstorm.cloudfallcore.events.horror.FakePlayerJoinEvent;
import cloud.playstorm.cloudfallcore.events.horror.TheCountdownEvent;
import cloud.playstorm.cloudfallcore.events.horror.DoorSoundsEvent;
import cloud.playstorm.cloudfallcore.events.horror.CaveBreathingEvent;
import cloud.playstorm.cloudfallcore.events.horror.InventoryShuffleEvent;
// Reward
import cloud.playstorm.cloudfallcore.events.reward.LuckyPlayerEvent;
import cloud.playstorm.cloudfallcore.events.reward.SupplyDropEvent;
import cloud.playstorm.cloudfallcore.events.reward.XPRainEvent;
import cloud.playstorm.cloudfallcore.events.reward.FullHealEvent;
import cloud.playstorm.cloudfallcore.events.reward.TreasureMapEvent;
import cloud.playstorm.cloudfallcore.events.reward.FreeFoodEvent;
import cloud.playstorm.cloudfallcore.events.reward.BeaconBuffEvent;
// Weather
import cloud.playstorm.cloudfallcore.events.weather.AcidRainEvent;
import cloud.playstorm.cloudfallcore.events.weather.MeteorShowerEvent;
import cloud.playstorm.cloudfallcore.events.weather.SolarEclipseEvent;
import cloud.playstorm.cloudfallcore.events.weather.ThunderstormOfGodsEvent;
import cloud.playstorm.cloudfallcore.events.weather.FallingStarsEvent;
import cloud.playstorm.cloudfallcore.events.weather.CropBlessingEvent;
import cloud.playstorm.cloudfallcore.events.weather.OreVeinEvent;
import cloud.playstorm.cloudfallcore.events.weather.TheFogEvent;
// Combat
import cloud.playstorm.cloudfallcore.events.combat.ZombieSiegeEvent;
import cloud.playstorm.cloudfallcore.events.combat.BossSpawnEvent;
import cloud.playstorm.cloudfallcore.events.combat.CreeperSwarmEvent;
import cloud.playstorm.cloudfallcore.events.combat.PhantomFlockEvent;
import cloud.playstorm.cloudfallcore.events.combat.WitchCircleEvent;
// Social
import cloud.playstorm.cloudfallcore.events.social.ServerComplimentEvent;
import cloud.playstorm.cloudfallcore.events.social.FireworkShowEvent;
import cloud.playstorm.cloudfallcore.events.social.PhotoBombEvent;
import cloud.playstorm.cloudfallcore.events.social.ServerTriviaEvent;
import cloud.playstorm.cloudfallcore.events.social.DareSystemEvent;
// Mystery
import cloud.playstorm.cloudfallcore.events.mystery.TheMerchantEvent;
import cloud.playstorm.cloudfallcore.events.mystery.EnchantedCoordsEvent;
import cloud.playstorm.cloudfallcore.events.mystery.DimensionalRiftEvent;
import cloud.playstorm.cloudfallcore.events.mystery.BuriedTreasureEvent;
import cloud.playstorm.cloudfallcore.events.reward.GoldenHourEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventRegistry {

    private final CloudFallCore plugin;
    private final EventLogger logger;
    private final AFKTracker afkTracker;
    private final List<ServerEvent> events = new ArrayList<>();
    private final Random random = new Random();

    public EventRegistry(CloudFallCore plugin, EventLogger logger, AFKTracker afkTracker) {
        this.plugin = plugin;
        this.logger = logger;
        this.afkTracker = afkTracker;
        registerEvents();
    }

    private void registerEvents() {
        // Horror & Psychological (10)
        events.add(new WhispersEvent());
        events.add(new PhantomFootstepsEvent());
        events.add(new TheScreamerEvent());
        events.add(new StalkerNotificationEvent());
        events.add(new HeartbeatEvent());
        events.add(new FakePlayerJoinEvent());
        events.add(new TheCountdownEvent());
        events.add(new DoorSoundsEvent());
        events.add(new CaveBreathingEvent());
        events.add(new InventoryShuffleEvent());

        // Reward & Loot (7)
        events.add(new LuckyPlayerEvent());
        events.add(new SupplyDropEvent());
        events.add(new XPRainEvent());
        events.add(new FullHealEvent());
        events.add(new TreasureMapEvent());
        events.add(new FreeFoodEvent());
        events.add(new BeaconBuffEvent());
        events.add(new GoldenHourEvent());

        // Weather & World (8)
        events.add(new AcidRainEvent());
        events.add(new MeteorShowerEvent());
        events.add(new SolarEclipseEvent());
        events.add(new ThunderstormOfGodsEvent());
        events.add(new FallingStarsEvent());
        events.add(new CropBlessingEvent());
        events.add(new OreVeinEvent());
        events.add(new TheFogEvent());

        // Combat & Challenge (5)
        events.add(new ZombieSiegeEvent());
        events.add(new BossSpawnEvent());
        events.add(new CreeperSwarmEvent());
        events.add(new PhantomFlockEvent());
        events.add(new WitchCircleEvent());

        // Social & Fun (5)
        events.add(new ServerComplimentEvent());
        events.add(new FireworkShowEvent());
        events.add(new PhotoBombEvent());
        events.add(new ServerTriviaEvent());
        events.add(new DareSystemEvent());

        // Mystery & Exploration (4)
        events.add(new TheMerchantEvent());
        events.add(new EnchantedCoordsEvent());
        events.add(new DimensionalRiftEvent());
        events.add(new BuriedTreasureEvent());
    }

    public ServerEvent getRandomEvent() {
        if (events.isEmpty()) return null;
        return events.get(random.nextInt(events.size()));
    }

    public ServerEvent getRandomEventByCategory(EventCategory category) {
        List<ServerEvent> filtered = new ArrayList<>();
        for (ServerEvent event : events) {
            if (event.getCategory() == category && plugin.getConfig().getBoolean("events." + event.getId() + ".enabled", true)) {
                filtered.add(event);
            }
        }
        if (filtered.isEmpty()) return null;
        return filtered.get(random.nextInt(filtered.size()));
    }
}
