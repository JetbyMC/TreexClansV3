package me.jetby.clans.common.gui.core;

import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.member.rank.Rank;
import me.jetby.clans.api.service.clan.member.rank.RankPerm;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.gui.Gui;
import me.jetby.libb.gui.parser.Item;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankPermissionGui extends Gui {


    private final Rank rank;

    public RankPermissionGui(@NotNull Player viewer, @NotNull FileConfiguration config, @NotNull JavaPlugin plugin, @NotNull Clan clan, @NotNull Rank rank) {
        super(viewer, config, plugin, clan);
        this.rank = rank;

        addClickHandler("type", event -> {
            String type = event.getSection().getString("type");
            if (type == null) return;
            String permName = type.replace("perm-", "").toUpperCase();
            RankPerm perm = RankPerm.valueOf(permName);

            if (!clan.getMember(player.getUniqueId()).getRank().perms().contains(RankPerm.SETRANK)) return;
            if (clan.getMember(player.getUniqueId()).getRank().equals(rank)) return;
            if (!clan.getLeader().getRank().perms().contains(perm)) return;
            if (!clan.getMember(player.getUniqueId()).getRank().perms().contains(perm)) return;

            if (rank.perms().contains(perm)) {
                rank.perms().remove(perm);
            } else {
                rank.perms().add(perm);
            }
        });
    }

    @Override
    public void refresh() {
        setReplace("%invite_status%", getStatus(rank.perms().contains(RankPerm.INVITE)));
        setReplace("%kick_status%", getStatus(rank.perms().contains(RankPerm.KICK)));
        setReplace("%base_status%", getStatus(rank.perms().contains(RankPerm.BASE)));
        setReplace("%setrank_status%", getStatus(rank.perms().contains(RankPerm.SETRANK)));
        setReplace("%setbase_status%", getStatus(rank.perms().contains(RankPerm.SETBASE)));
        setReplace("%deposit_status%", getStatus(rank.perms().contains(RankPerm.DEPOSIT)));
        setReplace("%withdraw_status%", getStatus(rank.perms().contains(RankPerm.WITHDRAW)));
        setReplace("%pvp_status%", getStatus(rank.perms().contains(RankPerm.PVP)));
        setReplace("%setslogan_status%", getStatus(rank.perms().contains(RankPerm.SETSLOGAN)));
        setReplace("%setprefix_status%", getStatus(rank.perms().contains(RankPerm.SETPREFIX)));
        setReplace("%rank%", rank.name());
        super.refresh();

    }

    private static String getStatus(boolean status) {
        return TreexClans.getInstance().getMessages().getCleanMessage(status ? "rank-perm-yes" : "rank-perm-no");
    }

}
