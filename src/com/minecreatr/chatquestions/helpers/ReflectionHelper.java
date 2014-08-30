package com.minecreatr.chatquestions.helpers;

import com.minecreatr.chatquestions.ChatQuestions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.plugin.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created on 8/29/2014
 */
public class ReflectionHelper {

    public static Field getField(Class c, String name){
        try {
            return c.getField(name);
        } catch (NoSuchFieldException exception){
            try {
                ChatQuestions.getInstance().getLogger().info("Error getting field " + name);
                exception.printStackTrace();
            } catch (ClassNotFoundException ex){
                System.out.println("Well this is a pickle were in isn't it");
            }
            return null;
        }
    }


}
