package com.minecreatr.chatquestions.listeners;

import com.minecreatr.chatquestions.ChatQuestions;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleFlightEvent;

/**
 * Created on 8/11/2014
 */
public class ToggleFlightListener {

    public void onToggle(PlayerToggleFlightEvent event){
        if (event.getPlayer().getGameMode()!= GameMode.CREATIVE) {
            if (!ChatQuestions.isInAir.containsKey(event.getPlayer().getName())) {
                ChatQuestions.isInAir.put(event.getPlayer().getName(), false);
            }
            if (!ChatQuestions.isInAir.get(event.getPlayer().getName())) {
                if (!ChatQuestions.disableDoubleJump.containsKey(event.getPlayer().getName())) {
                    ChatQuestions.disableDoubleJump.put(event.getPlayer().getName(), false);
                }
                if (!ChatQuestions.disableDoubleJump.get(event.getPlayer().getName())) {
                    Player player = event.getPlayer();
                    if (event.isFlying()) {
                        event.setCancelled(true);
                        player.setVelocity(player.getLocation().getDirection().add(player.getLocation().getDirection().add(player.getLocation().getDirection().add(player.getLocation().getDirection()))));
                    }
                }
                ChatQuestions.isInAir.put(event.getPlayer().getName(), true);
                event.getPlayer().setAllowFlight(false);
            }
        }
    }

//    public boolean isBlockSolid(Block block){
//        if (block!= Blocks.AIR&&block!=Blocks.WATER&&block!=Blocks.STATIONARY_WATER&&block!=Blocks.LAVA&&block!=Blocks.STATIONARY_LAVA&&
//                block!=Blocks.SIGN_POST&&block!=Blocks.WALL_SIGN&&block!=Blocks.TORCH&&block!=Blocks.REDSTONE_TORCH_OFF&&block!=Blocks.REDSTONE_TORCH_ON&&
//                block!=Blocks.REDSTONE_COMPARATOR_OFF&&block!=Blocks.REDSTONE_COMPARATOR_ON&&block!=Blocks.REDSTONE_WIRE&&block!=Blocks.WATER_LILY&&
//                block!=Blocks.DAYLIGHT_DETECTOR&&block!=Blocks.RAILS&&block!=Blocks.ACTIVATOR_RAIL&&block!=Blocks.DETECTOR_RAIL&&block!=Blocks.GOLDEN_RAIL){
//            return true;
//        }
//        else {
//            return false;
//        }
//    }
}
