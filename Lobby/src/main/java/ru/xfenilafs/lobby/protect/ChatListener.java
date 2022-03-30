package ru.xfenilafs.lobby.protect;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.redline.core.bukkit.util.NumberUtil;
import ru.xfenilafs.lobby.Main;

import java.util.HashMap;

public class ChatListener implements Listener {

    HashMap<String, Long> chat = new HashMap<>();
    HashMap<String, Long> commands = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChatSlow(AsyncPlayerChatEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            String name = player.getName();
            if (ProtectListener.owners.contains(name))
                return;
            if (chat.containsKey(name)) {
                if (chat.get(name) - System.currentTimeMillis() <= 0) {
                    chat.remove(name);
                    return;
                }
                player.sendMessage("§7[§c!§7] §fПодождите ещё §c" + NumberUtil.getTime(((chat.get(name) / 1000 - System.currentTimeMillis() / 1000) * 1000)));
                event.setCancelled(true);
                return;
            }
            long time = Main.getInstance().getConfigManager().getConfig().getConfiguration().getLong("chat.slow-chat.chat") * 1000L;
            chat.put(name, System.currentTimeMillis() + time);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommandSlow(PlayerCommandPreprocessEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            String name = player.getName();
            if (ProtectListener.owners.contains(name))
                return;
            if (commands.containsKey(name)) {
                if (commands.get(name) - System.currentTimeMillis() <= 0) {
                    commands.remove(name);
                    return;
                }
                player.sendMessage("§7[§c!§7] §fПодождите ещё §c" + NumberUtil.getTime(((commands.get(name) / 1000 - System.currentTimeMillis() / 1000) * 1000)));
                event.setCancelled(true);
                return;
            }
            long time = Main.getInstance().getConfigManager().getConfig().getConfiguration().getLong("chat.slow-chat.command") * 1000L;
            commands.put(name, System.currentTimeMillis() + time);
        }
    }

}