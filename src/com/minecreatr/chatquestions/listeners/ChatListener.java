package com.minecreatr.chatquestions.listeners;

import com.minecreatr.chatquestions.ChatQuestions;
import net.minecraft.server.v1_7_R4.EntityFireworks;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Created on 8/11/2014
 */
public class ChatListener {

    public ChatQuestions instance;


    public ChatListener(ChatQuestions q){
        instance=q;
    }

    public void onChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        //its a shame to delete good code
//        if (player.getName().equals("minecreatr")){
//            event.setCancelled(false);
//        }
        if ((!(ChatQuestions.curAnswer.equalsIgnoreCase("")))&& ChatQuestions.containsIgnoreCase(event.getMessage(), ChatQuestions.curAnswer)){
            player.sendMessage(ChatQuestions.pluginPrefix+"§e§lCongratulations you guessed correctly");
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(ChatQuestions.pluginPrefix + player.getDisplayName() + " §9has answered the question correctly!!!");
            Bukkit.broadcastMessage(ChatQuestions.pluginPrefix+"§9The correct answer was §c§l"+ChatQuestions.curAnswer);
            Bukkit.broadcastMessage(ChatQuestions.pluginPrefix+"§9and the question was §c§l"+ChatQuestions.curQuestion);
            Bukkit.broadcastMessage("");
            ChatQuestions.curAnswer="";
            ChatQuestions.curQuestion="";
            ChatQuestions.questionUUID=null;
            ChatQuestions.curAsker="";
            int curNum = instance.getQuestionStats().getInt(player.getName());
            instance.getQuestionStats().set(player.getName(), curNum+1);
            event.setCancelled(true);
            for (int i=0;i<20;i++) {
                EntityFireworks fw = (EntityFireworks) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
            }
        }
    }
}
