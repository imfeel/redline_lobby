package ru.xfenilafs.lobby.database;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.xfenilafs.lobby.Main;
import ru.xfenilafs.lobby.utils.ItemSerialization;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@SuppressWarnings("Duplicates")
@Getter
@Setter
public class GamePlayer {
    public static Map<String, GamePlayer> players = new ConcurrentHashMap<>();

    private String name;
    private Map<String, Integer> titles;

    public GamePlayer(String name) {
        this.name = name;
        load();
    }

    private void load() {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            if (playerExists()) {
                ResultSet resultSet = Main.getInstance().getDatabaseManager().getResult("SELECT * FROM lobby_players WHERE name='" + name + "'");
                try {
                    if (resultSet.next()) {
                        this.titles = new LinkedHashMap<>();
                        String[] upgradeLog = resultSet.getString("titles").split(",");
                        for (String upgrade : upgradeLog) {
                            String[] upgradeData = upgrade.split("=");
                            if (upgradeData.length == 2)
                                this.titles.put(upgradeData[0], Integer.valueOf(upgradeData[1]));
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            } else {
                this.titles = new LinkedHashMap<>();
                Main.getInstance().getDatabaseManager().update("INSERT INTO lobby_players(name, titles) VALUES('" + name + "', '" + null + "')");
            }
        });

        if (savedInventoryExists()) {
            ResultSet invQueryResult = Main.getInstance().getDatabaseManager().getResult("SELECT * FROM `lobby_inventory` WHERE name='" + name + "'");
            try {
                if (invQueryResult.next()) {
                    String encodedInvContent = invQueryResult.getString("inventory");
                    ItemStack[] invContents = ItemSerialization.itemStackArrayFromBase64(encodedInvContent);
                    Player p = getPlayer();
                    for (int i = 0; i < invContents.length; i++) {
                        p.getInventory().setItem(i, invContents[i]);
                    }
                }
            } catch (SQLException | IOException throwables) {
                throwables.printStackTrace();
            }
        } else {
            String encodedInventory = ItemSerialization.toBase64(this.getPlayer().getInventory());
            Main.getInstance().getDatabaseManager().update("INSERT INTO lobby_inventory(name, inventory) VALUES('" + name + "', '" + encodedInventory + "')");
        }

        players.put(name, this);
    }

    public boolean playerExists() {
        ResultSet resultSet = Main.getInstance().getDatabaseManager().getResult("SELECT * FROM `lobby_players` WHERE name='" + name + "'");
        try {
            if (resultSet.next())
                return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean savedInventoryExists() {
        ResultSet resultSet = Main.getInstance().getDatabaseManager().getResult("SELECT * FROM `lobby_inventory` WHERE name='" + name + "'");
        try {
            if (resultSet.next())
                return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public void save() {
        if (Bukkit.getPlayer(name) == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            String titlesLog = "";
            for (String title : titles.keySet())
                titlesLog = titlesLog + "," + title + "=" + titles.get(title);
            titlesLog = titlesLog.replaceFirst(",", "");

            Main.getInstance().getDatabaseManager().update("UPDATE `lobby_players` SET " +
                    "titles='" + titlesLog + "' " +
                    "WHERE name='" + name + "'"
            );

        });

        String encodedInventory = ItemSerialization.toBase64(this.getPlayer().getInventory());
        Main.getInstance().getDatabaseManager().update("UPDATE `lobby_inventory` SET " +
                "inventory='" + encodedInventory + "' " +
                "WHERE name='" + name + "'"
        );
    }

    public void unload() {
        save();
        players.remove(name);
    }

    public void sendActionBar(String actionBar, Object... objects) {
        if (getPlayer() == null)
            return;
        getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(String.format(actionBar, objects)).create());
    }

    public Map<String, Integer> getStatLog() {
        return titles;
    }

    public static GamePlayer getPlayer(String name) {
        return players.get(name);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(name);
    }

    public int getTitleLevel(String title) {
        if (titles.containsKey(title))
            return titles.get(title);
        return 0;
    }

    public void addTitleLevel(String title, int level) {
        if (titles.containsKey(title))
            titles.put(title, titles.get(title) + level);
        else
            titles.put(title, level);
    }

    public void setTitleLevel(String title, int level) {
        titles.put(title, level);
    }
}
