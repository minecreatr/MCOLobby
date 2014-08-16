package com.minecreatr.chatquestions.listeners;

import com.minecreatr.chatquestions.ChatQuestions;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

/**
 * Created on 8/11/2014
 */
public class ToggleFlightListener {

    public void onToggle(PlayerToggleFlightEvent event){
        //event.getPlayer().sendMessage("Is jumping disabled: "+ ChatQuestions.disableDoubleJump.get(event.getPlayer().getUniqueId()));
        //event.getPlayer().sendMessage("Is on ground: "+isOnGround(event.getPlayer()));
        if (event.getPlayer().getGameMode()!= GameMode.CREATIVE && !ChatQuestions.disableDoubleJump.contains(event.getPlayer().getUniqueId())) {
            if (isOnGround(event.getPlayer())) {
                if (!ChatQuestions.disableDoubleJump.contains(event.getPlayer().getUniqueId())) {
                    Player player = event.getPlayer();
                    if (event.isFlying()) {
                        player.setVelocity(player.getLocation().getDirection().add(player.getLocation().getDirection().add(player.getLocation().getDirection().add(player.getLocation().getDirection().add(player.getLocation().getDirection().add(player.getLocation().getDirection().add(player.getLocation().getDirection().add(player.getLocation().getDirection().add(player.getLocation().getDirection())))))))));
                        //player.setVelocity(player.getVelocity().multiply(new Vector(2, 2, 2)));
                        player.playSound(player.getLocation(), Sound.IRONGOLEM_THROW, 1, 1);
                    }
                }
                event.getPlayer().setAllowFlight(false);
            }
            else {
                event.getPlayer().setAllowFlight(false);
            }
            event.setCancelled(true);
        }
    }

    public static boolean isOnGround(Player player){
        Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
        return (block.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getType().isSolid());
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
