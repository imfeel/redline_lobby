package ru.xfenilafs.lobby.utils;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import lombok.experimental.UtilityClass;

import java.text.DecimalFormat;
import java.util.function.Function;

@UtilityClass
public class StringUtil {
    public static final Function<String, String> COLORIZE = (string -> (string == null) ? null : ChatColor.translateAlternateColorCodes('&', string));
    public static String formatDouble(double d) {
        if (d < 1000.0)
            return String.format("%.2f", d).replace(",", ".");
        if (d < 1000000.0)
            return String.format("%.2f", d / 1000.0).replace(",", ".") + "K";
        if (d < 1.0E9)
            return String.format("%.2f", d / 1000000.0).replace(",", ".") + "M";
        if (d < 1.0E12D)
            return String.format("%.2f", d / 1.0E9D).replace(",", ".") + "B";
        if (d < 1.0E15D)
            return String.format("%.2f", d / 1.0E12D).replace(",", ".") + "T";
        if (d < 1.0E18D)
            return String.format("%.2f", d / 1.0E15D).replace(",", ".") + "Q";
        if (d < 1.0E21D)
            return String.format("%.2f", d / 1.0E18D).replace(",", ".") + "D";
        if (d < 1.0E24D)
            return String.format("%.2f", d / 1.0E21D).replace(",", ".") + "S";
        if (d < 1.0E27D)
            return String.format("%.2f", d / 1.0E24D).replace(",", ".") + "P";
        return String.format("%.2f", d / 1.0E27D).replace(",", ".") + "O";
    }

    public static Double fixDouble(int i, double d) {
        return Double.valueOf(String.format("%." + i + "f", d).replace(",", "."));
    }

    public static double fixDouble(double amount, int digits) {
        if (digits == 0)
            return (int)amount;
        StringBuilder format = new StringBuilder("##");
        for (int i = 0; i < digits; i++) {
            if (i == 0)
                format.append(".");
            format.append("#");
        }
        return Double.parseDouble((new DecimalFormat(format.toString())).format(amount).replace(",", "."));
    }

    public static String trim(String string) {
        if (string.length() > 16)
            return string.substring(0, 16);
        return string;
    }

    public static String arrayToString(String[] strings, int start, int end) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = start; i <= end; ++i)
            stringBuilder.append(strings[i]).append(" ");
        return stringBuilder.toString();
    }

    public static boolean hasName(ItemStack itemStack, String name) {
        return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().equals(name);
    }

}
