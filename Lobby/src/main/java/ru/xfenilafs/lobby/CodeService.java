package ru.xfenilafs.lobby;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.TimeUnit;

public class CodeService {
    private static final Cache<String, String> discordCodeCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    public static String generateNewCode(String name) {
        String code = RandomStringUtils.randomAlphanumeric(6);
        discordCodeCache.put(code, name);
        return code;
    }

    public static boolean checkCode(String code) {
        return discordCodeCache.asMap().containsKey(code);
    }

    public static String values() {
        return discordCodeCache.asMap().toString();
    }

    public static boolean isCode(String name) {
       return discordCodeCache.asMap().containsValue(name);
    }

    public static void removeCode(String code) {
        discordCodeCache.asMap().remove(code);
    }

    public static String getPlayer(String code) {
        return discordCodeCache.asMap().get(code);
    }

}
