package com.minecreatr.chatquestions;

import com.minecreatr.chatquestions.listeners.ChatListener;
import com.minecreatr.chatquestions.listeners.CommandListener;
import com.minecreatr.chatquestions.listeners.ToggleFlightListener;
import net.minecraft.server.v1_7_R3.EntityFireworks;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R3.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created on 6/24/2014
 */
public class ChatQuestions extends JavaPlugin implements Listener{

    public static String curAnswer = "";
    public static String curQuestion = "";
    //red green yellow [MCO]
    public static String pluginPrefix = "[§cM§2C§eO§f§6C§dQ§f] ";
    public static HashMap<String, Boolean> blockPing = new HashMap<String, Boolean>();
    public static HashMap<String, Boolean> disableDoubleJump = new HashMap<String, Boolean>();
    public static HashMap<String, Boolean> isInAir = new HashMap<String, Boolean>();
    private ChatListener chatListener = new ChatListener();
    private CommandListener commandListener = new CommandListener();
    private ToggleFlightListener toggleFlightListener = new ToggleFlightListener();


    public void onEnable(){
        getServer().getPluginManager().registerEvents(this, this);
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                Player[] players = Bukkit.getServer().getOnlinePlayers();
                for (int i=0; i<players.length;i++){
                    Player curPlayer = players[i];
                    PlayerTickEvent playerTickEvent = new PlayerTickEvent(curPlayer);
                    getServer().getPluginManager().callEvent(playerTickEvent);
                }
            }
        }, 0L, 0L);
//        chatListener = new ChatListener();
//        commandListener = new CommandListener();
//        toggleFlightListener = new ToggleFlightListener();
    }



    public void onDisable(){

    }
//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onPlayerAttack(EntityDamageByEntityEvent event){
//        if (event.getDamager() instanceof Player){
//            Player player = (Player) event.getDamager();
//            if (player.getName().equals("minecreatr")){
//                event.setCancelled(false);
//            }
//        }
//    }

    public static boolean containsIgnoreCase(String par1, String par2){
        String temp1 = par1.toLowerCase();
        String temp2 = par2.toLowerCase();
        return temp1.contains(temp2);
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event){
        chatListener.onChat(event);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        return commandListener.onCommand(sender, cmd, label, args);
    }

    @EventHandler
    public void playerToggleFlight(PlayerToggleFlightEvent event){
        toggleFlightListener.onToggle(event);
    }

    @EventHandler
    public void onPlayerTick(PlayerTickEvent event){
        if (event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid()){
            isInAir.put(event.getPlayer().getName(), false);
            if (event.getPlayer().getGameMode()!=GameMode.CREATIVE) {
                event.getPlayer().setAllowFlight(true);
            }
        }
    }
}
