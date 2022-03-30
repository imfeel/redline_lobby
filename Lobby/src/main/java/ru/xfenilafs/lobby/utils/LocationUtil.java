package ru.xfenilafs.lobby.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@UtilityClass
public class LocationUtil {

    public static Location fromString(String in) {
        String[] info = in.split(" ");

        if (info.length == 4)
            return new Location(Bukkit.getWorld(info[0]), parse(info[1]), parse(info[2]), parse(info[3]));
        else if (info.length == 6)
            return new Location(Bukkit.getWorld(info[0]), parse(info[1]), parse(info[2]), parse(info[3]), (float) parse(info[4]), (float) parse(info[5]));
        throw new IllegalArgumentException("cannot parse location");
    }

    public static Location fromString(String in, Location def) {
        try {
            return fromString(in);
        } catch (Exception e) {
            return def;
        }
    }

    public static String toString(Location in) {
        return String.format(
                "%s %s %s %s %s %s",
                in.getWorld().getName(), in.getX(), in.getY(), in.getZ(), in.getYaw(), in.getPitch()
        );
    }

    private static double parse(String in) {
        return Double.parseDouble(in);
    }
}
