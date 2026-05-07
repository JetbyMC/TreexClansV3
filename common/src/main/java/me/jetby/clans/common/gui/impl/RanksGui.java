package me.jetby.clans.common.gui.impl;


import me.jetby.clans.common.gui.ExtendedGui;
import me.jetby.clans.common.gui.Gui;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.member.rank.Rank;
import me.jetby.clans.api.service.clan.member.rank.RankPerm;
import me.jetby.clans.common.TreexClans;
import me.jetby.libb.gui.item.ItemWrapper;
import me.jetby.libb.gui.parser.Item;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RanksGui extends Gui {

    private final Clan clan;
    private final TreexClans plugin;

    public RanksGui(@NotNull Player viewer, @NotNull ExtendedGui guiData,
                    @NotNull TreexClans plugin, Clan clan) {
        super(viewer, guiData, plugin, clan);

        this.clan = clan;
        this.plugin = plugin;

        setupRanksPagination();

        openPage(0);

        addClickHandler("type", event -> {
            String type = event.getSection().getString("type");
            if (type==null) return;
            switch (type.toLowerCase()) {
                case "all_ranks": {
                    break;
                }
                case "leader_rank": {
                    Rank rank = clan.getLeader().getRank();
                    replaceMemberPlaceholders(rank);
                    break;
                }
                case "prev_page": {
                    event.setCancelled(true);
                    prevPage();
                }
                case "next_page": {
                    event.setCancelled(true);
                    nextPage();
                }
            }
        });
    }

    @Override
    public boolean cancelRegistration(@NotNull Item item) {
        return item.type()!=null && item.type().equalsIgnoreCase("all_ranks");
    }

    private void setupRanksPagination() {
        List<Item> items = getBySectionOption("type").stream()
                .filter(b -> "all_ranks".equals(b.type()))
                .toList();

        List<Integer> slots = items.stream()
                .flatMap(i -> i.slots().stream())
                .distinct()
                .toList();

        if (items.isEmpty()) return;

        contentSlots(slots.toArray(new Integer[0]));


        Map<String, Rank> ranks = new HashMap<>(clan.getRanks());
        ranks.remove(clan.getLeader().getRank().id());
        List<String> ranksStr = new ArrayList<>(ranks.keySet());

        for (Map.Entry<String, Rank> entry : ranks.entrySet()) {
            String rankId = entry.getKey();
            Rank rank = entry.getValue();
            replaceMemberPlaceholders(rank);

            ItemWrapper wrapper = new ItemWrapper(Material.PAPER); // todo not paper
            wrapper.onClick(event -> {

                event.setCancelled(true);
                // todo sex
//                Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
//                        InstanceFactory.GUI_FACTORY.create(
//                                        getPlugin(),
//                                        getPlugin().getGuiLoader().getMenus().get(button.openGui()),
//                                        getViewer(), clan, rank)
//                                .open(getViewer()), 1L);
            });

        }

    }

    private void replaceMemberPlaceholders(Rank rank) {
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
    }

    private String getStatus(boolean status) {
        if (status) {
            return plugin.getMessages().getCleanMessage("rank-perm-yes");
        } else {
            return plugin.getMessages().getCleanMessage("rank-perm-no");
        }
    }

}
