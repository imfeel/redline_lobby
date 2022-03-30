package ru.xfenilafs.lobby.utils;

import java.util.HashMap;
import java.util.Map;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

@UtilityClass
public class CooldownUtils {
    public static Map<Player, Map<String, Long>> cooldowns = new HashMap<>();

    public static long checkCooldown(Player player, String cooldownName, long cooldownDelay) {
        if (cooldowns.containsKey(player) && cooldowns.get(player).containsKey(cooldownName)) {
            long remaining = cooldownDelay - (System.currentTimeMillis() - cooldowns.get(player).get(cooldownName)) / 1000L;
            if (remaining == 0L) {
                cooldowns.get(player).remove(cooldownName);
            }

            return remaining;
        } else {
            return 0L;
        }
    }

    public static void addCooldown(Player player, String cooldownName) {
        Map<String, Long> currentCooldowns = new HashMap<>();
        if (cooldowns.get(player) != null) {
            currentCooldowns = cooldowns.get(player);
        }

        currentCooldowns.put(cooldownName, System.currentTimeMillis());
        cooldowns.put(player, currentCooldowns);
    }

    public static void removeCooldown(Player player, String cooldownName) {
        if (cooldowns.containsKey(player) || cooldowns.get(player).containsKey(cooldownName)) {
            cooldowns.get(player).remove(cooldownName);
        }

    }
}

