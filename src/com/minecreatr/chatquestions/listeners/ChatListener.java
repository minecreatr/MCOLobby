package com.minecreatr.chatquestions.listeners;

import com.minecreatr.chatquestions.ChatQuestions;
import net.minecraft.server.v1_7_R3.EntityFireworks;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Created on 8/11/2014
 */
public class ChatListener {

    public void onChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        if (player.getName().equals("minecreatr")){
            event.setCancelled(false);
        }
        if ((!(ChatQuestions.curAnswer.equalsIgnoreCase("")))&& ChatQuestions.containsIgnoreCase(event.getMessage(), ChatQuestions.curAnswer)){
            player.sendMessage(ChatQuestions.pluginPrefix+"§e§lCONGRATULATIONS YOU GUESSED CORRECTLY");
            Bukkit.broadcastMessage(ChatQuestions.pluginPrefix + player.getDisplayName() + " §9HAS ANSWERED THE QESTION CORRECTLY!!!");
            Bukkit.broadcastMessage(ChatQuestions.pluginPrefix+"§9THE CORRECT ANSWER WAS §c§l"+ChatQuestions.curAnswer.toUpperCase());
            Bukkit.broadcastMessage(ChatQuestions.pluginPrefix+"§9AND THE QUESTION WAS §c§l"+ChatQuestions.curQuestion.toUpperCase());
            ChatQuestions.curAnswer="";
            ChatQuestions.curQuestion="";
            event.setCancelled(true);
            for (int i=0;i<20;i++) {
                EntityFireworks fw = (EntityFireworks) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
            }
        }
    }
}
