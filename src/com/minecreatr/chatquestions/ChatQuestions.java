package com.minecreatr.chatquestions;

import com.minecreatr.chatquestions.listeners.ChatListener;
import com.minecreatr.chatquestions.listeners.CommandListener;
import com.minecreatr.chatquestions.listeners.ToggleFlightListener;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created on 6/24/2014
 */
public class ChatQuestions extends JavaPlugin implements Listener {

    public static final String section = "§";
    public static String curAnswer = "";
    public static String curQuestion = "";
    public static String curAsker = "";
    public static ArrayList<String> curHints = new ArrayList<String>();
    public static UUID questionUUID;
    public static int pingCooldownLimit;
    public final static long questionTimeout = 20*60*3;
    //red green yellow [MCO]
    public static String pluginPrefix = "[§cM§2C§eO§f§6C§dQ§f] ";
    public static ArrayList<UUID> blockPing = new ArrayList<UUID>();
    public static ArrayList<UUID> disableDoubleJump = new ArrayList<UUID>();
    public static ArrayList<String> filter = new ArrayList<String>();
    //public static HashMap<UUID, Boolean> isInAir = new HashMap<UUID, Boolean>();
    private ChatListener chatListener = new ChatListener(this);
    private CommandListener commandListener = new CommandListener(this);
    private ToggleFlightListener toggleFlightListener = new ToggleFlightListener();

    //If player can double jump
    public static ArrayList<UUID> dJ = new ArrayList<UUID>();
    public static ArrayList<UUID> noCountdown = new ArrayList<UUID>();
    public static HashMap<UUID, Long> cooldown = new HashMap<UUID, Long>();
    public static HashMap<UUID, Long> pingCooldown = new HashMap<UUID, Long>();

    public static String enabledD = ChatColor.GREEN + "Enabled Leap Jump!";
    public static String disabledD = ChatColor.RED + "Disabled Leap Jump!";
    public static String noPermD = "" + ChatColor.RED + ChatColor.ITALIC + "Donate to get the ability to Leap Jump!";
    public static String doubleJumpD = "" + ChatColor.GREEN + ChatColor.UNDERLINE + "To Leap Jump hold shift and jump!";

    private FileConfiguration questionStats= null;
    private File questionStatsFile = null;

    //gets a long representation of a string value
    public static long getValue(String in){
        char[] chars = in.toCharArray();
        long out = 0;
        for (int i=0;i<chars.length;i++){
            out=out+chars[i];
        }
        return out;
    }

    //reload question stats
    public void reloadQuestionStats(){
        if(questionStatsFile==null){
            questionStatsFile = new File(getDataFolder(), "questionStats.yml");
        }
        questionStats = YamlConfiguration.loadConfiguration(questionStatsFile);
    }

    //get question stats
    public FileConfiguration getQuestionStats() {
        if (questionStatsFile == null) {
            reloadQuestionStats();
        }
        return questionStats;
    }


    public void saveQuestionStatsList() {
        if (questionStats == null || questionStatsFile == null) {
            return;
        }
        try {
            getQuestionStats().save(questionStatsFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + questionStatsFile, ex);
        }
    }

    public void expire(final UUID id){
        BukkitScheduler s = Bukkit.getScheduler();
        s.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                if (id==ChatQuestions.questionUUID){
                    Bukkit.broadcastMessage(ChatQuestions.pluginPrefix + " §6No one has answered the question ):");
                    Bukkit.broadcastMessage(ChatQuestions.pluginPrefix+"§9The correct answer was §c§l"+ChatQuestions.curAnswer);
                    Bukkit.broadcastMessage(ChatQuestions.pluginPrefix+"§9and the question was §c§l"+ChatQuestions.curQuestion);
                    curQuestion="";
                    curAnswer="";
                    questionUUID=null;
                    curHints.clear();
                }
            }
        }, questionTimeout);
    }


    public void onEnable() {
        this.getDataFolder().mkdirs();
        File file = new File(this.getDataFolder()+File.separator+"QuestionFilter.txt");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.getDataFolder()+File.separator+"QuestionFilter.txt"));
            while (br.ready()) {
                filter.add(br.readLine());
            }
            br.close();
        } catch (Exception e){
            e.printStackTrace();
            this.getLogger().info("Couldn't read QuestionFilter.txt");
        }
        getServer().getPluginManager().registerEvents(this, this);
        if (this.getConfig().getInt("pingMsgCooldown")==0){
            pingCooldownLimit = 3;
            this.getLogger().info("Could not find pingmsg cooldown, setting to 3");
        }
        else {
            pingCooldownLimit = this.getConfig().getInt("pingMsgCooldown");
            this.getLogger().info("Setting pingmsg cooldown to "+this.getConfig().getInt("pingMsgCooldown"));
        }
        reloadQuestionStats();
    }


    public void onDisable() {
        saveQuestionStatsList();
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

    public static String colorize(String in){
        if (!in.contains(section)){
            return in;
        }
        char[] chars = in.toCharArray();
        String out = "";
        for (int i=0;i<chars.length;i++){
            if (chars[i]=='&'){
                out=out+section;
            }
            else {
                out=out+chars[i];
            }
        }
        return out;
    }

    public static boolean isDirty(String in){
        boolean out = false;
        for (int i=0;i<filter.size();i++){
            if (in.contains(filter.get(i))){
                out = true;
            }
        }
        return out;
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