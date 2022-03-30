package ru.xfenilafs.lobby.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.xfenilafs.lobby.Main;
import ru.xfenilafs.lobby.config.Config;

import java.io.File;
import java.io.IOException;

public class ConfigUtils {
    public static Location loadLocationFromConfigurationSection(ConfigurationSection section) {
        if (section == null)
            return null;
        if (Bukkit.getWorld(section.getString("world")) == null)
            return null;
        World world = Bukkit.getWorld(section.getString("world"));
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static void saveLocationToConfigurationSection(Location location, Config config, String sectionName) {
        ConfigurationSection section;
        if (config.getConfiguration().getConfigurationSection(sectionName) != null)
            section = config.getConfiguration().getConfigurationSection(sectionName);
        else
            section = config.getConfiguration().createSection(sectionName);
        section.set("world", location.getWorld().getName());
        section.set("x", fixDouble(1, location.getX()));
        section.set("y", fixDouble(1, location.getY()));
        section.set("z", fixDouble(1, location.getZ()));
        section.set("yaw", fixDouble(1, location.getYaw()));
        section.set("pitch", fixDouble(1, location.getPitch()));
        config.save();
    }

    public static Double fixDouble(int i, double d) {
        return Double.valueOf(String.format("%." + i + "f", d).replace(",", "."));
    }

    public static FileConfiguration loadConfig(File file) {
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        return yamlConfiguration;
    }

    public static void saveConfig(FileConfiguration config, String fileName) {
        try {
            config.save(new File(Main.getInstance().getDataFolder(), fileName));
        } catch (IOException exception) {
        }

    }

    public static void saveDefaults(String fileName) {
        try {
            File file = new File(Main.getInstance().getDataFolder(), fileName);
            if (!(new File(Main.getInstance().getDataFolder(), fileName)).exists()) {
                file.createNewFile();
            }
        } catch (IOException exception) {
        }

    }
}

