package com.minecreatr.chatquestions;

import com.minecreatr.chatquestions.listeners.ChatListener;
import com.minecreatr.chatquestions.listeners.CommandListener;
import net.minecraft.server.v1_7_R2.EntityLiving;
import net.minecraft.server.v1_7_R2.EntitySnowball;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.logging.Level;

/**
 * Created on 6/24/2014
 */
public class ChatQuestions extends JavaPlugin implements Listener {

    Random random = new Random();
    public static final String section = "§";
    public String curAnswer = "";
    public String curQuestion = "";
    public String curAsker = "";
    public ArrayList<String> curHints = new ArrayList<String>();
    public HashMap<UUID, DisguisedBlock> coloredBlocks = new HashMap<UUID, DisguisedBlock>();
    public boolean haltRender = false;
    private int paintTimeout;
    public UUID questionUUID;
    public int pingCooldownLimit;
    public final static long questionTimeout = 20*60*3;
    public ScoreboardManager manager;
    public Scoreboard board;
    public Objective paintHits;
    //public static boolean isTesting;
    //red green yellow [MCO]
    public static final String pluginPrefix = "[§cM§2C§eO§f§6C§dQ§f] ";
    public ArrayList<UUID> blockPing = new ArrayList<UUID>();
    public ArrayList<UUID> disableDoubleJump = new ArrayList<UUID>();
    public ArrayList<UUID> paintballs = new ArrayList<UUID>();
    public ArrayList<String> filter = new ArrayList<String>();
    public ArrayList<UUID> pigs = new ArrayList<UUID>();
    //public static HashMap<UUID, Boolean> isInAir = new HashMap<UUID, Boolean>();
    private ChatListener chatListener = new ChatListener(this);
    private CommandListener commandListener = new CommandListener(this);

    //If player can double jump
    public ArrayList<UUID> dJ = new ArrayList<UUID>();
    public ArrayList<UUID> noCountdown = new ArrayList<UUID>();
    public HashMap<UUID, Long> cooldown = new HashMap<UUID, Long>();
    public HashMap<UUID, Long> pingCooldown = new HashMap<UUID, Long>();
    public HashMap<UUID, Long> paintCooldown = new HashMap<UUID, Long>();
    public HashMap<UUID, Long> pigCooldown = new HashMap<UUID, Long>();
    public HashMap<UUID, Long> batCooldown = new HashMap<UUID, Long>();

