package ru.xfenilafs.lobby.menus.game;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface IGui extends InventoryHolder {
    void onClick(Player player, ClickType clickType, int slot, ItemStack itemStack);
}
