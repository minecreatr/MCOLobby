package com.minecreatr.chatquestions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created on 8/27/2014
 */
public class DisguisedBlock {

    private Material type;
    private byte meta;
    private Location location;
    private long timeCreated;
    private Material originalType;
    private byte originalMeta;

    public DisguisedBlock(Material material, byte b, Location loc){
        type=material;
        meta=b;
        location=loc;
        timeCreated = System.currentTimeMillis();
        originalType = location.getBlock().getType();
        originalMeta = location.getBlock().getState().getRawData();
    }

    public Material getType(){
        return this.type;
    }
    public byte getMeta(){
         return meta;
     }

    public Location getLocation(){
        return location;
    }

    public void render(Player player){
        player.sendBlockChange(location, type, meta);
    }

    public void renderOld(Player player){
        player.sendBlockChange(location, originalType, originalMeta);
    }

    public UUID getID(){
        return generateID(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    //generates a UUID from 3 coordinate integers
    public static UUID generateID(long x, long y, long z){
        return new UUID(x*y, y*z);
    }

    //gets greatest command factor of two longs as an int
    public static int getGCF(long a, long b){
        return BigInteger.valueOf(a).gcd(BigInteger.valueOf(b)).intValue();
    }

    public static Location getLocFromIDAndWorld(UUID id, World world){
        double y = getGCF(id.getLeastSignificantBits(), id.getMostSignificantBits());
        double x = (double)id.getMostSignificantBits()/y;
        double z = (double)id.getLeastSignificantBits()/y;
        return new Location(world, x, y, z);
    }

    public long getTimeCreated(){
        return this.timeCreated;
    }
}
