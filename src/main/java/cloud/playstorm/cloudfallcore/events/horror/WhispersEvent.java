package cloud.playstorm.cloudfallcore.events.horror;

import cloud.playstorm.cloudfallcore.engine.EventCategory;
import cloud.playstorm.cloudfallcore.engine.EventType;
import cloud.playstorm.cloudfallcore.engine.ServerEvent;
import cloud.playstorm.cloudfallcore.util.MessageUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class WhispersEvent implements ServerEvent {

    @Override
    public String getId() { return "horror.whispers"; }

    @Override
    public String getDisplayName() { return "👻 Whispers"; }

    @Override
    public EventCategory getCategory() { return EventCategory.HORROR; }

    @Override
    public EventType getType() { return EventType.PERSONAL; }

    @Override
    public int getCooldownMinutes() { return 10; }

    @Override
    public boolean isEnabledByDefault() { return true; }

    @Override
    public void execute(Player target) {
        target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 0.5f, 0.5f);
        MessageUtil.send(target, "<color:#555555><i>...don't look behind you...</i></color>");
    }

    @Override
    public void execute() { }

    @Override
    public void cleanup() { }
}
