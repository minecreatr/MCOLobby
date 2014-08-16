package com.minecreatr.chatquestions.listeners;

import com.minecreatr.chatquestions.ChatQuestions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created on 8/11/2014
 */
public class CommandListener {

    public HashMap<UUID, UUID> toReply = new HashMap<UUID, UUID>();
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        Player player = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("question") && (player.hasPermission("chatquestions.askquestion")||player.isOp())){
            if (args.length<3){
                return false;
            }
            String question = "";
            String answer = "";
            boolean moveOn = false;
            int moveTo=0;
            for (int i=0;i<args.length;i++){
                if (moveOn){
                    moveTo=i;
                    break;
                }

                if (args[i].equals("||") || args[i].equals("::") || args.equals(";;")){
                    moveOn = true;
                }
                else {
                    question = question+" "+args[i];
                }
            }
            if (moveOn){
                for (int i=moveTo;i<args.length;i++){
                    answer = answer+" "+args[i];
                }
            }
            if (answer.contains("||") || answer.contains("::") || answer.contains(";;")){
                player.sendMessage("Please put in a valid answer");
                return true;
            }
            ChatQuestions.curAnswer=answer.substring(1);
            ChatQuestions.curQuestion=question.substring(1);
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(ChatQuestions.pluginPrefix + "§a§l" + ChatQuestions.curQuestion);
            Bukkit.broadcastMessage(ChatQuestions.pluginPrefix+"§5Asked by §f"+player.getDisplayName());
            Bukkit.broadcastMessage("");
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("curQuestion")||cmd.getName().equalsIgnoreCase("currentquestion")){
            if (ChatQuestions.curQuestion!=""){
                player.sendMessage(ChatQuestions.pluginPrefix+"§9"+ ChatQuestions.curQuestion);
            }
            else {
                player.sendMessage("No Current Question");
            }
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("curAnswer")){
            if (player.isOp()) {
                if (ChatQuestions.curAnswer != "") {
                    player.sendMessage(ChatQuestions.pluginPrefix + "§9" + ChatQuestions.curAnswer);
                } else {
                    player.sendMessage("No Current answer");
                }
            }
            else {
                player.sendMessage(ChatColor.RED+"You dont have permission to use this command");
            }
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("pingmsg")){

            if (args.length<2){
                return false;
            }
            if (Bukkit.getPlayer(args[0])==null){
                player.sendMessage("§4Could Not Find Target Player");
                return true;
            }
            UUID targetID = Bukkit.getPlayer(args[0]).getUniqueId();
            String message = "";
            for (int i=1;i<args.length;i++){
                message = message+args[i]+" ";
            }
            if (Bukkit.getServer().getPlayer(targetID) ==null){
                player.sendMessage("§4Could Not Find Target Player");
                return true;
            }
            if (ChatQuestions.blockPing.contains(targetID)){
                player.sendMessage("§4This player has ping messages disabled");
                return true;
            }
            message = ChatQuestions.colorize(message);
            Player targetPlayer = Bukkit.getServer().getPlayer(targetID);
            player.sendMessage("§6To "+args[0]+"§f: "+message);
            toReply.put(targetPlayer.getUniqueId(), player.getUniqueId());
            toReply.put(player.getUniqueId(), targetPlayer.getUniqueId());
            targetPlayer.sendMessage("§6From "+player.getName()+"§f: "+message);
            targetPlayer.playSound(targetPlayer.getLocation(), Sound.ANVIL_LAND, 1, 1);
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("pingreply")){
            if (!toReply.containsKey(player.getUniqueId())){
                player.sendMessage(ChatColor.RED+"You have no one to reply to!");
                return true;
            }
            if(Bukkit.getPlayer(toReply.get(player.getUniqueId()))==null){
                player.sendMessage(ChatColor.RED+"You have no one to reply to!");
                toReply.remove(player.getUniqueId());
                return true;
            }
            if (args.length<1){
                return false;
            }
            UUID targetID = toReply.get(player.getUniqueId());
            if (ChatQuestions.blockPing.contains(targetID)){
                player.sendMessage("§4This player has ping messages disabled");
                return true;
            }
            String message = "";
            for (int i=0;i<args.length;i++){
                message = message+args[i]+" ";
            }
            message = ChatQuestions.colorize(message);

            Player targetPlayer = Bukkit.getServer().getPlayer(targetID);
            player.sendMessage("§6To "+Bukkit.getPlayer(targetID).getName()+"§f: "+message);
            toReply.put(targetPlayer.getUniqueId(), player.getUniqueId());
            toReply.put(player.getUniqueId(), targetPlayer.getUniqueId());
            targetPlayer.sendMessage("§6From "+player.getName()+"§f: "+message);
            targetPlayer.playSound(targetPlayer.getLocation(), Sound.ANVIL_LAND, 1, 1);
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("pingreplyo")){
            if (!player.isOp()){
                player.sendMessage("§4You do not have permission to run this command");
                return true;
            }
            if (!toReply.containsKey(player.getUniqueId())){
                player.sendMessage(ChatColor.RED+"You have no one to reply to!");
                return true;
            }
            if(Bukkit.getPlayer(toReply.get(player.getUniqueId()))==null){
                player.sendMessage(ChatColor.RED+"You have no one to reply to!");
                toReply.remove(player.getUniqueId());
                return true;
            }
            if (args.length<1){
                return false;
            }
            UUID targetID = toReply.get(player.getUniqueId());
            String message = "";
            for (int i=0;i<args.length;i++){
                message = message+args[i]+" ";
            }
            message = ChatQuestions.colorize(message);

            Player targetPlayer = Bukkit.getServer().getPlayer(targetID);
            player.sendMessage("§6To "+Bukkit.getPlayer(targetID).getName()+"§f: "+message);
            toReply.put(targetPlayer.getUniqueId(), player.getUniqueId());
            toReply.put(player.getUniqueId(), targetPlayer.getUniqueId());
            targetPlayer.sendMessage("§6From "+player.getName()+"§f: "+message);
            targetPlayer.playSound(targetPlayer.getLocation(), Sound.ANVIL_LAND, 1, 1);
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("pingmsgo")){
            if (!player.isOp()){
                player.sendMessage("§4You do not have permission to run this command");
                return true;
            }
            if (args.length<2){
                return false;
            }
            String targetID = args[0];
            String message = "";
            for (int i=1;i<args.length;i++){
                message = message+args[i]+" ";
            }
            if (Bukkit.getServer().getPlayer(targetID) ==null){
                player.sendMessage("§4Could Not Find Target Player");
                return true;
            }
            Player targetPlayer = Bukkit.getServer().getPlayer(targetID);
            player.sendMessage("§6To "+targetID+"§f: "+message);
            targetPlayer.sendMessage("§6From "+player.getName()+"§f: "+message);
            toReply.put(targetPlayer.getUniqueId(), player.getUniqueId());
            toReply.put(player.getUniqueId(), targetPlayer.getUniqueId());
            targetPlayer.playSound(targetPlayer.getLocation(), Sound.ANVIL_BREAK, 1, 1);
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("toggleping")){
            boolean isPingBlocked;
            if (ChatQuestions.blockPing.contains(player.getUniqueId())){
                isPingBlocked=ChatQuestions.blockPing.contains(player.getUniqueId());
            }
            else {
                isPingBlocked=false;
            }
            if (isPingBlocked){
                ChatQuestions.blockPing.add(player.getUniqueId());
                player.sendMessage("§6Message Pinging is now enabled");
            }
            else if (!isPingBlocked){
                ChatQuestions.blockPing.add(player.getUniqueId());
                player.sendMessage("§4Message Pinging is now disabled");
            }
            return true;
        }
