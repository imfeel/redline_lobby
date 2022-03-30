package ru.xfenilafs.lobby.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.redline.core.bukkit.command.BukkitCommand;
import ru.redline.core.bukkit.command.annotation.CommandPermission;
import ru.redline.core.bukkit.util.ChatUtil;
import ru.xfenilafs.lobby.database.GamePlayer;

@CommandPermission(permission = "lobby.admin")
public class TitleCommand extends BukkitCommand<Player> {

    public TitleCommand() {
        super("title");
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        GamePlayer gamePlayer = GamePlayer.getPlayer(args[0]);

        if (args.length < 2) {
            ChatUtil.sendMessage(player, "&8[&bКоманды&8] &c/title <игрок> <титул> &f- выдать личный титул.");
            return;
        }

        if (gamePlayer == null) {
            ChatUtil.sendMessage(player, "&8[&bКоманды&8] &fДанный игрок не зарегистрирован на проекте.");
            return;
        }

        if (Bukkit.getPlayer(args[0]) == null) {
            ChatUtil.sendMessage(player, "&8[&bКоманды&8] &fДанный игрок не в сети.");
            return;
        }

        if (!args[1].startsWith("&")) {
            ChatUtil.sendMessage(player, "&8[&bКоманды&8] &fНеизвестный формат титула.");
            return;
        }

        gamePlayer.setTitleLevel(args[1], 1);
        ChatUtil.sendMessage(player, "&8[&bКоманды&8] &fВы выдали игроку &a%s &fтитул %s&f.",
                args[0], ChatUtil.color(args[1]));
    }
}
