package ru.xfenilafs.lobby;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.minecraft.server.v1_12_R1.EnumDirection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.scheduler.BukkitRunnable;
import ru.redline.core.bukkit.ApiManager;
import ru.redline.core.bukkit.CorePlugin;
import ru.redline.core.bukkit.banerboard.updatable.LeaderBoard;
import ru.redline.core.bukkit.banerboard.updatable.leader.Leader;
import ru.redline.core.bukkit.player.CorePlayer;
import ru.redline.core.bukkit.protocollib.entity.impl.FakePlayer;
import ru.redline.core.bukkit.scoreboard.BaseScoreboardBuilder;
import ru.redline.core.bukkit.scoreboard.BaseScoreboardScope;
import ru.redline.core.bukkit.util.ChatUtil;
import ru.redline.core.bukkit.util.ItemUtil;
import ru.redline.core.global.database.RemoteDatabaseConnectionHandler;
import ru.redline.core.global.group.player.User;
import ru.xfenilafs.lobby.commands.*;
import ru.xfenilafs.lobby.config.ConfigManager;
import ru.xfenilafs.lobby.database.DatabaseManager;
import ru.xfenilafs.lobby.database.GamePlayer;
import ru.xfenilafs.lobby.discord.DiscordListener;
import ru.xfenilafs.lobby.listeners.GlobalListener;
import ru.xfenilafs.lobby.menus.Menu;
import ru.xfenilafs.lobby.protect.ChatListener;
import ru.xfenilafs.lobby.protect.ProtectListener;
import ru.xfenilafs.lobby.utils.*;
import ru.xfenilafs.lobby.utils.hologram.HologramManager;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Getter
@Slf4j
public class Main extends CorePlugin {
    @Getter
    public static Main instance;

    private ConfigManager configManager;
    private HologramManager hologramManager;
    public static FileConfiguration MENUS_CONFIG;
    public static final Set<Player> BUILD_MODE = new HashSet<>();
    private RemoteDatabaseConnectionHandler gameConnector;
    private DatabaseManager databaseManager;

    private final String token = "Njk0OTY1NjAxOTQ4NjYzODE5.XoTTJw.zummEFwrysU1lQMGiwonNq2h4qY";
    private JDA jda;
    private Guild guild;

    @Override
    public void onPluginEnable() {
        instance = this;
        configManager = new ConfigManager();
        hologramManager = new HologramManager();
        MENUS_CONFIG = ConfigUtils.loadConfig(new File(this.getDataFolder(), "menus.yml"));
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeUtils());
        gameConnector = ru.redline.core.global.ApiManager.getConnectionHandler("Lobby");
        databaseManager = new DatabaseManager(
                "localhost",
                "Lobby",
                "root",
                "vi6RcaDhRvkO0U5d",
                false
        );

        ApiManager.registerListeners(this,
                new GlobalListener(), new ChatListener()//, new ProtectListener()
        );

        registerCommands(
                new MenuCommand(), new BuildCommand(),
                new TitleCommand(), new MyTitleCommand(),
                new DiscordCommand()
        );

        Bukkit.getWorlds().forEach(world -> {
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setGameRuleValue("announceAdvancements", "false");
            world.setWeatherDuration(2147483647);
        });

        loadMenus();
        loadNpc();
        loadHolograms();
        ProtectListener.loadOwners();

        try {
            jda = JDABuilder.createDefault(token).build();
            jda.getPresence().setPresence(
                    OnlineStatus.DO_NOT_DISTURB,
                    Activity.watching("на айпи: mc.redline.pw"),
                    true
            );
            jda.addEventListener(new DiscordListener());
            jda.awaitReady();
            guild = jda.getGuildById("680500426831167493");

        } catch (LoginException | InterruptedException exception) {
            exception.printStackTrace();
        }

