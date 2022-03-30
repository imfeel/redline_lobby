package ru.xfenilafs.lobby.menus.game;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.redline.core.bukkit.ApiManager;
import ru.redline.core.bukkit.util.ChatUtil;
import ru.redline.core.global.group.player.User;
import ru.xfenilafs.lobby.Main;
import ru.xfenilafs.lobby.database.GamePlayer;

public class TitlesMenu implements IGui {
    private Inventory inventory;
    private int page;
    private GamePlayer gamePlayer;

    public Inventory getInventory() {
        inventory = Bukkit.createInventory(this, 36, "§aЛичные титулы");
        fill(gamePlayer);
        return inventory;
    }

    public void fill(GamePlayer gamePlayer) {
        inventory.clear();

        if (!gamePlayer.getTitles().isEmpty())
            gamePlayer.getTitles().forEach((key, value) -> inventory.addItem(
                    ApiManager.newItemBuilder(Material.SKULL_ITEM)
                            .setName(ChatUtil.color(key.replace("_", " ")))
                            .addLore("")
                            .addLore(ChatUtil.color("&aНажмите, чтобы поставить титул"))
                            .setAmount(1)
                            .setDurability(3)
                            .setPlayerSkull(gamePlayer.getPlayer().getName())
                            .build()
            ));
    }

    public void open(GamePlayer gamePlayer) {
        if (page == 0)
            page = 1;
        this.gamePlayer = gamePlayer;
        gamePlayer.getPlayer().openInventory(getInventory());
    }

    @Override
    public void onClick(Player player, ClickType clickType, int slot, ItemStack itemStack) {
        switch (slot) {
            default:
                User.getUser(player.getName()).setSuffix(itemStack.getItemMeta().getDisplayName());
                ChatUtil.sendMessage(player, "&8[&bТитулы&8] &fВы установили личный титул &c%s&f.", ChatUtil.color(itemStack.getItemMeta().getDisplayName().replace("_", " ")));
                player.closeInventory();
                break;
        }
    }
}
