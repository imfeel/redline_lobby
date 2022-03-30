package ru.xfenilafs.lobby.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.redline.core.bukkit.util.ChatUtil;

@Getter
public class Menu {
    public static Map<String, Menu> menus = new HashMap<>();

    private final String id;
    private final String name;
    private final int size;
    private final boolean glass;
    private final List<MenuItem> items;
    private String permission;

    public Menu(String id, ConfigurationSection section) {
        this.id = id;
        this.name = section.getString("name");
        this.size = section.getInt("size");
        this.glass = section.getBoolean("glass");
        this.items = new ArrayList<>();
        ConfigurationSection itemsSection = section.getConfigurationSection("items");
        if (itemsSection != null && itemsSection.getKeys(false).size() > 0) {
            itemsSection.getKeys(false).forEach((item) -> {
                try {
                    this.items.add(new MenuItem(itemsSection.getConfigurationSection(item)));
                } catch (NullPointerException var5) {
                    System.out.println("Error while loading menu item in menu " + id + " with item key " + item);
                }

            });
        }

        if (section.getString("permission") != null) {
            this.permission = section.getString("permission");
        }

        menus.put(id, this);
    }

    public static Menu getMenu(String id) {
        return menus.get(id);
    }

    public static Menu getMenuByName(String name) {
        for (Menu menu : menus.values()) {
            if (menu.getName().equals(name)) {
                return menu;
            }
        }

        return null;
    }

    public void open(Player player) {
        if (this.permission != null && !player.hasPermission(this.permission)) {
            ChatUtil.sendMessage(player, "&8[&bКоманды&8] §fДанное меню вам недоступно.");
        } else {
            Inventory inventory = Bukkit.createInventory(null, this.size, this.name);
            ItemStack blank = new ItemStack(Material.STAINED_GLASS_PANE);
            ItemMeta blankMeta = blank.getItemMeta();
            blankMeta.setDisplayName(" ");
            blank.setItemMeta(blankMeta);

            if (glass) {
                for (int i = 0; i < 54 && i != this.size; ++i) {
                    if (i < 10 || i > this.size - 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36 || i == 44 || i == 45) {
                        inventory.setItem(i, blank);
                    }
                }
            }

            this.items.forEach((menuItem) -> {
                if (menuItem.getPosition() < this.size) {
                    inventory.setItem(menuItem.getPosition(), menuItem.getItemStack(player));
                } else {
                    System.out.println("Error while opening menu for player " + player.getName() + " while setting item menu " + menuItem.getName() + " (position >= size) with menu id " + this.id);
                }

            });
            player.openInventory(inventory);
        }
    }

    public MenuItem getMenuItem(int position) {
        for (MenuItem menuItem : this.items) {
            if (menuItem.getPosition() == position) {
                return menuItem;
            }
        }

        return null;
    }


}
