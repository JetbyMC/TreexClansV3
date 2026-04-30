package me.jetby.clans.common.gui.impl;


import me.jetby.clans.api.gui.Gui;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.member.Member;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.tools.NumberUtils;
import me.jetby.libb.gui.item.ItemWrapper;
import me.jetby.libb.gui.parser.Item;
import me.jetby.libb.gui.parser.SkullCreator;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.jetby.clans.common.TreexClans.NAMESPACED_KEY;


public class MembersGui extends Gui {

    private final TreexClans plugin;
    private final Clan clan;

    public MembersGui(@NotNull Player viewer, @NotNull FileConfiguration config, @NotNull TreexClans plugin, Clan clan) {
        super(viewer, config, plugin, clan);
        this.plugin = plugin;
        this.clan = clan;
        setupMembersPagination();

        addClickHandler("type", event -> {
            String type = event.getSection().getString("type");
            if (type==null) return;
            switch (type.toLowerCase()) {
                case "members": {
                    break;
                }
                case "leader": {
                    Member leader = clan.getLeader();
                    replaceMemberPlaceholders(leader);

                    OfflinePlayer target = Bukkit.getOfflinePlayer(leader.getUuid());
                    ItemStack itemStack = SkullCreator.itemFromName(target.getName());


                    event.getWrapper().itemStack(itemStack);

                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta != null) {
                        itemMeta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "menu_item");
                        itemStack.setItemMeta(itemMeta);
                    }
                    updateItem(event.getWrapper().key());
                    break;
                }

                case "next_page": {
                    event.setCancelled(true);
                    nextPage();
                }
                case "prev_page": {
                    event.setCancelled(true);
                    prevPage();
                }
            }
        });

        openPage(0);
    }


    @Override
    public boolean cancelRegistration(@NotNull Item item) {
        return item.type().equalsIgnoreCase("members");
    }

    private void setupMembersPagination() {

        List<Item> items = getBySectionOption("type").stream()
                .filter(b -> "members".equalsIgnoreCase(b.type()))
                .toList();

        List<Integer> slots = items.stream()
                .flatMap(item -> item.slots().stream())
                .toList();
        if (items.isEmpty()) return;


        List<Member> members = clan.getMembers().stream()
                .filter(m -> !m.equals(clan.getLeader()))
                .toList();

        contentSlots(slots.toArray(new Integer[0]));

        for (Member member : members) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(member.getUuid());

            replaceMemberPlaceholders(member);
            ItemStack itemStack = SkullCreator.itemFromName(target.getName());
            ItemWrapper wrapper = new ItemWrapper(itemStack);
            addItem(wrapper);

        }
    }

    private void replaceMemberPlaceholders(Member memberImpl) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberImpl.getUuid());
        setReplace("%joined-at%", plugin.getFormatTime().stringFormat(System.currentTimeMillis() - memberImpl.getJoinedAt()));
        setReplace("%last-online%", plugin.getClanManager().lookup().getLastOnlineFormatted(memberImpl));
        setReplace("%target_name%", offlinePlayer.getName());
        setReplace("%rank%", memberImpl.getRank().name());
        setReplace("%kills%", String.valueOf(memberImpl.getKills()));
        setReplace("%deaths%", String.valueOf(memberImpl.getDeaths()));
        setReplace("%kd%", calculateKD(memberImpl));
        setReplace("%exp%", String.valueOf(memberImpl.getExp()));
        setReplace("%coin%", String.valueOf(memberImpl.getCoin()));
    }

    private String calculateKD(Member memberImpl) {
        int kills = memberImpl.getKills();
        int deaths = memberImpl.getDeaths();
        return deaths == 0 ? kills + "" : NumberUtils.formatWithCommas((double) kills / deaths);
    }
}