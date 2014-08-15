package com.minecreatr.chatquestions.listeners;

import com.minecreatr.chatquestions.ChatQuestions;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleFlightEvent;

/**
 * Created on 8/11/2014
 */
public class ToggleFlightListener {

    public void onToggle(PlayerToggleFlightEvent event){
        if (event.getPlayer().getGameMode()!= GameMode.CREATIVE) {
            if (!ChatQuestions.isInAir.containsKey(event.getPlayer().getUniqueId())) {
                ChatQuestions.isInAir.put(event.getPlayer().getUniqueId(), false);
            }
            if (!ChatQuestions.isInAir.get(event.getPlayer().getUniqueId())) {
                if (!ChatQuestions.disableDoubleJump.containsKey(event.getPlayer().getUniqueId())) {
                    ChatQuestions.disableDoubleJump.put(event.getPlayer().getUniqueId(), true);
                }
                if (!ChatQuestions.disableDoubleJump.get(event.getPlayer().getUniqueId())) {
                    Player player = event.getPlayer();
                    if (event.isFlying()) {
                        event.setCancelled(true);
                        player.setVelocity(player.getLocation().getDirection().add(player.getLocation().getDirection().add(player.getLocation().getDirection().add(player.getLocation().getDirection()))));
                        player.playSound(player.getLocation(), Sound.IRONGOLEM_THROW, 1, 1);

                    }
                }
                ChatQuestions.isInAir.put(event.getPlayer().getUniqueId(), true);
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
