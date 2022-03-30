package ru.xfenilafs.lobby.discord;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.redline.core.global.group.PermissionGroup;

@Getter
@RequiredArgsConstructor
public enum DiscordGroup {
    VIP("734367946268344331", PermissionGroup.VIP),
    GOLD("681701825472364668", PermissionGroup.GOLD),
    DIAMOND("681702167286906941", PermissionGroup.DIAMOND),
    EMERALD("681702352390062107", PermissionGroup.EMERALD),
    REDLINE("681702550906339372", PermissionGroup.REDLINE),
    REDLINE_PLUS("681702792590917691", PermissionGroup.REDLINE_PLUS),
    SPONSOR("692123123415384145", PermissionGroup.SPONSOR),
    SPONSOR_PLUS("692123326314709103", PermissionGroup.SPONSOR_PLUS),
    YOUTUBE("692687326228250665", PermissionGroup.YOUTUBE),
    TESTER("680500939026989106", PermissionGroup.TESTER),
    HELPER("680500678548127778", PermissionGroup.HELPER),
    MODERATOR("680500656062464167", PermissionGroup.MODERATOR),
    SRMODERATOR("808753933760987167", PermissionGroup.SRMODERATOR),
    BUILDER("680500751855910942", PermissionGroup.BUILDER),
    SRBUILDER("865623056210460702", PermissionGroup.SRBUILDER),
    ADMIN("688124478982717595", PermissionGroup.ADMIN),
    DEVELOPER("680500542669324383", PermissionGroup.DEVELOPER);

    private final String id;
    private final PermissionGroup permissionGroup;
}
