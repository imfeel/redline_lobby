package ru.xfenilafs.lobby.menus;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ru.redline.core.bukkit.ApiManager;
import ru.redline.core.bukkit.util.ItemUtil;
import ru.xfenilafs.lobby.utils.BungeeUtils;

@Getter
public class MenuItem {
    private final Material material;
    private final int amount;
    private final short data;
    private final int position;
    private final String name;
    private List<String> lore;
    private final boolean glowing;
    private String permission;
    private Sound clickSound;
    private String message;
    private String permMessage;
    private String command;
    private final String playerSkill;
    private CommandType type;
    private final boolean title;

    public MenuItem(ConfigurationSection section) {
        this.material = Material.valueOf(section.getString("material"));
        this.playerSkill = section.getString("player-skull");
        this.amount = section.getInt("amount");
        this.data = (short) section.getInt("data");
        this.position = section.getInt("position");
        this.name = section.getString("name");
        if (section.getStringList("lore") != null && section.getStringList("lore").size() > 0) {
            this.lore = section.getStringList("lore");
        }

        this.glowing = section.getBoolean("glowing");

        if (section.getStringList("permission") != null) {
            this.permission = section.getString("permission");
        }

        if (section.getString("click-sound") != null) {
            this.clickSound = Sound.valueOf(section.getString("click-sound"));
        }

        if (section.getString("message") != null) {
            this.message = section.getString("message");
        }

        if (section.getString("perm-message") != null) {
            this.permMessage = section.getString("perm-message");
        }

        if (section.getString("command") != null) {
            this.command = section.getString("command");
        }

        if (section.getString("command-type") != null) {
            this.type = CommandType.valueOf(section.getString("command-type"));
        }

        this.title = section.getBoolean("title");

    }

    public ItemStack getItemStack(Player player) {
        ItemUtil.ItemBuilder itemBuilder = ApiManager.newItemBuilder(title ? Material.INK_SACK : material);

        itemBuilder.setName(name);

        if (playerSkill != null && data == 3) {
            if (playerSkill.equalsIgnoreCase("%player%")) {
                itemBuilder.setPlayerSkull(player.getName());
            } else {
                itemBuilder.setTextureValue(playerSkill);
            }
        }

        if (this.lore != null) {
            List<String> itemLore = new ArrayList<>();
            this.lore.forEach((string) -> {
                if (string.startsWith("online:")) {
                    String[] servers = string.replace("online:", "").split(",");
                    int count = 0;

                    for (String server : servers) {
                        count += BungeeUtils.getServerOnline(server);
                    }

                    itemLore.add(" §7| §fОнлайн режима: §a" + count);
                } else if (string.startsWith("online-default:")) {
                    String[] servers = string.replace("online-default:", "").split(",");
                    int count = 0;

                    for (String server : servers) {
                        count += BungeeUtils.getServerOnline(server);
                    }

                    itemLore.add("§fОнлайн: §a" + count);
                } else if (string.startsWith("hasServer:")) {
                    String server = string.replace("hasServer:", "");

                    if (Bukkit.getServerName().equalsIgnoreCase(server)) {
                        itemLore.add("§cВы уже находитесь на этом сервере.");
                    }
                } else {
                    itemLore.add(string);
                }

            });
            if (this.glowing) {
                itemBuilder.addEnchantment(Enchantment.LUCK, 1);
            }

            itemBuilder.setLore(itemLore);
        }

        itemBuilder.setDurability(data);

        if (title)
            itemBuilder.setDurability(player.hasPermission(permission) ? 10 : 8);

        itemBuilder.addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
        itemBuilder.addItemFlag(ItemFlag.HIDE_ENCHANTS);
        itemBuilder.addItemFlag(ItemFlag.HIDE_UNBREAKABLE);
        itemBuilder.addItemFlag(ItemFlag.HIDE_POTION_EFFECTS);

        return itemBuilder.build();
    }

    public enum CommandType {
        PLAYER,
        CONSOLE
    }
}
