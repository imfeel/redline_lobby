package ru.xfenilafs.lobby.listeners;

import lombok.val;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.redline.core.bukkit.ApiManager;
import ru.redline.core.bukkit.util.ChatUtil;
import ru.redline.core.global.group.player.User;
import ru.xfenilafs.lobby.Main;
import ru.xfenilafs.lobby.database.GamePlayer;
import ru.xfenilafs.lobby.menus.Menu;
import ru.xfenilafs.lobby.menus.MenuItem;
import ru.xfenilafs.lobby.menus.game.IGui;
import ru.xfenilafs.lobby.utils.BungeeUtils;
import ru.xfenilafs.lobby.utils.ConfigUtils;
import ru.xfenilafs.lobby.utils.CooldownUtils;
import ru.xfenilafs.lobby.utils.StringUtil;

import static ru.xfenilafs.lobby.Main.BUILD_MODE;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class GlobalListener implements Listener {
    private final Random random = new Random();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();

        GamePlayer gamePlayer = new GamePlayer(player.getName());

        if (player.hasPermission("lobby.fly")) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }

        //int randomTitle = random.nextInt(Main.getInstance().getConfig().getConfigurationSection("titles").getKeys(false).size());

        //player.sendTitle(
        //        ChatUtil.color(Main.getInstance().getConfig().getString("titles." + randomTitle + ".title")),
        //        ChatUtil.color(Main.getInstance().getConfig().getString("titles." + randomTitle + ".subtitle")),
        //       20, 55, 20
        //);

        Main.getInstance().loadScoreboard(player);
        Main.getInstance().loadItems(player);
        player.teleport(ConfigUtils.loadLocationFromConfigurationSection(Main.getInstance().getConfig().getConfigurationSection("locations.spawn")));
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        String prefix = User.getUser(player.getName()).getPrefix();

        TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatUtil.text("%s%s&f: %s", ru.xfenilafs.lobby.utils.ChatUtil.COLORIZE.apply(prefix), player.getName(), message)));

        event.setCancelled(true);
        Bukkit.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(component));
        Bukkit.getConsoleSender().sendMessage(ChatUtil.text("%s%s: %s", ru.xfenilafs.lobby.utils.ChatUtil.COLORIZE.apply(prefix), player.getName(), message));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (event.getClickedBlock() != null) {
            List<Material> blockedMaterials = Arrays.asList(Material.CHEST, Material.WORKBENCH, Material.ENCHANTMENT_TABLE, Material.ANVIL, Material.FURNACE, Material.DISPENSER, Material.DROPPER, Material.OBSERVER, Material.HOPPER, Material.BREWING_STAND);
            Material material = event.getClickedBlock().getType();

            if (!player.isOp() && blockedMaterials.contains(material)) {
                event.setCancelled(true);
            }
        }

        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        if (event.getHand() == EquipmentSlot.HAND && player.getInventory().getItemInMainHand() != null) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                ConfigurationSection section = Main.getInstance().getConfigManager().getItemsConfig().getConfiguration();
                if (section != null && section.getKeys(false).size() > 0) {
                    section.getKeys(false).forEach((joinItem) -> {
                        ConfigurationSection keySection = section.getConfigurationSection(joinItem);
                        if (StringUtil.hasName(item, keySection.getString("name"))) {
                            long delay = CooldownUtils.checkCooldown(player, "clicked", Main.getInstance().getConfig().getLong("cooldown"));

                            if (delay > 0L && !player.hasPermission("core.admin")) {
                                GamePlayer.getPlayer(player.getName()).sendActionBar("§fПодождите §c%s сек. §fперед следующим использованием", delay);
                                return;
                            }

                            Bukkit.dispatchCommand(player, keySection.getString("command"));
                            CooldownUtils.addCooldown(player, "clicked");
                        }

                    });
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        GamePlayer gamePlayer = GamePlayer.getPlayer(event.getPlayer().getName());

        try {
            gamePlayer.unload();
        } catch (NullPointerException ignored) {

        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        event.setLeaveMessage(null);

        GamePlayer gamePlayer = GamePlayer.getPlayer(event.getPlayer().getName());

        try {
            gamePlayer.unload();
        } catch (NullPointerException ignored) {

        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        if (event.getCurrentItem() != null && inventory.getName() != null && Menu.getMenuByName(inventory.getName()) != null) {
            event.setCancelled(true);
            Menu menu = Menu.getMenuByName(inventory.getName());
            if (Objects.requireNonNull(menu).getMenuItem(event.getRawSlot()) != null) {
                if (event.getRawSlot() >= inventory.getSize()) {
                    return;
                }

                player.closeInventory();
                MenuItem menuItem = menu.getMenuItem(event.getRawSlot());
                if (menuItem.getPermission() != null && !player.hasPermission(menuItem.getPermission())) {
                    if (menuItem.getPermMessage() != null) {
                        ChatUtil.sendMessage(player, ChatUtil.color(menuItem.getPermMessage()));
                        return;
                    }
                    ChatUtil.sendMessage(player, "&8[&bСервер&8] §fДанная функция вам недоступна.");
                    return;
                }

                if (menuItem.getClickSound() != null) {
                    player.playSound(player.getEyeLocation(), menuItem.getClickSound(), 1.0F, 1.0F);
                }

                if (menuItem.getMessage() != null) {
                    ChatUtil.sendMessage(player, ChatUtil.color(menuItem.getMessage()));
                }

                if (menuItem.getCommand() != null) {
                    if (menuItem.getCommand().startsWith("server:")) {
                        String server = menuItem.getCommand().replace("server:", "");
                        BungeeUtils.sendToServer(player, server);
                    } else {
                        if (menuItem.getType() != null) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), menuItem.getCommand().replace("%player%", player.getName()));
                        } else {
                            Bukkit.dispatchCommand(player, menuItem.getCommand().replace("%player%", player.getName()));
                        }
                    }
                }
            }
        }

        if (!event.getWhoClicked().isOp() && event.getClick() == ClickType.NUMBER_KEY) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
        } else {
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {

                if (event.getInventory().getHolder() instanceof IGui) {
                    event.setCancelled(true);
                    if (event.getCurrentItem() == null || event.getRawSlot() >= inventory.getSize() && !event.getView().getTitle().equals("Test")) {
                        return;
                    }

                    IGui gui = (IGui) inventory.getHolder();
                    gui.onClick(player, event.getClick(), event.getRawSlot(), event.getCurrentItem());
                    player.playSound(player.getEyeLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                }
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!BUILD_MODE.contains(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!BUILD_MODE.contains(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) { event.setCancelled(true);

    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) { event.setCancelled(true);

    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) { event.setCancelled(true);

    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) { event.setAmount(0);

    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) { event.setCancelled(true);

    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) { event.setCancelled(true);
    }
}
