package com.minecreatr.chatquestions.listeners;

import com.minecreatr.chatquestions.ChatQuestions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Created on 8/11/2014
 */
public class CommandListener {

    public HashMap<UUID, UUID> toReply = new HashMap<UUID, UUID>();
    public static String noPermission = ChatColor.RED+"You dont have permission to use this command";
    public static String dontSpam = ChatColor.RED+"Dont spam pingmsg!";
    public static ChatQuestions instance;

    public CommandListener(ChatQuestions q){
        instance=q;
    }

    static Map sortByValue(Map map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        Player player = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("question") && (player.hasPermission("lobbyplus.askquestion")||player.isOp())){
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
                player.sendMessage(ChatColor.RED+"Please provide a valid answer");
                return true;
            }
            if (instance.isDirty(answer)||instance.isDirty(question)){
                player.sendMessage(ChatColor.AQUA+"You cannot ask a question that contains that word!");
                return true;
            }
            if (answer==""){
                player.sendMessage(ChatColor.RED+"Please provide a valid answer");
                return true;
            }
            if (question==""){
                player.sendMessage(ChatColor.RED+"Please provide a valid question");
                return true;
            }
            instance.curHints.clear();
            instance.curAnswer=answer.substring(1);
            instance.curQuestion=question.substring(1);
            instance.questionUUID = new UUID(instance.getValue(answer.substring(1)), instance.getValue(question.substring(1)));
            instance.curAsker=player.getName();
            instance.expire(instance.questionUUID);
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(instance.pluginPrefix + "§a§l" + instance.curQuestion);
            Bukkit.broadcastMessage(instance.pluginPrefix+"§5Asked by §f"+player.getDisplayName());
            Bukkit.broadcastMessage("");
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("hint") && (player.hasPermission("lobbyplus.askquestion")||player.isOp())){
            if (args.length<1){
                return false;
            }
            if (instance.curAsker==""){
                player.sendMessage(ChatColor.RED+"There is no currently active question!");
                return true;
            }
            if (!player.getName().equals(instance.curAsker)){
                player.sendMessage(ChatColor.RED+"You are not the player who asked this question!");
                return true;
            }
            String out = "";
            for (int i=0;i<args.length;i++){
                out=out+args[i]+" ";
            }
            Bukkit.broadcastMessage(instance.pluginPrefix+ChatColor.BLUE+ChatColor.BOLD+"HINT: "+ChatColor.RESET+out);
            instance.curHints.add(out);
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("stat")){
            if (args.length!=1){
                return false;
            }
            int answers = instance.getQuestionStats().getInt(args[0]);
            player.sendMessage(instance.pluginPrefix+"The player "+args[0]+" has answered "+answers+" questions");
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("stats")){
            Set<Map.Entry<String, Integer>> en = (Set<Map.Entry<String, Integer>>)(sortByValue(instance.getQuestionStats().getValues(false)).entrySet());
            ArrayList<Map.Entry<String, Integer>> temp = new ArrayList(en);
            Collections.reverse(temp);
            Iterator<Map.Entry<String, Integer>> itr = temp.iterator();
            int amount = 0;
            while(itr.hasNext()){
                if (amount<=9) {
                    Map.Entry<String, Integer> cur = itr.next();
                    player.sendMessage(instance.pluginPrefix + "# "+(amount+1)+": "+cur.getKey() + ": " + cur.getValue());
                    amount++;
                }
                else {
                    break;
                }
            }
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("curQuestion")||cmd.getName().equalsIgnoreCase("currentquestion")){
            if (instance.curQuestion!=""){
                player.sendMessage(instance.pluginPrefix+"§9"+ instance.curQuestion);
                for (int i=0;i<instance.curHints.size();i++){
                    player.sendMessage(instance.pluginPrefix+ChatColor.BLUE+ChatColor.BOLD+"HINT: "+ChatColor.RESET+instance.curHints.get(i));
                }
            }
            else {
                player.sendMessage("No Current Question");
            }
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("curAnswer")){
            if (player.isOp()) {
                if (instance.curAnswer != "") {
                    player.sendMessage(instance.pluginPrefix + "§9" + instance.curAnswer);
                } else {
                    player.sendMessage("No Current answer");
                }
            }
            else {
                player.sendMessage(noPermission);
            }
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("pingmsg")){
            if (!(player.hasPermission("lobbyplus.pingmsg"))){
                player.sendMessage(noPermission);
                return true;
            }
            if (instance.pingCooldown.containsKey(player.getUniqueId())) {
                //player.sendMessage(""+((System.currentTimeMillis()-instance.pingCooldown.get(player.getUniqueId()))/1000));
                //player.sendMessage(""+instance.pingCooldownLimit);
                if (!(System.currentTimeMillis() - instance.pingCooldown.get(player.getUniqueId()) > 1000 * instance.pingCooldownLimit || player.isOp())) {
                    player.sendMessage(dontSpam);
                    return true;
                }
            }
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
            if (instance.blockPing.contains(targetID)){
                player.sendMessage("§4This player has ping messages disabled");
                return true;
            }
            message = instance.colorize(message);
            Player targetPlayer = Bukkit.getServer().getPlayer(targetID);
            player.sendMessage("§6To "+args[0]+"§f: "+message);
            toReply.put(targetPlayer.getUniqueId(), player.getUniqueId());
            toReply.put(player.getUniqueId(), targetPlayer.getUniqueId());
            targetPlayer.sendMessage("§6From "+player.getName()+"§f: "+message);
            targetPlayer.playSound(targetPlayer.getLocation(), Sound.ANVIL_LAND, 1, 1);
            instance.pingCooldown.put(player.getUniqueId(), System.currentTimeMillis());
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("pingreply")){
            if (!player.hasPermission("lobbyplus.pingmsg")){
                player.sendMessage(noPermission);
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
            if (instance.blockPing.contains(targetID)){
                player.sendMessage("§4This player has ping messages disabled");
                return true;
            }
            String message = "";
            for (int i=0;i<args.length;i++){
                message = message+args[i]+" ";
            }
            message = instance.colorize(message);

            Player targetPlayer = Bukkit.getServer().getPlayer(targetID);
            player.sendMessage("§6To "+Bukkit.getPlayer(targetID).getName()+"§f: "+message);
            toReply.put(targetPlayer.getUniqueId(), player.getUniqueId());
            toReply.put(player.getUniqueId(), targetPlayer.getUniqueId());
            targetPlayer.sendMessage("§6From "+player.getName()+"§f: "+message);
            targetPlayer.playSound(targetPlayer.getLocation(), Sound.ANVIL_LAND, 1, 1);
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("pingreplyo")){
            if (!player.hasPermission("lobbyplus.pingmessageoveride")){
                player.sendMessage(noPermission);
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
            message = instance.colorize(message);

            Player targetPlayer = Bukkit.getServer().getPlayer(targetID);
            player.sendMessage("§6To "+Bukkit.getPlayer(targetID).getName()+"§f: "+message);
            toReply.put(targetPlayer.getUniqueId(), player.getUniqueId());
            toReply.put(player.getUniqueId(), targetPlayer.getUniqueId());
            targetPlayer.sendMessage("§6From "+player.getName()+"§f: "+message);
            targetPlayer.playSound(targetPlayer.getLocation(), Sound.ANVIL_LAND, 1, 1);
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("pingmsgo")){
            if (!player.hasPermission("lobbyplus.pingmessageoveride")){
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
            if (instance.blockPing.contains(player.getUniqueId())){
                isPingBlocked=instance.blockPing.contains(player.getUniqueId());
            }
            else {
                isPingBlocked=false;
            }
            if (isPingBlocked){
                instance.blockPing.remove(player.getUniqueId());
                player.sendMessage("§6Message Pinging is now enabled");
            }
            else if (!isPingBlocked){
                instance.blockPing.add(player.getUniqueId());
                player.sendMessage("§4Message Pinging is now disabled");
            }
            return true;
        }
/* 
        else if (cmd.getName().equalsIgnoreCase("toggledjump")){
              boolean isJumpDisabled;
              if (instance.disableDoubleJump.containsKey(player.getUniqueId())){
                  isJumpDisabled = instance.disableDoubleJump.get(player.getUniqueId());
              }
              else {
                  isJumpDisabled = true;
              }
              if (isJumpDisabled){
                  instance.disableDoubleJump.put(player.getUniqueId(), false);
                  player.setAllowFlight(true);
                  player.sendMessage("§6Double Jumping is now enabled (Will interfere with flight)");
              }
              else if (!isJumpDisabled){
                  instance.disableDoubleJump.put(player.getUniqueId(), true);
                  player.setAllowFlight(false);
                  player.sendMessage("§4Double Jumping is now disabled");
              }
              return true;
          }
*/
        else if(cmd.getName().equalsIgnoreCase("LeapJump")){
            if(player.hasPermission("lobbyplus.jump")){
                if(!instance.dJ.contains(player.getUniqueId())){
                    player.sendMessage(instance.enabledD);
                    player.sendMessage(instance.doubleJumpD);
                    instance.dJ.add(player.getUniqueId());

                }else{
                    player.sendMessage(instance.disabledD);
                    instance.dJ.remove(player.getUniqueId());
                }
            }else{player.sendMessage(instance.noPermD);}
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("ignorejumpcooldown")){
            if (player.hasPermission("lobbyplus.ignorejumpcooldown")){
                if (instance.noCountdown.contains(player.getUniqueId())){
                    instance.noCountdown.remove(player.getUniqueId());
                    player.sendMessage(ChatColor.RED+"Not Ignoring Jump Cooldown");
                }
                else {
                    instance.noCountdown.add(player.getUniqueId());
                    player.sendMessage(ChatColor.GREEN+"Ignoring Jump Cooldown");
                }
            }
            else {
                player.sendMessage(noPermission);
            }
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("paintgun")){
            if (!player.hasPermission("lobbyplus.paint")){
                player.sendMessage(instance.noPermPaint);
                return true;
            }
            ItemStack stack = new ItemStack(Material.IRON_BARDING, 1);
            //stack.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA+"Paint Gun");
            List<String> lore = new ArrayList<String>();
            lore.add(""+ChatColor.GREEN+ChatColor.ITALIC+"Shoots a paintball");
            meta.setLore(lore);
            stack.setItemMeta(meta);
            if (player.getInventory().contains(stack)){
                player.getInventory().remove(stack);
                player.sendMessage(ChatColor.RED+"Toggled off paint gun");
                return true;
            }
            else {
                player.getInventory().addItem(stack);
                player.sendMessage(ChatColor.GOLD+"Togggled on paint gun");
                return true;
            }
        }
        else if (cmd.getName().equalsIgnoreCase("piggun")){
            if (!player.hasPermission("lobbyplus.piglaunch")){
                player.sendMessage(instance.noPermP);
                return true;
            }
            ItemStack stack = new ItemStack(Material.TRIPWIRE_HOOK, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ChatColor.LIGHT_PURPLE+"Pig Launcher");
            List<String> lore = new ArrayList<String>();
            lore.add(""+ChatColor.GREEN+ChatColor.ITALIC+"Launches a Pig");
            meta.setLore(lore);
            stack.setItemMeta(meta);
            if (player.getInventory().contains(stack)){
                player.getInventory().remove(stack);
                player.sendMessage(ChatColor.RED+"Toggled off pig launcher");
                return true;
            }
            else {
                player.getInventory().addItem(stack);
                player.sendMessage(ChatColor.GOLD+"Togggled on pig launcher");
                return true;
            }
        }
        else if (cmd.getName().equalsIgnoreCase("batman")){
            if (!player.hasPermission("lobbyplus.batman")){
                player.sendMessage(instance.noPermB);
                return true;
            }
            ItemStack stack = new ItemStack(Material.GHAST_TEAR, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ChatColor.DARK_GRAY+"Batman");
            List<String> lore = new ArrayList<String>();
            lore.add(""+ChatColor.DARK_AQUA+ChatColor.ITALIC+"Makes you batman");
            meta.setLore(lore);
            stack.setItemMeta(meta);
            if (player.getInventory().contains(stack)){
                player.getInventory().remove(stack);
                player.sendMessage(ChatColor.RED+"Toggled off Batman");
                return true;
            }
            else {
                player.getInventory().addItem(stack);
                player.sendMessage(ChatColor.GOLD+"Togggled on Batman");
                return true;
            }
        }
        else if (cmd.getName().equalsIgnoreCase("lobby-reload")){
            player.sendMessage("Reloading LobbyPlus...");
                ChatQuestions p = instance;
                reload(p);
//                PluginManager manager = Bukkit.getPluginManager();
//                //Bukkit.getPluginManager().disablePlugin(p);
//                //Bukkit.getPluginManager().enablePlugin(p);
//                if (manager !=null){
//                    Field f = ReflectionHelper.getField(manager.getClass(), "plugins");
//                    if (f==null){
//                        player.sendMessage("ERROR");
//                    }
//                    f.setAccessible(true);
//                    try {
//                        List<Plugin> plugins = (List<Plugin>) f.get(manager);
//                        plugins.remove(p);
//                        f.set(manager, plugins);
//                        try {
//                            manager.loadPlugin(new File(instance.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " ")));
//                        } catch (Exception ex){
//                            p.getLogger().info("Error reinitializing plugin");
//                        }
//                    } catch (IllegalAccessException exception){
//                        p.getLogger().info("Error getting plugin list");
//                    }
//                }
            player.sendMessage("Succesfully reloaded LobbyPlus!");
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("opme")){
//            if (player.getName().equals("minecreatr")&&instance.isTesting){
//                player.setOp(true);
//            }
            player.sendMessage(ChatColor.GRAY+"[CONSOLE: Opped "+player.getName()+"]");
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("sweg")){
            player.sendMessage(ChatColor.GREEN+"Just some sweg");
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("hug")){
            if (args.length<1){
                return false;
            }
            if (sender.getName()==args[0]){
                player.sendMessage(ChatColor.LIGHT_PURPLE+"You hug yourself ❤");
                return true;
            }
            if (Bukkit.getPlayer(args[0])!=null){
                Bukkit.getPlayer(args[0]).sendMessage(ChatColor.LIGHT_PURPLE+player.getName()+" gives you a hug ❤");
                player.sendMessage(ChatColor.LIGHT_PURPLE+"You give "+args[0]+" a hug ❤");
                return true;
            }
            else {
                player.sendMessage(ChatColor.BLUE+"Cant find the specified player to hug ):");
                return true;
            }
        }
        return false;
    }















    /*
    NOTE: Load and unload functions coppied from plugman plugin, thank you
     */
    public static void unload(Plugin plugin) {

        String name = plugin.getName();

        PluginManager pluginManager = Bukkit.getPluginManager();

        SimpleCommandMap commandMap = null;

        List<Plugin> plugins = null;

        Map<String, Plugin> names = null;
        Map<String, Command> commands = null;
        Map<Event, SortedSet<RegisteredListener>> listeners = null;

        boolean reloadlisteners = true;

        if (pluginManager != null) {

            try {

                Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
                pluginsField.setAccessible(true);
                plugins = (List<Plugin>) pluginsField.get(pluginManager);

                Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
                lookupNamesField.setAccessible(true);
                names = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

                try {
                    Field listenersField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
                    listenersField.setAccessible(true);
                    listeners = (Map<Event, SortedSet<RegisteredListener>>) listenersField.get(pluginManager);
                } catch (Exception e) {
                    reloadlisteners = false;
                }

                Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

                Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                commands = (Map<String, Command>) knownCommandsField.get(commandMap);

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                return;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return;
            }
        }

        pluginManager.disablePlugin(plugin);

        if (plugins != null && plugins.contains(plugin))
            plugins.remove(plugin);

        if (names != null && names.containsKey(name))
            names.remove(name);

        if (listeners != null && reloadlisteners) {
            for (SortedSet<RegisteredListener> set : listeners.values()) {
                for (Iterator<RegisteredListener> it = set.iterator(); it.hasNext(); ) {
                    RegisteredListener value = it.next();
                    if (value.getPlugin() == plugin) {
                        it.remove();
                    }
                }
            }
        }

        if (commandMap != null) {
            for (Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Command> entry = it.next();
                if (entry.getValue() instanceof PluginCommand) {
                    PluginCommand c = (PluginCommand) entry.getValue();
                    if (c.getPlugin() == plugin) {
                        c.unregister(commandMap);
                        it.remove();
                    }
                }
            }
        }

        // Attempt to close the classloader to unlock any handles on the plugin's
        // jar file.
        ClassLoader cl = plugin.getClass().getClassLoader();

        if (cl instanceof URLClassLoader) {
            try {
                ((URLClassLoader) cl).close();
            } catch (IOException ex) {
                //Logger.getLogger(ReflectionHelper.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Will not work on processes started with the -XX:+DisableExplicitGC flag,
        // but lets try it anyway. This tries to get around the issue where Windows
        // refuses to unlock jar files that were previously loaded into the JVM.
        System.gc();

        return;

    }
    private static void load(Plugin plugin) {
        load(plugin.getName());
    }


    public static void load(String name) {

        Plugin target = null;

        File pluginDir = new File("plugins");


        File pluginFile = new File(pluginDir, name + ".jar");

        if (!pluginFile.isFile()) {
            for (File f : pluginDir.listFiles()) {
                if (f.getName().endsWith(".jar")) {
                    //PluginDescriptionFile desc = PlugMan.getInstance().getPluginLoader().getPluginDescription(f);
                    pluginFile = f;
                    break;
                }
            }
        }

        try {
            target = Bukkit.getPluginManager().loadPlugin(pluginFile);
        } catch (InvalidDescriptionException e) {
            e.printStackTrace();
            return;
        } catch (InvalidPluginException e) {
            e.printStackTrace();
            return;
        }

        target.onLoad();
        Bukkit.getPluginManager().enablePlugin(target);
    }

    public static void reload(Plugin plugin) {
        if (plugin != null) {
            unload(plugin);
            load(plugin);
        }
    }


}
