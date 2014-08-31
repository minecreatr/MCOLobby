package com.minecreatr.chatquestions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created on 8/29/2014
 */
public class TickEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public TickEvent(){
    }


    @Override
    public HandlerList getHandlers(){
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
