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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created on 6/24/2014
 */
public class ChatQuestions extends JavaPlugin implements Listener {

    public static String curAnswer = "";
    public static String curQuestion = "";
    //red green yellow [MCO]
    public static String pluginPrefix = "[§cM§2C§eO§f§6C§dQ§f] ";
    public static HashMap<UUID, Boolean> blockPing = new HashMap<UUID, Boolean>();
    public static HashMap<UUID, Boolean> disableDoubleJump = new HashMap<UUID, Boolean>();
    //public static HashMap<UUID, Boolean> isInAir = new HashMap<UUID, Boolean>();
    private ChatListener chatListener = new ChatListener();
    private CommandListener commandListener = new CommandListener();
    private ToggleFlightListener toggleFlightListener = new ToggleFlightListener();

    //If player can double jump
    public static ArrayList<UUID> dJ = new ArrayList<UUID>();
    public static ArrayList<UUID> noCountdown = new ArrayList<UUID>();
    public static HashMap<UUID, Long> cooldown = new HashMap<UUID, Long>();

    public static String enabledD = ChatColor.GREEN + "Enabled Double Jump!";
    public static String disabledD = ChatColor.RED + "Disabled Double Jump!";
    public static String noPermD = "" + ChatColor.RED + ChatColor.ITALIC + "Donate to get the ability to Double Jump!";


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


    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent e){
        Player p = e.getPlayer();
        if(dJ.contains(p.getUniqueId())){
            boolean jump = false;
            boolean cD = false;
            if(e.getTo().getBlockY() - e.getFrom().getBlockY() == 1){
                jump = true;}
            if(cooldown.containsKey(p.getUniqueId())){
                if(System.currentTimeMillis() - cooldown.get(p.getUniqueId()) < 1000 * 3){
                    cD = true;
                }
            }

            if(p.isSneaking() && jump){
                if(!cD || noCountdown.contains(p.getUniqueId())){
                    //p.setVelocity(p.getLocation().getDirection().add(new Vector(0, 1, 0)));
                    launch(p);
                    cooldown.put(p.getUniqueId(), System.currentTimeMillis());
                    if (!noCountdown.contains(p.getUniqueId())) {
                        this.timers(p);
                    }
                }
            }
        }
    }

    public void launch(Player player){
        player.setVelocity(player.getLocation().getDirection().add(player.getLocation().getDirection().
                add(player.getLocation().getDirection().add(player.getLocation().getDirection()))));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        dJ.remove(p.getUniqueId());
    }

    @EventHandler
    public void dJnoDMG(EntityDamageEvent e){
        if(e.getEntity() instanceof Player &&  e.getCause() == EntityDamageEvent.DamageCause.FALL){
            Player p = (Player) e.getEntity();
            if(dJ.contains(p.getUniqueId())){e.setCancelled(true);}
        }
    }

    public void timers(final Player p){
        final Float originalEXP = p.getExp();
        final int originalLevel = p.getLevel();
        final BukkitScheduler s = Bukkit.getScheduler();

        p.setExp(1);
        p.setLevel(3);
        p.getWorld().playSound(p.getEyeLocation(), Sound.IRONGOLEM_THROW, 1, 1);
        Runnable exp = new Runnable(){
            public void run(){
                p.setExp(p.getExp() - 1/18F);
                Location loc = p.getLocation();
                Location location = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
                Material block = location.getBlock().getType();
                if(block == Material.AIR){p.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 13);}
            }
        };
        final int expTask = s.scheduleSyncRepeatingTask(this, exp, 0, 60/18);

        Runnable levels = new Runnable(){
            public void run(){
                p.setLevel(p.getLevel() - 1);
            }
        };
        final int levelTask = s.scheduleSyncRepeatingTask(this, levels, 20, 20);

        Runnable reset = new Runnable(){
            public void run(){
                p.setExp(originalEXP);
                p.setLevel(originalLevel);
                s.cancelTask(expTask);
                s.cancelTask(levelTask);
            }
        };
        s.scheduleSyncDelayedTask(this, reset, 3 * 20);
    }

//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void playerToggleFlight(PlayerToggleFlightEvent event) {
//        toggleFlightListener.onToggle(event);
//    }

//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onPlayerMove(PlayerMoveEvent event){
//        if (disableDoubleJump.get(event.getPlayer().getUniqueId())==null){
//            disableDoubleJump.put(event.getPlayer().getUniqueId(), true);
//        }
//        if (ToggleFlightListener.isOnGround(event.getPlayer())&&!disableDoubleJump.get(event.getPlayer().getUniqueId())){
//            event.getPlayer().setAllowFlight(true);
//        }
//    }


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