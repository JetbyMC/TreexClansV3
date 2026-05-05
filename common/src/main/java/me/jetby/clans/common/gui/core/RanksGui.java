package me.jetby.clans.common.gui.core;

import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.member.rank.Rank;
import me.jetby.clans.api.service.clan.member.rank.RankPerms;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.gui.Gui;
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


    public RanksGui(@NotNull Player viewer, @NotNull FileConfiguration config, @NotNull JavaPlugin plugin, @NotNull Clan clan) {
        super(viewer, config, plugin, clan);
        this.plugin = (TreexClans) plugin;

    }

    @Override
    public void buildItems(List<Item> items) {

        List<Rank> ranks = getClan().getRanks().values().stream().toList();
        List<Item> result = new ArrayList<>();

        for (Item item : items) {
            if (!"all_ranks".equalsIgnoreCase(item.type())) {
                result.add(item);
                continue;
            }

            List<Integer> slots = item.slots();
            for (int i = 0; i < Math.min(slots.size(), ranks.size()); i++) {
                result.add(cloneItemForRank(item, slots.get(i), ranks.get(i)));
            }
        }

        for (Item item : result) {
            System.out.println("SLOT: " + item.slots() + " | NAME: " + item.displayName() + " | LORE[0]: " + (item.lore() != null && !item.lore().isEmpty() ? item.lore().get(0) : "null"));
        }
        super.buildItems(result);
    }
    private String applyRankString(String text, Rank rank) {
        for (Map.Entry<String, String> entry : placeholders(rank).entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }

    private Item cloneItemForRank(Item item, int slot, Rank rank) {
        Item copy = new Item(item.itemStack().clone());
        copy.type(item.type());
        copy.slots(new ArrayList<>(List.of(slot)));
        copy.flags(item.flags());
        copy.enchantments(item.enchantments());
        copy.enchanted(item.enchanted());
        copy.customModelData(item.customModelData());
        copy.onClick(item.onClick());
        copy.section(item.section());
        copy.viewRequirements(item.viewRequirements());
        copy.priority(item.priority());
        copy.displayName(item.displayName() == null ? null : applyRankString(item.displayName(), rank));
        copy.lore(item.lore() == null ? null : item.lore().stream().map(line -> applyRankString(line, rank)).toList());
        return copy;
    }

    private void setup() {


    }

    @Override
    public void everyPageLogic() {

    }

    private Item applyRankPlaceholders(Item item, Rank rank) {
        if (item.lore() == null) return item;

        for (Map.Entry<String, String> entry : placeholders(rank).entrySet()) {
            if (item.displayName() == null) break;
            if (!item.displayName().contains(entry.getKey())) continue;
            item.displayName().replace(entry.getKey(), entry.getValue());
        }

        List<String> lore = item.lore();
        for (String line : lore) {
            for (Map.Entry<String, String> entry : placeholders(rank).entrySet()) {
                if (line == null) break;
                if (!line.contains(entry.getKey())) continue;
                line.replace(entry.getKey(), entry.getValue());
            }
        }
        item.lore(lore);
        return item;
    }

    private Map<String, String> placeholders(Rank rank) {
        Map<String, String> placeholders = new HashMap<>();

        placeholders.put("%invite_status%", getStatus(rank.perms().contains(RankPerms.INVITE)));
        placeholders.put("%kick_status%", getStatus(rank.perms().contains(RankPerms.KICK)));
        placeholders.put("%base_status%", getStatus(rank.perms().contains(RankPerms.BASE)));
        placeholders.put("%setrank_status%", getStatus(rank.perms().contains(RankPerms.SETRANK)));
        placeholders.put("%setbase_status%", getStatus(rank.perms().contains(RankPerms.SETBASE)));
        placeholders.put("%deposit_status%", getStatus(rank.perms().contains(RankPerms.DEPOSIT)));
        placeholders.put("%withdraw_status%", getStatus(rank.perms().contains(RankPerms.WITHDRAW)));
        placeholders.put("%pvp_status%", getStatus(rank.perms().contains(RankPerms.PVP)));
        placeholders.put("%setslogan_status%", getStatus(rank.perms().contains(RankPerms.SETSLOGAN)));
        placeholders.put("%setprefix_status%", getStatus(rank.perms().contains(RankPerms.SETPREFIX)));
        placeholders.put("%rank%", rank.name());

        return placeholders;
    }


    private String getStatus(boolean status) {
        if (status) {
            return plugin.getMessages().getCleanMessage("rank-perm-yes");
        } else {
            return plugin.getMessages().getCleanMessage("rank-perm-no");
        }
    }


}
