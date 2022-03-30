package ru.xfenilafs.lobby.utils;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.chat.*;
import java.util.*;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import net.md_5.bungee.api.*;

@UtilityClass
public final class ChatUtil {
    public static final Function<String, String> COLORIZE = (string -> (string == null) ? null : ChatColor.translateAlternateColorCodes('&', string));

    public static void sendMessage(CommandSender commandSender, String message) {
        commandSender.sendMessage("§8[§a!§8] §f" + c(message));
    }

    public static void sendMessage(Player player, String prefix, String message) {
        if (player != null) {
            player.sendMessage(c(prefix + ": &f" + message));
        }
    }

    public static void sendMessage(String name, String message) {
        if (Bukkit.getPlayer(name) != null) {
            Bukkit.getPlayer(name).sendMessage("§8[§a!§8] §f" + c(message));
        }
    }

    public static void sendMessage(Player player, String message, boolean prefix) {
        if (player != null) {
            player.sendMessage(prefix ? "§8[§a!§8] §f" + c(message) : c(message));
        }
    }

    public static void sendMessage(Player player, String message, Object... obj) {
        if (player != null) {
            player.sendMessage(String.format(c("§8[§a!§8] §f" + message), obj));
        }
    }

    public static String c(String message) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String color(String message) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }


    public static void b(Object message) {
        Bukkit.broadcastMessage("§8[§a!§8] §f" + c(message.toString()));
    }

    public static void broadcast(Object message) {
        Bukkit.broadcastMessage(c(message.toString()));
    }

    public static void b(String message, Object... args) {
        b(String.format(message, args));
    }

    public static void pb(String plugin, String message) {
        b("&8[&6%s&8] &e%s", plugin, message);
    }

    public static BaseComponent[] createMessage(String message, String click, String hover, ClickEvent.Action clickAction) {
        TextComponent textComponent = new TextComponent(message);
        textComponent.setClickEvent(new ClickEvent(clickAction, click));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(hover).duplicate()}));
        return new BaseComponent[]{textComponent.duplicate()};
    }

    public static BaseComponent[] createMessage(String message) {
        TextComponent textComponent = new TextComponent(message);
        return new BaseComponent[]{textComponent.duplicate()};
    }

    public static BaseComponent[] buildMessages(BaseComponent[]... baseComponents) {
        List<BaseComponent> outputList = new ArrayList<BaseComponent>();
        for (BaseComponent[] baseComponentArray : baseComponents) {
            outputList.addAll(Arrays.asList(baseComponentArray));
        }
        return outputList.toArray(new BaseComponent[0]);
    }

    public static void sendMessage(Player player, BaseComponent... messages) {
        player.spigot().sendMessage(messages);
    }

    public static void sendMessage(Player player, BaseComponent message) {
        player.spigot().sendMessage(message);
    }

    public static MessageBuilder newBuilder() {
        return newBuilder("");
    }

    public static MessageBuilder newBuilder(String text) {
        return new MessageBuilder(text);
    }

    public static class MessageBuilder implements Builder<BaseComponent[]> {
        private TextComponent component;

        public MessageBuilder(String message) {
            this.component = new TextComponent(message);
        }

        public MessageBuilder setText(String text) {
            this.component.setText(text);
            return this;
        }

        public MessageBuilder setHoverEvent(HoverEvent.Action action, String text) {
            HoverEvent hoverEvent = new HoverEvent(action, new BaseComponent[]{new TextComponent(text).duplicate()});
            this.component.setHoverEvent(hoverEvent);
            return this;
        }

        public MessageBuilder setClickEvent(ClickEvent.Action action, String text) {
            ClickEvent clickEvent = new ClickEvent(action, text);
            this.component.setClickEvent(clickEvent);
            return this;
        }

        public MessageBuilder setBold(boolean flag) {
            this.component.setBold(flag);
            return this;
        }

        public MessageBuilder setUnderlined(boolean flag) {
            this.component.setUnderlined(flag);
            return this;
        }

        public MessageBuilder setColor(ChatColor color) {
            this.component.setColor(color);
            return this;
        }

        @Override
        public BaseComponent[] build() {
            return new BaseComponent[]{this.component.duplicate()};
        }
    }

    public interface Builder<T>
    {
        T build();
    }

}