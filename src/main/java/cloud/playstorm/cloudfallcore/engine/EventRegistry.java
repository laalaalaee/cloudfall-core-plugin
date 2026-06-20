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
import cloud.playstorm.cloudfallcore.events.reward.GoldenHourEvent;
import cloud.playstorm.cloudfallcore.events.reward.BankOverflowEvent;
import cloud.playstorm.cloudfallcore.events.reward.GiftDropEvent;
import cloud.playstorm.cloudfallcore.events.reward.MiningFrenzyEvent;
import cloud.playstorm.cloudfallcore.events.reward.HerosBlessingEvent;
import cloud.playstorm.cloudfallcore.events.reward.GoldRushEvent;
import cloud.playstorm.cloudfallcore.events.reward.LotteryEvent;
import cloud.playstorm.cloudfallcore.events.reward.DoubleCoinsHourEvent;
import cloud.playstorm.cloudfallcore.events.reward.MobBountyEvent;
import cloud.playstorm.cloudfallcore.events.reward.FishingFrenzyEvent;
import cloud.playstorm.cloudfallcore.events.reward.MigratingHerdEvent;
import cloud.playstorm.cloudfallcore.events.reward.HarvestFestivalEvent;
// Weather
import cloud.playstorm.cloudfallcore.events.weather.AcidRainEvent;
import cloud.playstorm.cloudfallcore.events.weather.MeteorShowerEvent;
import cloud.playstorm.cloudfallcore.events.weather.SolarEclipseEvent;
import cloud.playstorm.cloudfallcore.events.weather.ThunderstormOfGodsEvent;
import cloud.playstorm.cloudfallcore.events.weather.FallingStarsEvent;
import cloud.playstorm.cloudfallcore.events.weather.CropBlessingEvent;
import cloud.playstorm.cloudfallcore.events.weather.OreVeinEvent;
import cloud.playstorm.cloudfallcore.events.weather.TheFogEvent;
import cloud.playstorm.cloudfallcore.events.weather.AuroraEvent;
// Combat
import cloud.playstorm.cloudfallcore.events.combat.ZombieSiegeEvent;
import cloud.playstorm.cloudfallcore.events.combat.BossSpawnEvent;
import cloud.playstorm.cloudfallcore.events.combat.CreeperSwarmEvent;
import cloud.playstorm.cloudfallcore.events.combat.PhantomFlockEvent;
import cloud.playstorm.cloudfallcore.events.combat.WitchCircleEvent;
import cloud.playstorm.cloudfallcore.events.combat.WorldBossEvent;
import cloud.playstorm.cloudfallcore.events.combat.WaveDefenseEvent;
import cloud.playstorm.cloudfallcore.events.combat.KingOfTheHillEvent;
import cloud.playstorm.cloudfallcore.events.combat.HuntQuotaEvent;
import cloud.playstorm.cloudfallcore.events.combat.AnvilRainEvent;
// Social
import cloud.playstorm.cloudfallcore.events.social.ServerComplimentEvent;
import cloud.playstorm.cloudfallcore.events.social.FireworkShowEvent;
import cloud.playstorm.cloudfallcore.events.social.PhotoBombEvent;
import cloud.playstorm.cloudfallcore.events.social.ServerTriviaEvent;
import cloud.playstorm.cloudfallcore.events.social.DareSystemEvent;
import cloud.playstorm.cloudfallcore.events.social.QuickMathEvent;
import cloud.playstorm.cloudfallcore.events.social.ReactionTestEvent;
import cloud.playstorm.cloudfallcore.events.social.UnscrambleEvent;
// Mystery
import cloud.playstorm.cloudfallcore.events.mystery.TheMerchantEvent;
import cloud.playstorm.cloudfallcore.events.mystery.EnchantedCoordsEvent;
import cloud.playstorm.cloudfallcore.events.mystery.DimensionalRiftEvent;
import cloud.playstorm.cloudfallcore.events.mystery.BuriedTreasureEvent;
import cloud.playstorm.cloudfallcore.events.mystery.TreasureHuntEvent;
import cloud.playstorm.cloudfallcore.events.mystery.LootMeteorEvent;
import cloud.playstorm.cloudfallcore.events.mystery.RelicHuntEvent;

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
        // Horror & Psychological
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

        // Reward & Loot
        events.add(new LuckyPlayerEvent());
        events.add(new SupplyDropEvent());
        events.add(new XPRainEvent());
        events.add(new FullHealEvent());
        events.add(new TreasureMapEvent());
        events.add(new FreeFoodEvent());
        events.add(new BeaconBuffEvent());
        events.add(new GoldenHourEvent());
        events.add(new BankOverflowEvent());      // 2.0
        events.add(new GiftDropEvent());          // 2.0
        events.add(new MiningFrenzyEvent());      // 2.0
        events.add(new HerosBlessingEvent());     // 2.0
        events.add(new GoldRushEvent());          // 2.0
        events.add(new LotteryEvent());           // 2.0
        events.add(new DoubleCoinsHourEvent());   // 2.0
        events.add(new MobBountyEvent());         // 2.0
        events.add(new FishingFrenzyEvent());     // 2.0
        events.add(new MigratingHerdEvent());     // 2.0
        events.add(new HarvestFestivalEvent());   // 2.0

        // Weather & World
        events.add(new AcidRainEvent());
        events.add(new MeteorShowerEvent());
        events.add(new SolarEclipseEvent());
        events.add(new ThunderstormOfGodsEvent());
        events.add(new FallingStarsEvent());
        events.add(new CropBlessingEvent());
        events.add(new OreVeinEvent());
        events.add(new TheFogEvent());
        events.add(new AuroraEvent());            // 2.0

        // Combat & Challenge
        events.add(new ZombieSiegeEvent());
        events.add(new BossSpawnEvent());
        events.add(new CreeperSwarmEvent());
        events.add(new PhantomFlockEvent());
        events.add(new WitchCircleEvent());
        events.add(new WorldBossEvent());         // 2.0
        events.add(new WaveDefenseEvent());       // 2.0
        events.add(new KingOfTheHillEvent());     // 2.0
        events.add(new HuntQuotaEvent());         // 2.0
        events.add(new AnvilRainEvent());         // 2.0

        // Social & Fun
        events.add(new ServerComplimentEvent());
        events.add(new FireworkShowEvent());
        events.add(new PhotoBombEvent());
        events.add(new ServerTriviaEvent());
        events.add(new DareSystemEvent());
        events.add(new QuickMathEvent());         // 2.0
        events.add(new ReactionTestEvent());      // 2.0
        events.add(new UnscrambleEvent());        // 2.0

        // Mystery & Exploration
        events.add(new TheMerchantEvent());
        events.add(new EnchantedCoordsEvent());
        events.add(new DimensionalRiftEvent());
        events.add(new BuriedTreasureEvent());
        events.add(new TreasureHuntEvent());      // 2.0
        events.add(new LootMeteorEvent());        // 2.0
        events.add(new RelicHuntEvent());         // 2.0
    }

    public int size() {
        return events.size();
    }

    public List<ServerEvent> getAll() {
        return new ArrayList<>(events);
    }

    public ServerEvent getById(String id) {
        for (ServerEvent event : events) {
            if (event.getId().equalsIgnoreCase(id)) return event;
        }
        return null;
    }

    public ServerEvent getRandomEvent() {
        if (events.isEmpty()) return null;
        return events.get(random.nextInt(events.size()));
    }

    private boolean isEnabled(ServerEvent event) {
        return plugin.getConfig().getBoolean("events." + event.getId() + ".enabled", event.isEnabledByDefault());
    }

    private int weightOf(ServerEvent event) {
        return Math.max(0, plugin.getConfig().getInt("events." + event.getId() + ".weight", event.getWeight()));
    }

    /** Picks an enabled event in the category using per-event weights. */
    public ServerEvent getWeightedEventByCategory(EventCategory category) {
        List<ServerEvent> pool = new ArrayList<>();
        int total = 0;
        for (ServerEvent event : events) {
            if (event.getCategory() == category && isEnabled(event)) {
                int w = weightOf(event);
                if (w <= 0) continue;
                pool.add(event);
                total += w;
            }
        }
        if (pool.isEmpty() || total <= 0) return null;

        int rand = random.nextInt(total);
        int current = 0;
        for (ServerEvent event : pool) {
            current += weightOf(event);
            if (rand < current) return event;
        }
        return pool.get(pool.size() - 1);
    }
}
