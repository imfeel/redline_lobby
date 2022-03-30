package ru.xfenilafs.lobby.discord;

import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.entity.Player;
import ru.redline.core.global.database.RemoteDatabaseTable;
import ru.redline.core.global.database.query.row.ValueQueryRow;
import ru.redline.core.global.group.PermissionGroup;
import ru.redline.core.global.group.player.User;
import ru.xfenilafs.lobby.Main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class DiscordManager {
    public static boolean created = false;

    public static boolean isCreated(String name) {
        RemoteDatabaseTable discordTable = Main.getInstance().getGameConnector().getTable("discord");

        discordTable.newDatabaseQuery()
                .selectQuery()

                .queryRow(new ValueQueryRow("name", name))

                .executeQueryAsync(Main.getInstance().getGameConnector())
                .thenAccept(result -> created = result.next());

        return created;
    }

    public static void setDiscordId(String name, String discordId) {
        RemoteDatabaseTable discordTable = Main.getInstance().getGameConnector().getTable("discord");

        discordTable.newDatabaseQuery()
                .selectQuery()

                .queryRow(new ValueQueryRow("name", name))

                .executeQueryAsync(Main.getInstance().getGameConnector())
                .thenAccept(result -> {
                    if (!result.next()) {
                        discordTable.newDatabaseQuery()
                                .insertQuery()

                                .queryRow(new ValueQueryRow("name", name))
                                .queryRow(new ValueQueryRow("discordId", discordId))

                                .executeSync(Main.getInstance().getGameConnector());
                    }
                });
    }

    public static void setDiscordGroup(Member member, Player player) {
        member.getRoles().forEach(role -> Main.getInstance().getGuild().removeRoleFromMember(member, role).queue());

        List<DiscordGroup> staffGroups = Arrays.asList(DiscordGroup.HELPER, DiscordGroup.MODERATOR, DiscordGroup.SRMODERATOR, DiscordGroup.BUILDER, DiscordGroup.SRBUILDER, DiscordGroup.ADMIN, DiscordGroup.DEVELOPER);
        Arrays.asList(DiscordGroup.values()).forEach(group -> {
            if (User.getUser(player.getName()).getGroup() == group.getPermissionGroup() || User.getUser(player.getName()).getPrimanyGroup() == group.getPermissionGroup()) {
                if (group.getId() != null)
                    if (staffGroups.contains(group)) {
                        Main.getInstance().getGuild()
                                .addRoleToMember(member, Objects.requireNonNull(
                                        Main.getInstance().getGuild().getRoleById("688136792720801829")))
                                .queue();
                    }
                if (group.getPermissionGroup() == PermissionGroup.ADMIN || group.getPermissionGroup() == PermissionGroup.DEVELOPER) {
                    Main.getInstance().getGuild()
                            .addRoleToMember(member, Objects.requireNonNull(
                                    Main.getInstance().getGuild().getRoleById("688121759555387431")))
                            .queue();
                }
                    Main.getInstance().getGuild()
                            .addRoleToMember(member, Objects.requireNonNull(
                                    Main.getInstance().getGuild().getRoleById(group.getId())))
                            .queue();
            }
        });

        Main.getInstance().getGuild().addRoleToMember(member, Objects.requireNonNull(Main.getInstance().getGuild().getRoleById("770189656234000385"))).queue();
        Main.getInstance().getGuild().addRoleToMember(member, Objects.requireNonNull(Main.getInstance().getGuild().getRoleById("700727469296320652"))).queue();
    }
}
