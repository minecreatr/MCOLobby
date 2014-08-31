package com.minecreatr.chatquestions.listeners;

import com.minecreatr.chatquestions.ChatQuestions;
import net.minecraft.server.v1_7_R2.EntityFireworks;
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
        if ((!(instance.curAnswer.equalsIgnoreCase("")))&& instance.containsIgnoreCase(event.getMessage(), instance.curAnswer)){
            player.sendMessage(instance.pluginPrefix+"§e§lCongratulations you guessed correctly");
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(instance.pluginPrefix + player.getDisplayName() + " §9has answered the question correctly!!!");
            Bukkit.broadcastMessage(instance.pluginPrefix+"§9The correct answer was §c§l"+instance.curAnswer);
            Bukkit.broadcastMessage(instance.pluginPrefix+"§9and the question was §c§l"+instance.curQuestion);
            Bukkit.broadcastMessage("");
            instance.curAnswer="";
            instance.curQuestion="";
            instance.questionUUID=null;
            instance.curAsker="";
            instance.curHints.clear();
            int curNum = instance.getQuestionStats().getInt(player.getName());
            instance.getQuestionStats().set(player.getName(), curNum+1);
            event.setCancelled(true);
            for (int i=0;i<20;i++) {
                EntityFireworks fw = (EntityFireworks) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
            }
        }
    }
}
