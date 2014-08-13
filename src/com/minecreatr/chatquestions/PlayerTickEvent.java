package com.minecreatr.chatquestions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created on 8/11/2014
 */
public class PlayerTickEvent extends Event{

    private static final HandlerList handlers = new HandlerList();
    private Player player;

    public PlayerTickEvent(Player player){
        this.player=player;
    }
    public Player getPlayer(){
        return this.player;
    }


    @Override
    public HandlerList getHandlers(){
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
