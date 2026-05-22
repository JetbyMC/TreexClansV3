package org.jetby.clans.common.gui.core;

import org.jetby.clans.api.gui.Gui;
import org.jetby.clans.api.gui.GuiContext;
import org.jetby.clans.api.gui.GuiModel;
import org.jetby.clans.api.service.clan.member.rank.Rank;
import org.jetby.clans.api.service.clan.member.rank.RankPerm;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.configurations.Config;
import org.jetby.clans.common.gui.GuiLoader;
import org.jetby.libb.gui.parser.Item;
import org.jetbrains.annotations.NotNull;

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
            Rank rank = plugin.getCfg().getRanks().get(rankName);
            if (rank == null) return;
            if (rank==plugin.getCfg().getLeaderRank()) return;

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

        List<Rank> ranks = plugin.getCfg().getRanks().values()
                .stream()
                .filter(rank ->
                        getClan().getLeader().getRank() != rank
                ).toList();
        List<Item> result = new ArrayList<>();

        for (Item item : items) {
            String rankName = item.section().getString("rank");
            if (rankName==null) {
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
                    for (int i = 0; i < Math.min(slots.size(), ranks.size()); i++) {
                        Rank rank = ranks.get(i);
                        if (rank==plugin.getCfg().getLeaderRank()) continue;
                        int slot = slots.get(i-1);

                        result.add(cloneItemForRank(item, List.of(slot), rank));
                    }
                 continue;
                }
                default: {
                    Rank rank = plugin.getCfg().getRanks().get(rankName);
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
        Item copy = new Item(item.itemStack().clone());
        copy.type("rank-" + rank.id());
        copy.slots(slots);
        copy.flags(item.flags());
        copy.enchantments(item.enchantments());
        copy.enchanted(item.enchanted());
        copy.customModelData(item.customModelData());
        copy.onClick(item.onClick());
        copy.section(item.section());
        copy.viewRequirements(item.viewRequirements());
        copy.priority(item.priority());
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

        placeholders.put("{invite_status}", getStatus(rank.perms().contains(RankPerm.INVITE)));
        placeholders.put("{kick_status}", getStatus(rank.perms().contains(RankPerm.KICK)));
        placeholders.put("{base_status}", getStatus(rank.perms().contains(RankPerm.BASE)));
        placeholders.put("{setrank_status}", getStatus(rank.perms().contains(RankPerm.SETRANK)));
        placeholders.put("{setbase_status}", getStatus(rank.perms().contains(RankPerm.SETBASE)));
        placeholders.put("{deposit_status}", getStatus(rank.perms().contains(RankPerm.DEPOSIT)));
        placeholders.put("{withdraw_status}", getStatus(rank.perms().contains(RankPerm.WITHDRAW)));
        placeholders.put("{pvp_status}", getStatus(rank.perms().contains(RankPerm.PVP)));
        placeholders.put("{setslogan_status}", getStatus(rank.perms().contains(RankPerm.SETSLOGAN)));
        placeholders.put("{setprefix_status}", getStatus(rank.perms().contains(RankPerm.SETPREFIX)));
        placeholders.put("{rank}", rank.name());

        return placeholders;
    }

    private static String getStatus(boolean status) {
        return TreexClans.getInstance().getMessages().getCleanMessage(status ? "rank-perm-yes" : "rank-perm-no");
    }


}
