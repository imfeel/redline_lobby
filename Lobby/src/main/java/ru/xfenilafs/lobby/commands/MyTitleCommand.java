package ru.xfenilafs.lobby.commands;

import org.bukkit.entity.Player;
import ru.redline.core.bukkit.command.BukkitCommand;
import ru.redline.core.bukkit.util.ChatUtil;
import ru.xfenilafs.lobby.database.GamePlayer;
import ru.xfenilafs.lobby.menus.game.TitlesMenu;

public class MyTitleCommand extends BukkitCommand<Player> {

    public MyTitleCommand() {
        super("mytitles");
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        GamePlayer gamePlayer = GamePlayer.getPlayer(player.getName());

        if (gamePlayer.getTitles().isEmpty()) {
            ChatUtil.sendMessage(player, "&8[&bТитулы&8] &fУ вас нет личных титулов.");
        } else {
            new TitlesMenu().open(GamePlayer.getPlayer(player.getName()));
        }
    }

}