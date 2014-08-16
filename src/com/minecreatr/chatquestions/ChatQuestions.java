package com.minecreatr.chatquestions;

import com.minecreatr.chatquestions.listeners.ChatListener;
import com.minecreatr.chatquestions.listeners.CommandListener;
import com.minecreatr.chatquestions.listeners.ToggleFlightListener;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

/**
 * Created on 6/24/2014
 */
public class ChatQuestions extends JavaPlugin implements Listener {

    public static String curAnswer = "";
    public static String curQuestion = "";
    //red green yellow [MCO]
    public static String pluginPrefix = "[§cM§2C§eO§f§6C§dQ§f] ";
    //player
    public static HashMap<UUID, Boolean> blockPing = new HashMap<UUID, Boolean>();
    public static HashMap<UUID, Boolean> disableDoubleJump = new HashMap<UUID, Boolean>();
    //public static HashMap<UUID, Boolean> isInAir = new HashMap<UUID, Boolean>();
    private ChatListener chatListener = new ChatListener();
    private CommandListener commandListener = new CommandListener();
    private ToggleFlightListener toggleFlightListener = new ToggleFlightListener();


    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
//        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
//        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
//            @Override
//            public void run() {
//                Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
//                Iterator<? extends Player> it = players.iterator();
//                while (it.hasNext()){
//                    Player curPlayer = it.next();
//                    PlayerTickEvent playerTickEvent = new PlayerTickEvent(curPlayer);
//                    getServer().getPluginManager().callEvent(playerTickEvent);
//                }
//            }
//        }, 0L, 0L);
//        chatListener = new ChatListener();
//        commandListener = new CommandListener();
//        toggleFlightListener = new ToggleFlightListener();
    }


    public void onDisable() {
        Iterator<UUID> ids = disableDoubleJump.keySet().iterator();
        while (ids.hasNext()){
            UUID curId = ids.next();
            if (Bukkit.getPlayer(curId)!=null){
                if (Bukkit.getPlayer(curId).getGameMode() != GameMode.CREATIVE){
                    Bukkit.getPlayer(curId).setAllowFlight(false);
                }
            }
        }
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

    public static boolean containsIgnoreCase(String par1, String par2) {
        String temp1 = par1.toLowerCase();
        String temp2 = par2.toLowerCase();
        return temp1.contains(temp2);
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        chatListener.onChat(event);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return commandListener.onCommand(sender, cmd, label, args);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerToggleFlight(PlayerToggleFlightEvent event) {
        toggleFlightListener.onToggle(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event){
        if (disableDoubleJump.get(event.getPlayer().getUniqueId())==null){
            disableDoubleJump.put(event.getPlayer().getUniqueId(), true);
        }
        if (ToggleFlightListener.isOnGround(event.getPlayer())&&!disableDoubleJump.get(event.getPlayer().getUniqueId())){
            event.getPlayer().setAllowFlight(true);
        }
    }

//    @EventHandler
//    public void onPlayerTick(PlayerTickEvent event){
//        if (event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid()){
//            isInAir.put(event.getPlayer().getUniqueId(), false);
//            if (disableDoubleJump.get(event.getPlayer().getUniqueId())==null){
//                disableDoubleJump.put(event.getPlayer().getUniqueId(), true);
//            }
//            if (event.getPlayer().getGameMode()!=GameMode.CREATIVE && !disableDoubleJump.get(event.getPlayer().getUniqueId())) {
//                event.getPlayer().setAllowFlight(true);
//            }
//        }
//    }
//}
}