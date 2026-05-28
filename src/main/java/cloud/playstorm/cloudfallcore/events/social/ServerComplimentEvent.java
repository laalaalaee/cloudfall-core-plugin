package cloud.playstorm.cloudfallcore.events.social;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Random;

public class ServerComplimentEvent implements ServerEvent {

    private final Random random = new Random();

    private static final String[] COMPLIMENTS = {
            "is absolutely crushing it today!",
            "is the backbone of this server!",
            "has the best builds!",
            "is a legend in the making!",
            "just radiates good vibes!",
            "is everyone's favorite player!",
            "makes this server a better place!",
            "deserves a standing ovation!",
            "is on fire today!",
            "is the MVP of the day!",
            "has the best survival skills!",
            "is a true diamond in the rough!",
            "is an absolute treasure!",
            "just keeps getting better!",
            "is the heart and soul of this community!",
            "has the sickest base on the server!",
            "is a PvP god!",
            "always knows what to say!",
            "is the definition of awesome!",
            "makes everyone smile!",
            "is the reason we all log in!",
            "has been carrying this server all day!",
            "deserves all the diamonds in the world!"
    };

    @Override
    public String getId() { return "social.server_compliment"; }

    @Override
    public String getDisplayName() { return "🌟 Server Compliment"; }

    @Override
    public EventCategory getCategory() { return EventCategory.SOCIAL; }

    @Override
    public EventType getType() { return EventType.PERSONAL; }

    @Override
    public int getCooldownMinutes() { return 8; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) {
        String compliment = COMPLIMENTS[random.nextInt(COMPLIMENTS.length)];
        MessageUtil.broadcast("<color:#ffaa00>🌟 " + target.getName() + "</color> <color:#55ff55>" + compliment + "</color>");
        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    @Override
    public void execute() { }

    @Override
    public void cleanup() { }
}
