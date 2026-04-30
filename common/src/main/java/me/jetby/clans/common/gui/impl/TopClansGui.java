package me.jetby.clans.common.gui.impl;


import me.jetby.clans.api.gui.Gui;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.leaderboard.LeaderboardService;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.tools.NumberUtils;
import me.jetby.libb.gui.item.ItemWrapper;
import me.jetby.libb.gui.parser.Item;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.jetby.clans.common.TreexClans.NAMESPACED_KEY;

public class TopClansGui extends Gui {

    private final TreexClans plugin;
    private final LeaderboardService.TopType currentSort;
    private int s;

    public TopClansGui(@NotNull Player viewer, @NotNull FileConfiguration config, @NotNull TreexClans plugin, Clan clan, LeaderboardService.TopType topType) {
        super(viewer, config, plugin, clan);
        this.plugin = plugin;
        this.s = s;
        this.currentSort = Objects.requireNonNullElse(topType, LeaderboardService.TopType.KILLS);

        setupMembersPagination();
        openPage(0);


    }

    public void sex() {

        addClickHandler("type", event -> {
            String type = event.getSection().getString("type");
            if (type == null) return;
            ItemWrapper wrapper = event.getWrapper();
            switch (type.toLowerCase()) {
                case "clans": {
                    break;
                }
                case "filter": {
                    // todo sex
                    //                    if (s + 1 > getTops(w).size()) s = 0;

                    wrapper.onClick(event1 -> {
                        // todo sex
//                        close(player);
//                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
//                            Gui gui = InstanceFactory.GUI_FACTORY.create(plugin, getMenu(), player, getClanImpl(), getTops(button).get(s), s + 1);
//                            gui.open(player);
//                        }, 1L);
                    });
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

    }

    @Override
    public boolean cancelRegistration(@NotNull Item item) {
        return (item.type().equalsIgnoreCase("clans"));
    }

    private void setupMembersPagination() {
        List<Item> items = getBySectionOption("type").stream()
                .filter(b -> "clans".equalsIgnoreCase(b.type()))
                .toList();

        List<Integer> slots = items.stream()
                .flatMap(i -> i.slots().stream())
                .distinct()
                .toList();

        if (items.isEmpty()) return;

        contentSlots(slots.toArray(new Integer[0]));


        List<Clan> clans = new ArrayList<>();
        int maxClansToShow = 10000;

        for (int i = 1; i <= maxClansToShow; i++) {
            Clan clanImpl = plugin.getLeaderboardService().getTopClan(currentSort, i);
            if (clanImpl != null) {
                clans.add(clanImpl);
            } else {
                break;
            }
        }

        if (clans.isEmpty()) {
            Bukkit.getLogger().warning("Top clans list is empty!");
            return;
        }


        int top = 1;
        for (Clan clan : clans) {

            setPlaceholders(clan);
            // todo sex it was  SkullCreator.itemFromUuid(clan.getLeader().getUuid());
            ItemStack itemStack = new ItemStack(Material.PAPER);
            ItemMeta meta = itemStack.getItemMeta();
            meta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "clans");
            itemStack.setItemMeta(meta);
            ItemWrapper wrapper = new ItemWrapper(itemStack);

            setReplace("%top_num%", String.valueOf(top));
            addItem(wrapper);

            top++;
        }
    }

    private List<LeaderboardService.TopType> getTops(Item item) {
        List<LeaderboardService.TopType> list = new ArrayList<>();
        for (String s : item.lore()) {
            if (s.contains("%top_kills_set%")) {
                list.add(LeaderboardService.TopType.KILLS);
                continue;
            }
            if (s.contains("%top_deaths_set%")) {
                list.add(LeaderboardService.TopType.DEATHS);
                continue;
            }
            if (s.contains("%top_kd_set%")) {
                list.add(LeaderboardService.TopType.KD);
                continue;
            }
            if (s.contains("%top_balance_set%")) {
                list.add(LeaderboardService.TopType.BALANCE);
                continue;
            }
            if (s.contains("%top_level_set%")) {
                list.add(LeaderboardService.TopType.LEVEL);
                continue;
            }
            if (s.contains("%top_members_set%")) {
                list.add(LeaderboardService.TopType.MEMBERS);
            }

        }

        return list;
    }

    private void setPlaceholders(Clan clanImpl) {

        int kills = 0;
        int deaths = 0;
        for (var memberImpl : clanImpl.getMembersWithLeader()) {
            kills += memberImpl.getKills();
            deaths += memberImpl.getDeaths();
        }

        if (clanImpl.getPrefix() != null) {
            setReplace("%prefix%", clanImpl.getPrefix());
        } else {
            setReplace("%prefix%", clanImpl.getId().toUpperCase());
        }

        OfflinePlayer leader = Bukkit.getOfflinePlayer(clanImpl.getLeader().getUuid());
        String leaderName = leader.getName() != null ? leader.getName() : "Unknown";
        setReplace("%slogan%", clanImpl.getSlogan());
        setReplace("%tag%", clanImpl.getId());
        setReplace("%level%", clanImpl.getLevel().id());
        setReplace("%leader_name%", leaderName);
        setReplace("%kills%", String.valueOf(kills));
        setReplace("%deaths%", String.valueOf(deaths));
        setReplace("%kd%", calculateKD(kills, deaths));
        setReplace("%balance%", String.valueOf(clanImpl.getBalance()));
    }

    private String getCurrentSort(String text) {
        if (text == null) return null;
        switch (currentSort) {
            case KILLS -> setReplace("%top_kills_set%", plugin.getMessages().getCleanMessage("gui.tops.kills.set"));
            case DEATHS ->
                    setReplace("%top_deaths_set%", plugin.getMessages().getCleanMessage("gui.tops.deaths.set"));
            case KD -> setReplace("%top_kd_set%", plugin.getMessages().getCleanMessage("gui.tops.kd.set"));
            case BALANCE ->
                    setReplace("%top_balance_set%", plugin.getMessages().getCleanMessage("gui.tops.balance.set"));
            case LEVEL -> setReplace("%top_level_set%", plugin.getMessages().getCleanMessage("gui.tops.level.set"));
            case MEMBERS ->
                    setReplace("%top_members_set%", plugin.getMessages().getCleanMessage("gui.tops.members.set"));
        }
        setReplace("%top_kills_set%", plugin.getMessages().getCleanMessage("gui.tops.kills.unset"));
        setReplace("%top_deaths_set%", plugin.getMessages().getCleanMessage("gui.tops.deaths.unset"));
        setReplace("%top_kd_set%", plugin.getMessages().getCleanMessage("gui.tops.kd.unset"));
        setReplace("%top_balance_set%", plugin.getMessages().getCleanMessage("gui.tops.balance.unset"));
        setReplace("%top_level_set%", plugin.getMessages().getCleanMessage("gui.tops.level.unset"));
        setReplace("%top_members_set%", plugin.getMessages().getCleanMessage("gui.tops.members.unset"));
        return text;
    }

    private String calculateKD(int kills, int deaths) {
        return deaths == 0 ? kills + "" : NumberUtils.formatWithCommas((double) kills / deaths);
    }

}