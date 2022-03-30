package ru.xfenilafs.lobby.commands;

import org.bukkit.entity.Player;
import ru.redline.core.bukkit.command.BukkitCommand;
import ru.redline.core.bukkit.util.ChatUtil;
import ru.xfenilafs.lobby.Main;
import ru.xfenilafs.lobby.menus.Menu;

public class MenuCommand extends BukkitCommand<Player> {

    public MenuCommand() {
        super("menu");
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        switch(args.length) {
            case 1:
                if (!player.hasPermission("core.admin")) {
                    ChatUtil.sendMessage(player, "&8[&bКоманды&8] §fДанная команда вам недоступна.");
                } else if (args[0].equals("list")) {
                    ChatUtil.sendMessage(player, "&8[&bКоманды&8] §fСписок меню: ");
                    if (Menu.menus.isEmpty()) {
                        ChatUtil.sendMessage(player, "&8[&bКоманды&8] §fНа этом сервере пока что нет меню.");
                        return;
                    }

                    Menu.menus.keySet().forEach((menu) -> {
                        ChatUtil.sendMessage(player, "§7- §a" + menu);
                    });
                } else if (args[0].equals("reload")) {
                    Menu.menus.clear();
                    Main.getInstance().reloadMenus();
                    ChatUtil.sendMessage(player, "&8[&bКоманды&8] §fВы успешно перезагрузили все меню.");
                }
                break;
            case 2:
                if (args[0].equals("open")) {
                    String id = args[1];
                    if (Menu.getMenu(id) != null) {
                        Menu.getMenu(id).open(player);
                    } else {
                        ChatUtil.sendMessage(player, "&8[&bКоманды&8] §fМеню с данным id не найдено.");
                    }
                }
                break;
            default:
                if (player.hasPermission("core.admin")) {
                    ChatUtil.sendMessage(player, "&8[&bКоманды&8] §cИспользование: ");
                    ChatUtil.sendMessage(player, "&8[&bКоманды&8] §c/menu list §f- посмотреть список всех меню этого сервера");
                    ChatUtil.sendMessage(player, "&8[&bКоманды&8] §c/menu open <id> §f- открыть меню");
                    ChatUtil.sendMessage(player, "&8[&bКоманды&8] §c/menu reload §f- перезагрузить все меню этого сервера");
                } else {
                    ChatUtil.sendMessage(player, "&8[&bКоманды&8] §fИспользование: §c/menu open <id>§f.");
                }
        }
    }
}
