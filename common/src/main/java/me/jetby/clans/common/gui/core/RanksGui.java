package me.jetby.clans.common.gui.core;

import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.member.rank.Rank;
import me.jetby.clans.api.service.clan.member.rank.RankPerm;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.gui.*;
import me.jetby.libb.gui.parser.Item;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RanksGui extends Gui {

    private final TreexClans plugin;


    public RanksGui(@NotNull Player viewer,
                    @NotNull FileConfiguration config,
                    @NotNull JavaPlugin plugin,
                    @NotNull Clan clan
    ) {
        super(viewer, config, plugin, clan);
        this.plugin = (TreexClans) plugin;

        addClickHandler("type", event -> {
            if (!event.getSection().getString("type").equalsIgnoreCase("all_ranks")) return;
            String type = event.getItem().type();
            if (type == null) return;
            if (!type.startsWith("rank-")) return;
            String rankName = type.replace("rank-", "");
            Rank rank = getClan().getRanks().get(rankName);
            if (rank == null) return;

            GuiFactory.create(GuiFactoryRequest
                            .builder()
                            .player(getViewer())
                            .clan(getClan())
                            .rank(rank)
                            .configuration(GuiLoader.getGuiConfiguration(GuiType.RANK_PERMISSIONS))
                            .plugin((TreexClans) plugin)
                            .build())
                    .open(player);
        });
    }

    @Override
    public void buildItems(List<Item> items) {
        if (getClan() == null) return;

        List<Rank> ranks = getClan().getRanks().values()
                .stream()
                .filter(rank ->
                        getClan().getLeader().getRank() != rank
                ).toList();
        List<Item> result = new ArrayList<>();

        for (Item item : items) {
            if (!("all_ranks").equalsIgnoreCase(item.type())) {
                result.add(item);
                continue;
            }

            List<Integer> slots = item.slots();
            for (int i = 0; i < Math.min(slots.size(), ranks.size()); i++) {
                Rank rank = ranks.get(i);
                int slot = slots.get(i);

                result.add(cloneItemForRank(item, slot, rank));
            }
        }
        super.buildItems(result);
    }

    @Override
    public void everyPageLogic() {

    }

    private Item cloneItemForRank(Item item, int slot, Rank rank) {
        Item copy = new Item(item.itemStack().clone());
        copy.type("rank-" + rank.id());
        copy.slots(new ArrayList<>(List.of(slot)));
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

        placeholders.put("%invite_status%", getStatus(rank.perms().contains(RankPerm.INVITE)));
        placeholders.put("%kick_status%", getStatus(rank.perms().contains(RankPerm.KICK)));
        placeholders.put("%base_status%", getStatus(rank.perms().contains(RankPerm.BASE)));
        placeholders.put("%setrank_status%", getStatus(rank.perms().contains(RankPerm.SETRANK)));
        placeholders.put("%setbase_status%", getStatus(rank.perms().contains(RankPerm.SETBASE)));
        placeholders.put("%deposit_status%", getStatus(rank.perms().contains(RankPerm.DEPOSIT)));
        placeholders.put("%withdraw_status%", getStatus(rank.perms().contains(RankPerm.WITHDRAW)));
        placeholders.put("%pvp_status%", getStatus(rank.perms().contains(RankPerm.PVP)));
        placeholders.put("%setslogan_status%", getStatus(rank.perms().contains(RankPerm.SETSLOGAN)));
        placeholders.put("%setprefix_status%", getStatus(rank.perms().contains(RankPerm.SETPREFIX)));
        placeholders.put("%rank%", rank.name());

        return placeholders;
    }

    private static String getStatus(boolean status) {
        return TreexClans.getInstance().getMessages().getCleanMessage(status ? "rank-perm-yes" : "rank-perm-no");
    }


}
