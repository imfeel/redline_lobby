package ru.xfenilafs.lobby.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.ApiStatus;
import ru.xfenilafs.lobby.Main;

import java.awt.Color;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class DiscordUtil {

    public static JDA getJda() {
        return Main.getInstance().getJda();
    }

    public static String getRoleName(Role role) {
        return role == null ? "" : role.getName();
    }

    public static Role getTopRole(Member member) {
        return member.getRoles().size() != 0 ? member.getRoles().get(0) : null;
    }

    public static Role getTopRoleWithCustomColor(Member member) {
        for (Role role : member.getRoles()) if (role.getColor() != null) return role;
        return null;
    }

    private static final Pattern USER_MENTION_PATTERN = Pattern.compile("(<@!?([0-9]{16,20})>)");
    private static final Pattern CHANNEL_MENTION_PATTERN = Pattern.compile("(<#([0-9]{16,20})>)");
    private static final Pattern ROLE_MENTION_PATTERN = Pattern.compile("(<@&([0-9]{16,20})>)");
    private static final Pattern EMOTE_MENTION_PATTERN = Pattern.compile("(<a?:([a-zA-Z]{2,32}):[0-9]{16,20}>)");

    public static String convertMentionsToNames(String message) {
        Matcher userMatcher = USER_MENTION_PATTERN.matcher(message);
        while (userMatcher.find()) {
            String mention = userMatcher.group(1);
            String userId = userMatcher.group(2);
            User user = getUserById(userId);
            message = message.replace(mention, user != null ? "@" + user.getName() : mention);
        }

        Matcher channelMatcher = CHANNEL_MENTION_PATTERN.matcher(message);
        while (channelMatcher.find()) {
            String mention = channelMatcher.group(1);
            String channelId = channelMatcher.group(2);
            TextChannel channel = getTextChannelById(channelId);
            message = message.replace(mention, channel != null ? "#" + channel.getName() : mention);
        }

        Matcher roleMatcher = ROLE_MENTION_PATTERN.matcher(message);
        while (roleMatcher.find()) {
            String mention = roleMatcher.group(1);
            String roleId = roleMatcher.group(2);
            Role role = getRole(roleId);
            message = message.replace(mention, role != null ? "@" + role.getName() : mention);
        }

        Matcher emoteMatcher = EMOTE_MENTION_PATTERN.matcher(message);
        while (emoteMatcher.find()) {
            message = message.replace(emoteMatcher.group(1), ":" + emoteMatcher.group(2) + ":");
        }

        return message;
    }

    public static String convertMentionsFromNames(String message, Guild guild) {
        if (!message.contains("@")) return message;

        Map<Pattern, String> patterns = new HashMap<>();
        for (Role role : guild.getRoles()) {
            Pattern pattern = mentionPatternCache.computeIfAbsent(
                    role.getId(),
                    mentionable -> Pattern.compile(
                            "(?<!<)" +
                                    Pattern.quote("@" + role.getName()),
                            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
                    )
            );
            patterns.put(pattern, role.getAsMention());
        }

        for (Member member : guild.getMembers()) {
            Pattern pattern = mentionPatternCache.computeIfAbsent(
                    member.getId(),
                    mentionable -> Pattern.compile(
                            "(?<!<)" +
                                    Pattern.quote("@" + member.getEffectiveName()),
                            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
                    )
            );
            patterns.put(pattern, member.getAsMention());
        }

        for (Map.Entry<Pattern, String> entry : patterns.entrySet()) {
            message = entry.getKey().matcher(message).replaceAll(entry.getValue());
        }

        return message;
    }
    private static Map<String, Pattern> mentionPatternCache = new HashMap<>();
    static {
        if (DiscordUtil.getJda() != null) {
            DiscordUtil.getJda().addEventListener(new ListenerAdapter() {
                @Override
                public void onUserUpdateName(UserUpdateNameEvent event) {
                    mentionPatternCache.remove(event.getUser().getId());
                }
                @Override
                public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
                    mentionPatternCache.remove(event.getMember().getId());
                }
                @Override
                public void onRoleUpdateName(RoleUpdateNameEvent event) {
                    mentionPatternCache.remove(event.getRole().getId());
                }
            });
        }
    }

    public static String escapeMarkdown(String text) {
        return text == null ? "" : text.replace("_", "\\_").replace("*", "\\*").replace("~", "\\~");
    }

    @Deprecated
    public static String strip(String text) {
        return ChatColor.stripColor(text);
    }

    @Deprecated
    public static String stripSectionOnly(String text) {
        return ChatColor.stripColor(text);
    }

    private static final Pattern aggressiveStripPattern = Pattern.compile("\u001B(?:\\[0?m|\\[38;2(?:;\\d{1,3}){3}m|\\[([0-9]{1,2}[;m]?){3})");

    public static String aggressiveStrip(String text) {
        if (StringUtils.isBlank(text)) {
            log.debug("Tried aggressively stripping blank message");
            return null;
        }

        return aggressiveStripPattern.matcher(text).replaceAll("");
    }

    @SuppressWarnings("unused")
    @Deprecated
    public static void sendMessage(TextChannel channel, String message, int expiration, @Deprecated boolean editMessage) {
        sendMessage(channel, message, expiration);
    }

    public static void sendMessage(TextChannel channel, String message) {
        sendMessage(channel, message, 0);
    }

    public static void sendMessage(TextChannel channel, String message, int expiration) {
        if (channel == null) {
            log.debug("Tried sending a message to a null channel");
            return;
        }

        if (getJda() == null) {
            log.debug("Tried sending a message using a null JDA instance");
            return;
        }

        if (message == null) {
            log.debug("Tried sending a null message to " + channel);
            return;
        }

        if (StringUtils.isBlank(message)) {
            log.debug("Tried sending a blank message to " + channel);
            return;
        }

        message = ChatColor.stripColor(message);

        String overflow = null;
        int maxLength = Message.MAX_CONTENT_LENGTH;
        if (message.length() > maxLength) {
            log.debug("Tried sending message with length of " + message.length() + " (" + (message.length() - maxLength) + " over limit)");
            overflow = message.substring(maxLength);
            message = message.substring(0, maxLength);
        }

        queueMessage(channel, message, m -> {
            if (expiration > 0) {
                try { Thread.sleep(expiration); } catch (InterruptedException ignored) {}
                deleteMessage(m);
            }
        });
        if (overflow != null) sendMessage(channel, overflow, expiration);
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public static String cutPhrases(String message) {
        return message;
    }

    public static boolean checkPermission(GuildChannel channel, Permission permission) {
        return checkPermission(channel, getJda().getSelfUser(), permission);
    }

    public static boolean checkPermission(Guild guild, Permission permission) {
        return guild != null && guild.getSelfMember().hasPermission(permission);
    }

    public static boolean checkPermission(GuildChannel channel, User user, Permission permission) {
        if (channel == null) return false;
        Member member = channel.getGuild().getMember(user);
        if (member == null) return false;
        return member.hasPermission(channel, permission);
    }

    public static Message sendMessageBlocking(TextChannel channel, String message) {
        return sendMessageBlocking(channel, message, false);
    }

    public static Message sendMessageBlocking(TextChannel channel, String message, boolean allowMassPing) {
        if (message == null || StringUtils.isBlank(message)) {
            log.debug("Tried sending a null or blank message");
            return null;
        }

        if (channel == null) {
            log.debug("Tried sending a message to a null channel");
            return null;
        }

        message = translateEmotes(message, channel.getGuild());

        return sendMessageBlocking(channel, new MessageBuilder().append(message).build(), allowMassPing);
    }

    public static Message sendMessageBlocking(TextChannel channel, Message message, boolean allowMassPing) {
        if (getJda() == null) {
            log.debug("Tried sending a message when JDA was null");
            return null;
        }

        if (channel == null) {
            log.debug("Tried sending a message to a null channel");
            return null;
        }

        if (message == null || StringUtils.isBlank(message.getContentRaw())) {
            log.debug("Tried sending a null or blank message");
            return null;
        }

        Message sentMessage;
        try {
            MessageAction action = channel.sendMessage(message);
            if (allowMassPing) action = action.allowedMentions(EnumSet.allOf(Message.MentionType.class));
            sentMessage = action.complete();
        } catch (PermissionException e) {
            if (e.getPermission() != Permission.UNKNOWN) {
                log.warn("Could not send message in channel " + channel + " because the bot does not have the \"" + e.getPermission().getName() + "\" permission");
            } else {
                log.warn("Could not send message in channel " + channel + " because \"" + e.getMessage() + "\"");
            }
            return null;
        }

        return sentMessage;
    }

    public static void queueMessage(TextChannel channel, String message) {
        if (channel == null) {
            log.debug("Tried sending a message to a null channel");
            return;
        }

        message = translateEmotes(message, channel.getGuild());
        if (StringUtils.isBlank(message)) return;
        queueMessage(channel, new MessageBuilder().append(message).build(), false);
    }

    public static void queueMessage(TextChannel channel, String message, boolean allowMassPing) {
        if (channel == null) {
            log.debug("Tried sending a message to a null channel");
            return;
        }

        message = translateEmotes(message, channel.getGuild());
        if (StringUtils.isBlank(message)) return;
        queueMessage(channel, new MessageBuilder().append(message).build(), allowMassPing);
    }

    public static void queueMessage(TextChannel channel, Message message) {
        queueMessage(channel, message, null);
    }

    public static void queueMessage(TextChannel channel, Message message, boolean allowMassPing) {
        queueMessage(channel, message, null, allowMassPing);
    }

    public static void queueMessage(TextChannel channel, String message, Consumer<Message> consumer) {
        message = translateEmotes(message, channel.getGuild());
        if (StringUtils.isBlank(message)) return;
        queueMessage(channel, new MessageBuilder().append(message).build(), consumer);
    }

    public static void queueMessage(TextChannel channel, String message, Consumer<Message> consumer, boolean allowMassPing) {
        message = translateEmotes(message, channel.getGuild());
        queueMessage(channel, new MessageBuilder().append(message).build(), consumer, allowMassPing);
    }

    public static void queueMessage(TextChannel channel, Message message, Consumer<Message> consumer) {
        queueMessage(channel, message, consumer, false);
    }

    public static void queueMessage(TextChannel channel, Message message, Consumer<Message> consumer, boolean allowMassPing) {
        if (channel == null) {
            log.debug("Tried sending a message to a null channel");
            return;
        }

        try {
            MessageAction action = channel.sendMessage(message);
            if (allowMassPing) action = action.allowedMentions(EnumSet.allOf(Message.MentionType.class));
            action.queue(sentMessage -> {
                if (consumer != null) consumer.accept(sentMessage);
            }, throwable -> log.error("Failed to send message to channel " + channel + ": " + throwable.getMessage()));
        } catch (PermissionException e) {
            if (e.getPermission() != Permission.UNKNOWN) {
                log.warn("Could not send message in channel " + channel + " because the bot does not have the \"" + e.getPermission().getName() + "\" permission");
            } else {
                log.warn("Could not send message in channel " + channel + " because \"" + e.getMessage() + "\"");
            }
        } catch (IllegalStateException e) {
            log.error("Could not send message to channel " + channel + ": " + e.getMessage());
        }
    }

    public static void setTextChannelTopic(TextChannel channel, String topic) {
        if (channel == null) {
            log.debug("Attempted to set status of null channel");
            return;
        }

        try {
            channel.getManager().setTopic(topic).queue();
        } catch (Exception e) {
            if (e instanceof PermissionException) {
                PermissionException pe = (PermissionException) e;
                if (pe.getPermission() != Permission.UNKNOWN) {
                    log.warn("Could not set topic of channel " + channel + " because the bot does not have the \"" + pe.getPermission().getName() + "\" permission");
                }
            } else {
                log.warn("Could not set topic of channel " + channel + " because \"" + e.getMessage() + "\"");
            }
        }
    }

    public static void setGameStatus(String gameStatus) {
        if (getJda() == null) {
            log.debug("Attempted to set game status using null JDA");
            return;
        }
        if (StringUtils.isBlank(gameStatus)) {
            log.debug("Attempted setting game status to a null or empty string");
            return;
        }

        getJda().getPresence().setActivity(Activity.playing(gameStatus));
    }

    public static void deleteMessage(Message message) {
        if (message.isFromType(ChannelType.PRIVATE)) return;

        try {
            message.delete().queue();
        } catch (PermissionException e) {
            if (e.getPermission() != Permission.UNKNOWN) {
                log.warn("Could not delete message in channel " + message.getTextChannel() + " because the bot does not have the \"" + e.getPermission().getName() + "\" permission");
            } else {
                log.warn("Could not delete message in channel " + message.getTextChannel() + " because \"" + e.getMessage() + "\"");
            }
        }
    }

    public static void privateMessage(User user, String message) {
        user.openPrivateChannel().queue(privateChannel ->
                privateChannel.sendMessage(message).queue()
        );
    }

    public static boolean memberHasRole(Member member, Set<String> rolesToCheck) {
        Set<String> rolesLowercase = rolesToCheck.stream().map(String::toLowerCase).collect(Collectors.toSet());
        return member.getRoles().stream().anyMatch(role -> rolesLowercase.contains(role.getName().toLowerCase()));
    }

    public static final Color DISCORD_DEFAULT_COLOR = new Color(153, 170, 181, 1);

    @Deprecated
    public static String convertRoleToMinecraftColor(Role role) {
        if (role == null) {
            log.debug("Attempted to look up color for null role");
            return "";
        }

        return Color.getColor(Objects.requireNonNull(role.getColor()).toString()).toString();
    }

    public static void setAvatar(File avatar) throws RuntimeException {
        try {
            getJda().getSelfUser().getManager().setAvatar(Icon.from(avatar)).queue();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void setAvatarBlocking(File avatar) throws RuntimeException {
        try {
            getJda().getSelfUser().getManager().setAvatar(Icon.from(avatar)).complete();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void modifyRolesOfMember(Member member, Set<Role> rolesToAdd, Set<Role> rolesToRemove) {
        rolesToAdd = rolesToAdd.stream()
                .filter(role -> !role.isManaged())
                .filter(role -> !role.getGuild().getPublicRole().getId().equals(role.getId()))
                .filter(role -> !member.getRoles().contains(role))
                .collect(Collectors.toSet());
        Set<Role> nonInteractableRolesToAdd = rolesToAdd.stream().filter(role -> !member.getGuild().getSelfMember().canInteract(role)).collect(Collectors.toSet());
        rolesToAdd.removeAll(nonInteractableRolesToAdd);
        nonInteractableRolesToAdd.forEach(role -> log.warn("Failed to add role \"" + role.getName() + "\" to \"" + member.getEffectiveName() + "\" because the bot's highest role is lower than the target role and thus can't interact with it"));

        rolesToRemove = rolesToRemove.stream()
                .filter(role -> !role.isManaged())
                .filter(role -> !role.getGuild().getPublicRole().getId().equals(role.getId()))
                .filter(role -> member.getRoles().contains(role))
                .collect(Collectors.toSet());
        Set<Role> nonInteractableRolesToRemove = rolesToRemove.stream().filter(role -> !member.getGuild().getSelfMember().canInteract(role)).collect(Collectors.toSet());
        rolesToRemove.removeAll(nonInteractableRolesToRemove);
        nonInteractableRolesToRemove.forEach(role -> log.warn("Failed to remove role \"" + role.getName() + "\" from \"" + member.getEffectiveName() + "\" because the bot's highest role is lower than the target role and thus can't interact with it"));

        member.getGuild().modifyMemberRoles(member, rolesToAdd, rolesToRemove).queue();
    }

    public static void addRoleToMember(Member member, Role role) {
        if (member == null) {
            log.debug("Can't add role to null member");
            return;
        }

        try {
            member.getGuild().addRoleToMember(member, role).queue();
        } catch (PermissionException e) {
            if (e.getPermission() != Permission.UNKNOWN) {
                log.warn("Could not add " + member + " to role " + role + " because the bot does not have the \"" + e.getPermission().getName() + "\" permission");
            } else {
                log.warn("Could not add " + member + " to role " + role + " because \"" + e.getMessage() + "\"");
            }
        }
    }

    public static void addRolesToMember(Member member, Role... roles) {
        if (member == null) {
            log.debug("Can't add roles to null member");
            return;
        }

        List<Role> rolesToAdd = Arrays.stream(roles)
                .filter(role -> !role.isManaged())
                .filter(role -> !role.getGuild().getPublicRole().getId().equals(role.getId()))
                .collect(Collectors.toList());

        try {
            member.getGuild().modifyMemberRoles(member, rolesToAdd, Collections.emptySet()).queue();
        } catch (PermissionException e) {
            if (e.getPermission() != Permission.UNKNOWN) {
                log.warn("Could not add " + member + " to role(s) " + rolesToAdd + " because the bot does not have the \"" + e.getPermission().getName() + "\" permission");
            } else {
                log.warn("Could not add " + member + " to role(s) " + rolesToAdd + " because \"" + e.getMessage() + "\"");
            }
        }
    }

    public static void addRolesToMember(Member member, Set<Role> rolesToAdd) {
        addRolesToMember(member, rolesToAdd.toArray(new Role[0]));
    }

    public static void removeRolesFromMember(Member member, Role... roles) {
        if (member == null) {
            log.debug("Can't remove roles from null member");
            return;
        }

        List<Role> rolesToRemove = Arrays.stream(roles)
                .filter(role -> !role.isManaged())
                .filter(role -> !role.getGuild().getPublicRole().getId().equals(role.getId()))
                .collect(Collectors.toList());

        try {
            member.getGuild().modifyMemberRoles(member, Collections.emptySet(), rolesToRemove).queue();
        } catch (PermissionException e) {
            if (e.getPermission() != Permission.UNKNOWN) {
                log.warn("Could not demote " + member + " from role(s) " + rolesToRemove + " because the bot does not have the \"" + e.getPermission().getName() + "\" permission");
            } else {
                log.warn("Could not demote " + member + " from role(s) " + rolesToRemove + " because \"" + e.getMessage() + "\"");
            }
        }
    }
    public static void removeRolesFromMember(Member member, Set<Role> rolesToRemove) {
        removeRolesFromMember(member, rolesToRemove.toArray(new Role[0]));
    }

    public static void setNickname(Member member, String nickname) {
        if (member == null) {
            log.debug("Can't set nickname of null member");
            return;
        }

        if (!member.getGuild().getSelfMember().canInteract(member)) {
            log.debug("Not setting " + member + "'s nickname because we can't interact with them");
            return;
        }

        if (nickname != null && nickname.equals(member.getNickname())) {
            log.debug("Not setting " + member + "'s nickname because it wouldn't change");
            return;
        }

        try {
            member.modifyNickname(nickname).queue();
        } catch (PermissionException e) {
            if (e.getPermission() != Permission.UNKNOWN) {
                log.warn("Could not set nickname for " + member + " because the bot does not have the \"" + e.getPermission().getName() + "\" permission");
            } else {
                log.warn("Could not set nickname for " + member + " because \"" + e.getMessage() + "\"");
            }
        }
    }

    public static Role getRole(String roleId) {
        try {
            return getJda().getRoleById(roleId);
        } catch (Exception ignored) {
            return null;
        }
    }
    public static Role getRoleByName(Guild guild, String roleName) {
        return guild.getRoles().stream()
                .filter(role -> role.getName().equalsIgnoreCase(roleName))
                .findFirst()
                .orElse(null);
    }

    public static void banMember(Member member) {
        banMember(member, 0);
    }
    public static void banMember(Member member, int daysOfMessagesToDelete) {
        if (member == null) {
            log.debug("Attempted to ban null member");
            return;
        }

        daysOfMessagesToDelete = Math.abs(daysOfMessagesToDelete);

        try {
            member.ban(daysOfMessagesToDelete).queue();
        } catch (PermissionException e) {
            if (e.getPermission() != Permission.UNKNOWN) {
                log.warn("Failed to ban " + member + " because the bot does not have the \"" + e.getPermission().getName() + "\" permission");
            } else {
                log.warn("Failed to ban " + member + " because \"" + e.getMessage() + "\"");
            }
        }
    }

    public static void unbanUser(Guild guild, User user) {
        try {
            guild.unban(user).queue(null, t -> log.error("Failed to unban user " + user + ": " + t.getMessage()));
        } catch (Exception e) {
            log.error("Failed to unban user " + user + ": " + e.getMessage());
        }
    }

    public static String translateEmotes(String messageToTranslate) {
        return translateEmotes(messageToTranslate, getJda().getEmotes());
    }
    public static String translateEmotes(String messageToTranslate, Guild guild) {
        return translateEmotes(messageToTranslate, guild.getEmotes());
    }
    public static String translateEmotes(String messageToTranslate, List<Emote> emotes) {
        for (Emote emote : emotes)
            messageToTranslate = messageToTranslate.replace(":" + emote.getName() + ":", emote.getAsMention());
        return messageToTranslate;
    }

    public static TextChannel getTextChannelById(String channelId) {
        try {
            return getJda().getTextChannelById(channelId);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Member getMemberById(String memberId) {
        try {
            return getJda().getGuilds().stream()
                    .filter(guild -> guild.getMemberById(memberId) != null)
                    .findFirst()
                    .map(guild -> guild.getMemberById(memberId))
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public static User getUserById(String userId) {
        try {
            return getJda().getUserById(userId);
        } catch (Exception ignored) {
            return null;
        }
    }
}