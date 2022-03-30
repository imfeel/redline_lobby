package ru.xfenilafs.lobby.commands;

import org.bukkit.entity.Player;
import ru.redline.core.bukkit.command.BukkitCommand;
import ru.redline.core.bukkit.util.ChatUtil;
import ru.xfenilafs.lobby.CodeService;
import ru.xfenilafs.lobby.discord.DiscordManager;

public class DiscordCommand extends BukkitCommand<Player> {

    public DiscordCommand() {
        super("discord");
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        if (DiscordManager.isCreated(player.getName())) {
            ChatUtil.sendMessage(player, "&8[&bКоманды&8] &fВаш аккаунт уже привязан к &9Discord&f.");
            return;
        }

        if (CodeService.isCode(player.getName())) {
            ChatUtil.sendMessage(player, "&8[&bКоманды&8] &cНа данный момент активен старый код!");
            return;
        }

        String code = CodeService.generateNewCode(player.getName());
        ChatUtil.sendMessage(player, "&8[&bКоманды&8] &fВаш код для авторизации в &9Discord: &a%s", code);
        ChatUtil.sendMessage(player, "&8[&bКоманды&8] &fДанный код будет действовать лишь минуту, если вы не успели его ввести, то тогда напишите команду повторно!", code);
    }
}
