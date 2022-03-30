package ru.xfenilafs.lobby.discord;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import ru.redline.core.bukkit.util.ChatUtil;
import ru.xfenilafs.lobby.CodeService;
import ru.xfenilafs.lobby.Main;

import java.util.Objects;

public class DiscordListener extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (!event.getChannel().getId().equals("768194563201695774")) {
            return;
        }

        String[] args = event.getMessage().getContentRaw().split(" ");

        if (args[0].equalsIgnoreCase("/code")) {
            String code = args[1];

            if (!CodeService.checkCode(code)) {
                return;
            }

            Main.getInstance().getGuild()
                    .modifyNickname(Objects.requireNonNull(event.getMember()), Bukkit.getPlayer(CodeService.getPlayer(code)).getName())
                    .queue();

            DiscordManager.setDiscordGroup(event.getMember(), Bukkit.getPlayer(CodeService.getPlayer(code)));
            DiscordManager.setDiscordId(CodeService.getPlayer(code), event.getMember().getId());

            ChatUtil.sendBungeeMessage(CodeService.getPlayer(code), "§8[§bКоманды§8] §fВы успешно прошли авторизацию в дискорде!");
            CodeService.removeCode(code);
        }

        event.getMessage().delete().queue();
    }
}
