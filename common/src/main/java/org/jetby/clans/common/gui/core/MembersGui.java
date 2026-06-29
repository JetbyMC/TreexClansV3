package org.jetby.clans.common.gui.core;

import org.jetby.clans.api.gui.Gui;
import org.jetby.clans.api.gui.GuiContext;
import org.jetby.clans.api.service.clan.member.Member;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.tools.NumberUtils;
import org.jetby.libb.gui.parser.Item;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MembersGui extends Gui {

    public MembersGui(@NotNull GuiContext ctx) {
        super(ctx);
    }

    @Override
    public void buildItems(List<Item> items) {
        if (getClan() == null) return;
        List<Item> result = new ArrayList<>();

        Item membersItem = null;

        for (Item item : items) {
            if ("members".equalsIgnoreCase(item.type())) {
                membersItem = item;
                continue;
            }
            if ("leader".equalsIgnoreCase(item.type())) {
                result.add(cloneItemForRank(item, item.slots(), getClan().getLeader()));
                continue;
            }
            result.add(item);
        }

        if (membersItem != null) {
            List<Integer> memberSlots = membersItem.slots();
            contentSlots(memberSlots.toArray(new Integer[0]));

            for (Member member : getClan().getMembers()) {
                Item cloned = cloneItemForRank(membersItem, memberSlots, member);
                addItem(buildItemWrapper(cloned));
            }
        }

        super.buildItems(result);
    }

    private Item cloneItemForRank(Item item, List<Integer> slots, Member member) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(member.getUuid());
        ItemStack clone = item.itemStack().clone();
        if (item.itemStack().getType() == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) clone.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(player);
            }
            clone.setItemMeta(meta);
        }

        Item copy = new Item(clone);
        copy.type("member-" + member.getUuid());
        copy.slots(slots);
        copy.flags(item.flags());
        copy.enchantments(item.enchantments());
        copy.enchanted(item.enchanted());
        copy.customModelData(item.customModelData());
        copy.onClick(item.onClick());
        copy.section(item.section());
        copy.viewRequirements(item.viewRequirements());
        copy.priority(item.priority());
        copy.displayName(item.displayName() == null ? null : applyRankPlaceholders(
                applyPlaceholders(item.displayName()), member));
        copy.lore(item.lore() == null ? null : item.lore()
                .stream()
                .map(line -> applyRankPlaceholders(applyPlaceholders(line), member))
                .toList());
        return copy;
    }

    private static String applyRankPlaceholders(String text, Member member) {
        for (Map.Entry<String, String> entry : placeholders(member).entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }

    private static Map<String, String> placeholders(Member member) {
        Map<String, String> placeholders = new HashMap<>();

        OfflinePlayer player = Bukkit.getOfflinePlayer(member.getUuid());
        placeholders.put("{name}", player.getName());
        placeholders.put("{rank}", member.getRank().name());
        placeholders.put("{joined-at}", TreexClans.getInstance().getFormatTime().stringFormat(System.currentTimeMillis() - member.getJoinedAt()));
        placeholders.put("{last-online}", TreexClans.getInstance().getClanManager().getLastOnlineFormatted(member));
        placeholders.put("{exp}", String.valueOf(member.getExp()));
        placeholders.put("{coin}", String.valueOf(member.getCoin()));
        placeholders.put("{kills}", String.valueOf(member.getKills()));
        placeholders.put("{deaths}", String.valueOf(member.getDeaths()));
        placeholders.put("{kd}", calculateKD(member));

        return placeholders;
    }

    private static String calculateKD(Member member) {
        int kills = member.getKills();
        int deaths = member.getDeaths();
        return deaths == 0 ? kills + "" : NumberUtils.formatWithCommas((double) kills / deaths);
    }
}