        ru.redline.core.bukkit.Main.getInstance().getUpdatableBoardManager().register(new LeaderBoard(
                new Location(Bukkit.getWorlds().get(0), 220, 106, 204),
                EnumDirection.NORTH, 5, "&7%s#&7 | &f%s &7- &6%s", "§fТоп по §aДонат-Валюте",
                lb -> ru.redline.core.global.ApiManager.getExecuteHandler("DonateBalance")
                        .executeQuery(true, "SELECT `PlayerName`, `balance` FROM `DonateBalance` ORDER BY `balance` DESC LIMIT 10")
                        .thenAccept(rs -> {
                            val contents = new LinkedList<Leader>();
                            int pos = 0;
                            while (rs.next() && pos < 10)
                                contents.add(new Leader(
                                        (pos == 0 ? "§c" : pos == 1 ? "§e" : pos == 2 ? "§6" : "§a") + (++pos),
                                        rs.getString("PlayerName"), StringUtil.fixDouble(1, rs.getDouble("balance")).toString() + "р")
                                );
                            lb.update(contents);
                        })
        ).setBackground(instance.getResource("top.jpg")).setDelay(360));
    }

    @Override
    public void onPluginDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        gameConnector.handleDisconnect();
        jda.shutdownNow();
    }

    private void loadMenus() {
        ConfigurationSection section = MENUS_CONFIG;
        if (section != null && section.getKeys(false).size() > 0) {
            section.getKeys(false).forEach((key) -> {
                ConfigurationSection keySection = section.getConfigurationSection(key);
                new Menu(key, keySection);
            });
        }

    }

    public void reloadMenus() {
        MENUS_CONFIG = ConfigUtils.loadConfig(new File(this.getDataFolder(), "menus.yml"));
        this.loadMenus();
    }

    public void loadScoreboard(Player player) {
        BaseScoreboardBuilder scoreboardBuilder = ApiManager.newScoreboardBuilder();

        scoreboardBuilder.scoreboardDisplay(ChatUtil.color("&3Лобби"));

        scoreboardBuilder.scoreboardScope(BaseScoreboardScope.PROTOTYPE);

        scoreboardBuilder.scoreboardLine(10, "");
        scoreboardBuilder.scoreboardLine(9, " §fНик: §aЗагрузка...");
        scoreboardBuilder.scoreboardLine(8, " §fГруппа: §aЗагрузка...");
        scoreboardBuilder.scoreboardLine(7, " §fБаланс: §aЗагрузка...");
        scoreboardBuilder.scoreboardLine(6, "");
        scoreboardBuilder.scoreboardLine(5, " §fХаб: §aЗагрузка...");
        scoreboardBuilder.scoreboardLine(4, " §fОнлайн: §aЗагрузка...");
        scoreboardBuilder.scoreboardLine(3, "");
        scoreboardBuilder.scoreboardLine(2, "    §bredline.pw");

        scoreboardBuilder.scoreboardUpdater((baseScoreboard, boardPlayer) -> {

            baseScoreboard.updateScoreboardLine(9, boardPlayer, ChatUtil.text(" &fНик: §a%s", boardPlayer.getName()));
            baseScoreboard.updateScoreboardLine(8, boardPlayer, ChatUtil.text(" &fГруппа: §a%s", ChatUtil.color(User.getUser(boardPlayer.getName()).getGroup().getFullName())));
            baseScoreboard.updateScoreboardLine(7, boardPlayer, ChatUtil.text(" &fБаланс: §a%s", StringUtil.fixDouble(CorePlayer.getCorePlayer(boardPlayer.getName()).getBalance(), 1)));

            baseScoreboard.updateScoreboardLine(5, boardPlayer, ChatUtil.text(" &fХаб: §a#%s", Bukkit.getServerName().replace("hub", "")));
            baseScoreboard.updateScoreboardLine(4, boardPlayer, ChatUtil.text(" &fОнлайн: §a%s", BungeeUtils.getAllOnline(boardPlayer)));

        }, 20);

        scoreboardBuilder.build().setScoreboardToPlayer(player);
    }

    public void loadNpc() {
        ConfigurationSection section = configManager.getNpcConfig().getConfiguration();
        if (section != null && section.getKeys(false).size() > 0) {
            section.getKeys(false).forEach((npc) -> {
                ConfigurationSection keySection = section.getConfigurationSection(npc);

                Location location = ConfigUtils.loadLocationFromConfigurationSection(keySection);

                String playerSkin = keySection.getString("playerSkin");
                String glowingColor = keySection.getString("glowing");
                String connect = keySection.getString("connect");
                String server = keySection.getString("server");
                String permission = keySection.getString("permission");
                String menu = keySection.getString("menu");

                boolean invisible = keySection.getBoolean("invisible");
                boolean sneaking = keySection.getBoolean("sneaking");
                boolean isDev = keySection.getBoolean("isDev");

                val servers = getConfig().getStringList("servers");

                val fakePlayer = new FakePlayer(playerSkin, location);

                if (glowingColor != null) {
                    fakePlayer.setGlowingColor(ChatColor.valueOf(glowingColor));
                }

                fakePlayer.setClickAction(clickedPlayer -> {
                    if (permission != null && !clickedPlayer.hasPermission(permission)) {
                        return;
                    }

                    long delay = CooldownUtils.checkCooldown(clickedPlayer, "clicked", Main.getInstance().getConfig().getLong("cooldown"));

                    if (delay > 0L && !clickedPlayer.hasPermission("core.admin")) {
                        GamePlayer.getPlayer(clickedPlayer.getName()).sendActionBar("§fПодождите §c%s сек. §fперед следующим использованием", delay);
                        return;
                    }
                    if (menu != null) {
                        Menu.getMenu(menu).open(clickedPlayer);
                        CooldownUtils.addCooldown(clickedPlayer, "clicked");
                        return;
                    }

                    if (connect != null) {
                        BungeeUtils.sendToServer(clickedPlayer, connect);
                        CooldownUtils.addCooldown(clickedPlayer, "clicked");
                    }
                });

                fakePlayer.setInvisible(invisible);
                fakePlayer.setSneaking(sneaking);

                if (isDev) {
                    getHologramManager().createHologram(server, location.clone().add(0.0D, 0.25D, 0.0D), hologram -> {
                        hologram.addLine(ChatUtil.text(server));
                        hologram.addLine(ChatUtil.text("&bВ разработке"));

                        hologram.spawn();
                    });
                } else {
                    getHologramManager().createHologram(connect, location.clone().add(0.0D, 0.25D, 0.0D), hologram -> {
                        hologram.addLine(ChatUtil.text("&aНажмите для подключения"));
                        hologram.addLine(ChatUtil.text("&fК серверу %s", server));
                        hologram.addLine(ChatUtil.text("&fОнлайн: &a%s&7/&a200", BungeeUtils.getServerOnline(connect)));

                        hologram.spawn();
                    });
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        servers.forEach(serverUpdate -> getHologramManager().getCachedHologram(serverUpdate)
                                .modifyLine(2, ChatUtil.text("§fОнлайн: §a%s&7/§a200", BungeeUtils.getServerOnline(serverUpdate))));
                    }
                }.runTaskTimerAsynchronously(instance, 600L, 600L);

                fakePlayer.spawn();
            });
        }
    }

    public void loadHolograms() {
        ConfigurationSection section = configManager.getHologramConfig().getConfiguration();
        if (section != null && section.getKeys(false).size() > 0) {
            section.getKeys(false).forEach((hologram) -> {
                ConfigurationSection keySection = section.getConfigurationSection(hologram);

                String name = keySection.getString("name");
                Location location = ConfigUtils.loadLocationFromConfigurationSection(keySection);
                List<String> lines = keySection.getStringList("lines");

                getHologramManager().createHologram(name, location, holo -> {
                    for (String line : lines) {
                        holo.addLine(
                                ChatUtil.color(line)
                        );
                    }

                    holo.spawn();
                });
            });

        }
    }

    public void loadItems(Player player) {
        player.getInventory().clear();

        ConfigurationSection section = configManager.getItemsConfig().getConfiguration();
        if (section != null && section.getKeys(false).size() > 0) {
            section.getKeys(false).forEach((item) -> {
                ConfigurationSection keySection = section.getConfigurationSection(item);

                String name = keySection.getString("name");
                String playerSkill = keySection.getString("player-skull");
                Material material = Material.valueOf(keySection.getString("material"));
                short data = (short) section.getInt("data");
                List<String> lore = keySection.getStringList("lore");
                int slot = keySection.getInt("slot");

                ItemUtil.ItemBuilder itemBuilder = ApiManager.newItemBuilder(material);

                if (playerSkill != null && data == 3) {
                    if (playerSkill.equalsIgnoreCase("%player%")) {
                        itemBuilder.setPlayerSkull(player.getName());
                    } else {
                        itemBuilder.setTextureValue(playerSkill);
                    }
                }

                itemBuilder.setDurability(data);
                itemBuilder.setName(ChatUtil.color(name));
                itemBuilder.setLore(lore);
                itemBuilder.setUnbreakable(true);
                itemBuilder.addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
                itemBuilder.addItemFlag(ItemFlag.HIDE_ENCHANTS);
                itemBuilder.addItemFlag(ItemFlag.HIDE_UNBREAKABLE);
                itemBuilder.addItemFlag(ItemFlag.HIDE_POTION_EFFECTS);

                player.getInventory().setItem(
                        slot,
                        itemBuilder.build()
                );
            });
        }
    }
}
