package me.jetby.clans.common.gui.impl;


import me.jetby.clans.common.gui.Gui;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.member.rank.Rank;
import me.jetby.clans.api.service.clan.member.rank.RankPerms;
import me.jetby.clans.common.TreexClans;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;


public class RankPermissionsGui extends Gui {
    private final Rank rank;
    private final TreexClans plugin;
    private final Clan clan;

    public RankPermissionsGui(@NotNull Player viewer, @NotNull FileConfiguration config,
                              @NotNull TreexClans plugin, Clan clan, Rank rank) {
        super(viewer, config, plugin, clan);
        this.clan = clan;
        this.rank = rank;
        this.plugin = plugin;



        addClickHandler("type", event -> {
            String type = event.getSection().getString("type");
            if (type == null) return;
            String permName = type.replace("perm-", "").toUpperCase();
            RankPerms perm = RankPerms.valueOf(permName);

            if (!clan.getMember(player.getUniqueId()).getRank().perms().contains(RankPerms.SETRANK)) return;
            if (clan.getMember(player.getUniqueId()).getRank().equals(rank)) return;
            if (!clan.getLeader().getRank().perms().contains(perm)) return;
            if (!clan.getMember(player.getUniqueId()).getRank().perms().contains(perm)) return;

            if (rank.perms().contains(perm)) {
                rank.perms().remove(perm);
            } else {
                rank.perms().add(perm);
            }

            Material material = rank.perms().contains(perm) ? Material.LIME_DYE : Material.RED_DYE;
            event.getWrapper().material(material);
            replaceMemberPlaceholders(rank.id());
            updateItem(event.getWrapper().key());

        });
        openPage(0);
    }


    private void replaceMemberPlaceholders(String rankName) {
        Rank rank = clan.getRanks().get(rankName);
        if (rank == null) return;

        Set<RankPerms> perms = EnumSet.allOf(RankPerms.class);
        for (RankPerms perm : perms) {
            setReplace("%" + perm.name().toLowerCase() + "_status%", getStatus(rank.perms().contains(perm)));
        }
        setReplace("%rank%", rank.name());

    }

    private String getStatus(boolean status) {
        return plugin.getMessages().getCleanMessage(status ? "rank-perm-yes" : "rank-perm-no");
    }

}