    public static final String enabledD = ChatColor.GREEN + "Enabled Leap Jump!";
    public static final String disabledD = ChatColor.RED + "Disabled Leap Jump!";
    public static final String noPermD = "" + ChatColor.RED + ChatColor.ITALIC + "Donate to get the ability to Leap Jump!";
    public static final String noPermPaint = "" + ChatColor.RED + ChatColor.ITALIC + "Donate to get the ability to launch paint!";
    public static final String doubleJumpD = "" + ChatColor.GREEN + ChatColor.UNDERLINE + "To Leap Jump hold shift and jump!";
    public static final String noPermP = ""+ChatColor.RED + ChatColor.ITALIC + "Donate to get the ability to shoot pigs!";
    public static final String noPermB = ""+ChatColor.RED + ChatColor.ITALIC + "Donate to be batman";

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
                if (id==questionUUID){
                    Bukkit.broadcastMessage(ChatQuestions.pluginPrefix + " §6No one has answered the question ):");
                    Bukkit.broadcastMessage(ChatQuestions.pluginPrefix+"§9The correct answer was §c§l"+curAnswer);
                    Bukkit.broadcastMessage(ChatQuestions.pluginPrefix+"§9and the question was §c§l"+curQuestion);
                    curQuestion="";
                    curAnswer="";
                    questionUUID=null;
                    curHints.clear();
                }
            }
        }, questionTimeout);
    }


    public void onEnable() {
        loadStuff();
        reloadQuestionStats();
        BukkitScheduler s = Bukkit.getScheduler();
//        Runnable update = new Runnable() {
//            @Override
//            public void run() {
//                Player[] players = Bukkit.getOnlinePlayers();
//                for (int i=0;i<players.length;i++){
//                    PlayerTickEvent event = new PlayerTickEvent(players[i]);
//                    getServer().getPluginManager().callEvent(event);
//                    TickEvent tick = new TickEvent();
//                    getServer().getPluginManager().callEvent(tick);
//                }
//            }
//        };
//        s.scheduleSyncRepeatingTask(this, update, 0l, 1l);
        saveConfig();
        manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();
        paintHits = board.getObjective("paintHits");
        if (paintHits==null){
            paintHits = board.registerNewObjective("paintHits", "dummy");
        }
        paintHits.setDisplayName(ChatColor.AQUA+"Paintball Hits");
        paintHits.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    public void loadStuff(){
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
        reloadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        if (this.getConfig().getInt("pingMsgCooldown")==0){
            pingCooldownLimit = 3;
            this.getLogger().info("Could not find pingmsg cooldown, setting to 3");
            this.getConfig().set("pingMsgCooldown", 3);
        }
        else {
            pingCooldownLimit = this.getConfig().getInt("pingMsgCooldown");
            this.getLogger().info("Setting pingmsg cooldown to "+this.getConfig().getInt("pingMsgCooldown"));
        }
        if (this.getConfig().getInt("paintTimeout")==0){
            paintTimeout = 3;
            this.getLogger().info("Could not find paint ball timeout time, setting to 3");
            this.getConfig().set("paintTimeout", 3);
        }
        else {
            paintTimeout = this.getConfig().getInt("paintTimeout");
            this.getLogger().info("Setting the paint timeout to "+this.getConfig().getInt("paintTimeout"));
        }
        //this.isTesting=this.getConfig().getBoolean("testingMode");
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


    @EventHandler(priority = EventPriority.HIGHEST)
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

//    public void resetBlock(final Location location, final Material block, final byte meta){
//        final BukkitScheduler s = Bukkit.getScheduler();
//        Runnable reset = new Runnable() {
//            @Override
//            public void run() {
//                if (location.getBlock().getType()==Material.WOOL) {
//                    location.getBlock().setType(block);
//                    location.getBlock().setData(meta);
//                }
//            }
//        };
//        s.scheduleSyncDelayedTask(this, reset, 20*3);
//    }
//
//    public void resetMeta(final Location location, final byte meta){
//        final BukkitScheduler s = Bukkit.getScheduler();
//        Runnable reset = new Runnable() {
//            @Override
//            public void run() {
//                if (location.getBlock().getType()==Material.WOOL) {
//                    location.getBlock().setData(meta);
//                }
//            }
//        };
//        s.scheduleSyncDelayedTask(this, reset, 20*3);
//    }

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

    public void pigExplode(final UUID id){
        BukkitScheduler s = Bukkit.getScheduler();
        Runnable selfDestruct = new Runnable() {
            @Override
            public void run() {
                Pig pig = getPig(id);
                if (pig!=null){
                    Location loc = pig.getLocation();
                    pig.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 4, false, false);
                    pig.setHealth(0);
                }
            }
        };
        s.scheduleSyncDelayedTask(this, selfDestruct, 20*2);
    }

    public void batDie(final Bat bat){
        BukkitScheduler s = Bukkit.getScheduler();
        Runnable batDestroy = new Runnable() {
            @Override
            public void run() {
                bat.getWorld().playEffect(bat.getLocation(), Effect.SMOKE, 13);
                bat.setHealth(0);
            }
        };
        s.scheduleSyncDelayedTask(this, batDestroy, 20*6);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event){
        if (pigs.contains(event.getEntity().getUniqueId()) && event.getEntity() instanceof Pig){
            event.getDrops().clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event){
        //event.getPlayer().sendMessage(event.getAction().toString());
        if ((event.getAction()== Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)&& event.getPlayer().getItemInHand().getType()==Material.IRON_BARDING
                && event.getPlayer().getItemInHand().getItemMeta().getLore().contains(""+ChatColor.GREEN+ChatColor.ITALIC+"Shoots a paintball") &&
                event.getPlayer().hasPermission("lobbyplus.paint")){
            Player player = event.getPlayer();
            if (paintCooldown.containsKey(player.getUniqueId())){
                if (!(System.currentTimeMillis() - paintCooldown.get(player.getUniqueId()) > 1000 * 6 || player.isOp())){
                    player.sendMessage(ChatColor.RED+"You can use the paintgun in "+(6-(1000*(System.currentTimeMillis()-paintCooldown.get(player.getUniqueId()))))+" seconds");
                    return;
                }
            }
            Snowball snowball = player.throwSnowball();
            paintballs.add(snowball.getUniqueId());
            player.playSound(snowball.getLocation(), Sound.CHICKEN_EGG_POP, 1, 1);
            paintCooldown.put(player.getUniqueId(), System.currentTimeMillis());
            event.setCancelled(true);
        }
        else if ((event.getAction()== Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getPlayer().getItemInHand().getType()==Material.TRIPWIRE_HOOK
                &&event.getPlayer().getItemInHand().getItemMeta().getLore().contains(""+ChatColor.GREEN+ChatColor.ITALIC+"Launches a Pig") &&
                event.getPlayer().hasPermission("lobbyplus.piglaunch")){
            Player player = event.getPlayer();
            if (pigCooldown.containsKey(player.getUniqueId())){
                if (!(System.currentTimeMillis() - pigCooldown.get(player.getUniqueId()) > 1000 * 60 || player.isOp())){
                    player.sendMessage(ChatColor.RED+"You can use the pig launcher in "+(60-(1000*(System.currentTimeMillis()-pigCooldown.get(player.getUniqueId()))))+" seconds");
                    return;
                }
            }
            Pig pig = (Pig)player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.PIG);
            pigs.add(pig.getUniqueId());
            //pig.setSaddle(true);
            pig.setVelocity(player.getLocation().getDirection().add(player.getLocation().getDirection().
                    add(player.getLocation().getDirection().add(player.getLocation().getDirection()))));
            player.playSound(player.getLocation(), Sound.PIG_DEATH, 3, 2);
            pigExplode(pig.getUniqueId());
            pigCooldown.put(player.getUniqueId(), System.currentTimeMillis());
            event.setCancelled(true);
        }
        else if ((event.getAction()== Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getPlayer().getItemInHand().getType()==Material.GHAST_TEAR
                &&event.getPlayer().getItemInHand().getItemMeta().getLore().contains(""+ChatColor.DARK_AQUA+ChatColor.ITALIC+"Makes you batman") &&
                event.getPlayer().hasPermission("lobbyplus.batman")){
            Player player = event.getPlayer();
            if (batCooldown.containsKey(player.getUniqueId())){
                if (!(System.currentTimeMillis() - batCooldown.get(player.getUniqueId()) > 1000 * 30 || player.isOp())){
                    player.sendMessage(ChatColor.RED+"You can use batman in "+(30-(1000*(System.currentTimeMillis()-batCooldown.get(player.getUniqueId()))))+" seconds");
                    return;
                }
            }
            int batNum = random.nextInt(15);
            for (int i=0;i<batNum;i++){
                Bat curBat = (Bat)player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.BAT);
                player.getWorld().playEffect(player.getEyeLocation(), Effect.SMOKE, 13);
                batDie(curBat);
            }
            batCooldown.put(player.getUniqueId(), System.currentTimeMillis());
            event.setCancelled(true);

        }

    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFrameBreak(HangingBreakEvent event){
        if (event.getCause()== HangingBreakEvent.RemoveCause.EXPLOSION){
            if (event.getEntity() instanceof ItemFrame){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event){
        if (pigs.contains(event.getEntity().getUniqueId())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void entityDamageByEntity(EntityDamageByEntityEvent event){
        if (event.getCause()== EntityDamageEvent.DamageCause.PROJECTILE){
            if (event.getDamager() instanceof EntitySnowball){
                if (paintballs.contains(((EntitySnowball) event.getDamager()).getUniqueID())){
                    EntityLiving shooter = ((EntitySnowball) event.getDamager()).getShooter();
                    if (event.getEntity() instanceof Player){
                        ((Player) event.getEntity()).sendMessage("You were painted by "+shooter.getName());
                        Score score = paintHits.getScore((OfflinePlayer)shooter);
                        score.setScore(score.getScore()+1);
                        ((Player) shooter).playSound(((Player) shooter).getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
                    }
                }
            }
        }
    }

    public void removePaint(final Location location){
        BukkitScheduler s = Bukkit.getScheduler();
        Runnable remove = new Runnable() {
            @Override
            public void run() {
                Player[] players = Bukkit.getOnlinePlayers();
                for (int i=0;i<players.length;i++){
                    players[i].sendBlockChange(location, location.getBlock().getType(), location.getBlock().getData());
                }
            }
        };
        s.scheduleSyncDelayedTask(this, remove, 20*3);
    }

    public void addPaint(Location location, byte color, int times){
        if (location.getBlock().getType().isSolid() && (location.getBlock().getType()!=Material.SIGN && location.getBlock().getType()!=Material.SIGN_POST
            && location.getBlock().getType()!=Material.WALL_SIGN)){
//            haltRender = true;
//            DisguisedBlock block = new DisguisedBlock(Material.WOOL, color, location);
//            coloredBlocks.put(block.getID(), block);
//            haltRender = false;
            Player[] players = Bukkit.getOnlinePlayers();
            for (int i=0;i<players.length;i++){
                players[i].sendBlockChange(location, Material.WOOL, color);
            }
            removePaint(location);
        }
        else if (!(times>1)){
            location.setY(location.getY()-1);
            times++;
            addPaint(location, color, times);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileHit(ProjectileHitEvent event){
        if (paintballs.contains(event.getEntity().getUniqueId())){
            paintballs.remove(event.getEntity().getUniqueId());
            //event.getEntity().getWorld().getBlockAt(event.getEntity().getLocation()).setType(Material.WOOL);
            Location loc = event.getEntity().getLocation();
            addPaint(new Location(event.getEntity().getWorld(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ()), (byte) random.nextInt(15), 0);
        }
    }

    public boolean isDirty(String in){
        boolean out = false;
        for (int i=0;i<filter.size();i++){
            if (in.contains(filter.get(i))){
                out = true;
            }
        }
        return out;
    }

    @EventHandler
    public void onPlayerTick(PlayerTickEvent event){
        try {
            Iterator<DisguisedBlock> blocks = coloredBlocks.values().iterator();
            while (blocks.hasNext() && !haltRender) {
                DisguisedBlock block = blocks.next();
                if ((System.currentTimeMillis() - block.getTimeCreated()) > (1000 * paintTimeout)) {
                    block.renderOld(event.getPlayer());
                    coloredBlocks.remove(block.getID());
                } else {
                    block.render(event.getPlayer());
                }
            }
        } catch(ConcurrentModificationException exception){
            //we dont care so do nothing
        }
    }

    @EventHandler
    public void onCollide(VehicleEntityCollisionEvent event){

    }

    @EventHandler
    public void onTick(TickEvent event){
        for (int i=0;i<pigs.size();i++){
            UUID curId = pigs.get(i);
            Pig pig = getPig(curId);
            if (pig==null){
                pigs.remove(i);
            }
        }
    }

    public static Pig getPig(UUID id){
        for (World w : Bukkit.getWorlds()){
            Iterator<Pig> p = w.getEntitiesByClass(Pig.class).iterator();
            while(p.hasNext()){
                Pig curPig = p.next();
                if (curPig.getUniqueId()==id){
                    return curPig;
                }
            }
        }
        return null;
    }

    public static ChatQuestions getInstance() throws ClassNotFoundException{
        if (Bukkit.getPluginManager().getPlugin("LobbyPlus") instanceof ChatQuestions){
            ChatQuestions instance = (ChatQuestions)Bukkit.getPluginManager().getPlugin("LobbyPlus");
            return instance;
        }
        else {
            throw new ClassNotFoundException("I'M SPECIAL!");
        }
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


//}
}