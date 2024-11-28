package com.minecraftsolutions.vip.events;

import com.minecraftsolutions.vip.model.vip.Vip;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@AllArgsConstructor
public class PlayerVipChangedEvent extends Event {

    private final OfflinePlayer offlinePlayer;
    private final Vip vip;
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
