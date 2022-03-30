package ru.xfenilafs.lobby.config;

import lombok.Getter;

@Getter
public class ConfigManager {
    private Config config;
    private Config menuConfig;
    private Config npcConfig;
    private Config hologramConfig;
    private Config itemsConfig;
    private Config protectConfig;

    public ConfigManager() {
        init();
    }

    private void init() {
        config = new Config("config.yml", "config.yml");
        menuConfig = new Config("menus.yml", "menus.yml");
        npcConfig = new Config("npcs.yml", "npcs.yml");
        hologramConfig = new Config("holograms.yml", "holograms.yml");
        itemsConfig = new Config("items.yml", "items.yml");
        protectConfig = new Config("protect.yml", "protect.yml");
    }
}
