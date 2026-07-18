package org.jetby.clans.common.gui.core;

import org.jetbrains.annotations.NotNull;
import org.jetby.clans.api.gui.Gui;
import org.jetby.clans.api.gui.GuiContext;
import org.jetby.clans.api.gui.GuiModel;
import org.jetby.clans.api.service.clan.member.rank.Permission;
import org.jetby.clans.api.service.clan.member.rank.PermissionRegistry;
import org.jetby.clans.api.service.clan.member.rank.Rank;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.configurations.Config;
import org.jetby.clans.common.gui.GuiLoader;
import org.jetby.libb.gui.parser.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RanksGui extends Gui {


    private final TreexClans plugin;

    public RanksGui(@NotNull GuiContext ctx) {
        super(ctx);
        this.plugin = (TreexClans) getPlugin();

        addClickHandler("rank", event -> {
            String rankName = event.getItem().type().replace("rank-", "");
            Rank rank = getClan().getRanks().get(rankName);
            if (rank == null) return;
            if (rank == plugin.getCfg().getLeaderRank()) return;

            ((TreexClans) getPlugin()).getGuiFactory().create(GuiContext.of(
                                    getPlugin(),
                                    GuiLoader.getGuiConfiguration(GuiModel.RANK_PERMISSIONS),
                                    getViewer(),
                                    getClan(), Config.CONFIG_COLORIZER)
                            .with(rank))
                    .open(getViewer());
        });
    }

    @Override
    public void buildItems(List<Item> items) {
        if (getClan() == null) return;


        List<Rank> members = getClan().getRanks().values()
                .stream()
                .filter(rank ->
                        getClan().getLeader().getRank() != rank
                ).toList();

        List<Rank> ranks = getClan().getRanks().values()
                .stream()
                .toList();

        List<Item> result = new ArrayList<>();

        for (Item item : items) {
            String rankName = item.section().getString("rank");
            if (rankName == null) {
                result.add(item);
                continue;
            }
            switch (rankName.toLowerCase()) {
                case "all": {
                    List<Integer> slots = item.slots();
                    for (int i = 0; i < Math.min(slots.size(), ranks.size()); i++) {
                        Rank rank = ranks.get(i);
                        int slot = slots.get(i);

                        result.add(cloneItemForRank(item, List.of(slot), rank));
                    }
                    continue;
                }
                case "members": {
                    List<Integer> slots = item.slots();
                    for (int i = 0; i < Math.min(slots.size(), members.size()); i++) {
                        Rank rank = members.get(i);
                        int slot = slots.get(i);

                        result.add(cloneItemForRank(item, List.of(slot), rank));
                    }
                    continue;
                }
                case "leader": {
                    getClan().getRanks().values().stream().filter(r -> plugin.getCfg().getLeaderRank().equals(r)).findFirst().ifPresent(r -> {
                        result.add(cloneItemForRank(item, item.slots(), r));
                    });
                    continue;
                }
                default: {
                    Rank rank = getClan().getRanks().get(rankName);
                    if (rank == null) {
                        TreexClans.LOGGER.error("Rank '" + rankName + "' not found");
                        continue;
                    }
                    result.add(cloneItemForRank(item, item.slots(), rank));
                    continue;
                }
            }

        }
        super.buildItems(result);
    }

    private Item cloneItemForRank(Item item, List<Integer> slots, Rank rank) {
        Item copy = item.clone();
        copy.type("rank-" + rank.id());
        copy.slots(slots);
        copy.displayName(item.displayName() == null ? null : applyRankPlaceholders(applyPlaceholders(item.displayName()), rank));
        copy.lore(item.lore() == null ? null : item.lore()
                .stream()
                .map(line -> applyRankPlaceholders(applyPlaceholders(line), rank))
                .toList());
        return copy;
    }

    private static String applyRankPlaceholders(String text, Rank rank) {
        for (Map.Entry<String, String> entry : placeholders(rank).entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }

    private static Map<String, String> placeholders(Rank rank) {
        Map<String, String> placeholders = new HashMap<>();

        for (Permission perm : PermissionRegistry.getAll()) {
            placeholders.put("{" + perm.getId().toLowerCase() + "_status}", getStatus(rank.perms().contains(perm)));
        }
        placeholders.put("{rank}", rank.name());

        return placeholders;
    }

    private static String getStatus(boolean status) {
        return TreexClans.getInstance().getMessages().getCleanMessage(status ? "rank-perm-yes" : "rank-perm-no");
    }


}