/* 
        else if (cmd.getName().equalsIgnoreCase("toggledjump")){
              boolean isJumpDisabled;
              if (ChatQuestions.disableDoubleJump.containsKey(player.getUniqueId())){
                  isJumpDisabled = ChatQuestions.disableDoubleJump.get(player.getUniqueId());
              }
              else {
                  isJumpDisabled = true;
              }
              if (isJumpDisabled){
                  ChatQuestions.disableDoubleJump.put(player.getUniqueId(), false);
                  player.setAllowFlight(true);
                  player.sendMessage("§6Double Jumping is now enabled (Will interfere with flight)");
              }
              else if (!isJumpDisabled){
                  ChatQuestions.disableDoubleJump.put(player.getUniqueId(), true);
                  player.setAllowFlight(false);
                  player.sendMessage("§4Double Jumping is now disabled");
              }
              return true;
          }
*/
        else if(cmd.getName().equalsIgnoreCase("DoubleJump")){
            if(player.hasPermission("doublejump.use")){
                if(!ChatQuestions.dJ.contains(player.getUniqueId())){
                    player.sendMessage(ChatQuestions.enabledD);
                    player.sendMessage(ChatQuestions.doubleJumpD);
                    ChatQuestions.dJ.add(player.getUniqueId());

                }else{
                    player.sendMessage(ChatQuestions.disabledD);
                    ChatQuestions.dJ.remove(player.getUniqueId());
                }
            }else{player.sendMessage(ChatQuestions.noPermD);}
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("ignorejumpcooldown")){
            if (player.isOp()){
                if (ChatQuestions.noCountdown.contains(player.getUniqueId())){
                    ChatQuestions.noCountdown.remove(player.getUniqueId());
                    player.sendMessage(ChatColor.RED+"Not Ignoring Jump Cooldown");
                }
                else {
                    ChatQuestions.noCountdown.add(player.getUniqueId());
                    player.sendMessage(ChatColor.GREEN+"Ignoring Jump Cooldown");
                }
            }
            else {
                player.sendMessage(ChatColor.RED+"You do not have permission to perform this command");
            }
            return true;
        }
        return false;
    }
}
