package ru.xfenilafs.lobby.protect;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import ru.xfenilafs.lobby.Main;

import java.util.*;

public class ProtectListener implements Listener {

    public static final List<String> owners = new ArrayList<>();
    private static final List<String> permissions = new ArrayList<>();
    private static final List<String> whiteListPermissions = new ArrayList<>();
    private static final List<String> permissionsContains = new ArrayList<>();
    private static final List<String> commandsBlackList = new ArrayList<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (getConfig().getBoolean("owners.settings.join")) {
            if (owners.contains(player.getName())) return;
            checkPlayer(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (getConfig().getBoolean("owners.settings.quit")) {
            if (owners.contains(player.getName())) return;
            checkPlayer(player);
        }
    }

    @EventHandler
    public void onCommandEasyBackup(PlayerCommandPreprocessEvent event) {
        if(commandsBlackList.contains(event.getMessage()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (getConfig().getBoolean("owners.settings.command")) {
            if (owners.contains(player.getName())) return;
            Set<PermissionAttachmentInfo> sets = player.getEffectivePermissions();
            for (PermissionAttachmentInfo permissionAttachmentInfo : sets) {
                if(permissionAttachmentInfo.getValue()) {
                    String pex = permissionAttachmentInfo.getPermission().toLowerCase();
                    if (permissionsContains.contains(pex.split("\\.")[0]) && !whiteListPermissions.contains(pex)) {
                        System.out.println(pex);
                        dispatch(player);
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            if (player.isOp() || player.hasPermission("*")) {
                dispatch(player);
                event.setCancelled(true);
                return;
            }
            for (String s : permissions) {
                if (player.hasPermission(s)) {
                    dispatch(player);
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }


    private static FileConfiguration getConfig() {
        return Main.getInstance().getConfigManager().getProtectConfig().getConfiguration();
    }

    public static void loadOwners() {
        FileConfiguration config = getConfig();
        owners.addAll(config.getStringList("owners.players"));
        permissions.addAll(config.getStringList("owners.permissions"));
        permissionsContains.addAll(config.getStringList("owners.permissions-contains"));
        whiteListPermissions.addAll(config.getStringList("owners.permissions-whitelist"));
        commandsBlackList.addAll(config.getStringList("owners.blackList-commands"));
    }

    private void dispatch(Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getConfig().getString("owners.command")
                .replace("%name%", player.getName())
                .replace("%messageIP%", getConfig().getString("owners.message-ban-ip")));
    }

    private void checkPlayer(Player player) {
        Set<PermissionAttachmentInfo> sets = player.getEffectivePermissions();
        for (PermissionAttachmentInfo permissionAttachmentInfo : sets) {
            if(permissionAttachmentInfo.getValue()) {
                String pex = permissionAttachmentInfo.getPermission().toLowerCase();
                if (permissionsContains.contains(pex.split("\\.")[0]) && !whiteListPermissions.contains(pex)) {
                    System.out.println(pex);
                    dispatch(player);
                    return;
                }
            }
        }
        if (player.isOp() || player.hasPermission("*")) {
            dispatch(player);
            return;
        }
        for (String s : permissions) {
            if (player.hasPermission(s)) {
                dispatch(player);
                break;
            }
        }
    }

}