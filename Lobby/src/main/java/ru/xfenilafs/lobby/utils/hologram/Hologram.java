package ru.xfenilafs.lobby.utils.hologram;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Consumer;

public interface Hologram {

    void addLine(String line);

    void addItem(Material material);

    void modifyLine(int index, String line);

    void spawn();

    void addReceiver(Player player);

    void remove();

    void removeReceiver(Player player);

    void setLocation(Location location);

    void refreshHologram();

}