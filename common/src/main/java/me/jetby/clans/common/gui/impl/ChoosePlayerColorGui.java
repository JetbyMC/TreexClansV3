package me.jetby.clans.common.gui.impl;


import me.jetby.clans.common.gui.Gui;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.member.Member;
import me.jetby.clans.common.TreexClans;
import me.jetby.libb.gui.item.ItemWrapper;
import me.jetby.libb.gui.parser.Item;
import me.jetby.libb.gui.parser.SkullCreator;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChoosePlayerColorGui extends Gui {


    private final Clan clan;

    public ChoosePlayerColorGui(@NotNull Player viewer, @NotNull FileConfiguration config, @NotNull TreexClans plugin, Clan clan) {
        super(viewer, config, plugin, clan);
        this.clan = clan;

        setupMembersPagination();

        openPage(0);

        addClickHandler("type", event -> {
            String type = event.getSection().getString("type");
            if (type == null) return;

            switch (type.toLowerCase()) {
                case "players": {
                    break;
                }
                case "next_page": {
                    event.setCancelled(true);
                    nextPage();
                    break;
                }
                case "prev_page": {
                    event.setCancelled(true);
                    prevPage();
                    break;
                }

            }
        });
    }


    @Override
    public boolean cancelRegistration(@NotNull Item item) {
        return item.type()!=null && item.type().equalsIgnoreCase("players");
    }

    @Override
    public void everyPageLogic() {

    }

    private void setupMembersPagination() {

        List<Item> items = getBySectionOption("type").stream()
                .filter(b -> "players".equalsIgnoreCase(b.type()))
                .toList();


        List<Integer> slots = items.stream()
                .flatMap(item -> item.slots().stream())
                .toList();

        if (items.isEmpty()) return;

        contentSlots(slots.toArray(new Integer[0]));

        List<Member> members = new ArrayList<>(clan.getMembers());
        members.add(clan.getLeader());
        members.removeIf(m -> m.getUuid().equals(getViewer().getUniqueId()));


        Item item = items.get(0);


        for (Member member : members) {
            if (member.equals(clan.getMember(getViewer().getUniqueId()))) continue;

            OfflinePlayer target = Bukkit.getOfflinePlayer(member.getUuid());

            setReplace("%target_name%", target.getName());

            ItemStack itemStack = SkullCreator.itemFromName(target.getName());
            ItemMeta meta = itemStack.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            itemStack.setItemMeta(meta);

            ItemWrapper wrapper = new ItemWrapper(itemStack);

            wrapper.displayName(item.displayName());
            wrapper.setLore(item.lore());

            wrapper.customModelData(item.customModelData());
            wrapper.enchanted(item.enchanted());

            wrapper.onClick((event) -> {
                event.setCancelled(true);

                // TODO sex
//                Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
//                        InstanceFactory.GUI_FACTORY.create(
//                                        getPlugin(),
//                                        getPlugin().getGuiLoader().getMenus().get(button.openGui()),
//                                        getViewer(), clan, member)
//                                .open(getViewer()), 1L);

            });

            addItem(wrapper);

        }
    }

}