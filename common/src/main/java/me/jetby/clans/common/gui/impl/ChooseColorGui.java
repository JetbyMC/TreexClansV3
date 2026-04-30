package me.jetby.clans.common.gui.impl;


import me.jetby.clans.api.gui.Gui;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.member.Member;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.functions.glow.Equipment;
import org.bukkit.Color;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChooseColorGui extends Gui {


    public ChooseColorGui(@NotNull Player viewer, @NotNull FileConfiguration config, @NotNull TreexClans plugin, Clan clan, Member target) {
        super(viewer, config, plugin, clan);

        addClickHandler("type", event -> {

            String type = event.getSection().getString("type");
            if (type == null) return;

            Member member = clan.getMember(player.getUniqueId());
            Color color = Equipment.getColorByName(type.replace("color-", ""));

            if (target != null) {
                plugin.getClanManager().colors().setColor(member, target, color);
                if (plugin.getGlow().hasObserver(getViewer())) {
                    plugin.getGlow().removeObserver(getViewer());
                    plugin.getGlow().addObserver(getViewer(), clan.getMembers());
                }
                return;
            }

            plugin.getClanManager().colors().setColor(clan, member, color);

            if (plugin.getGlow().hasObserver(player)) {
                plugin.getGlow().removeObserver(player);
                plugin.getGlow().addObserver(player, clan.getMembers());
            }


        });

    }

}

