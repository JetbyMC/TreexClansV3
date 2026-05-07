package me.jetby.clans.common.gui;

import lombok.Getter;
import me.jetby.clans.api.service.leaderboard.LeaderboardService;
import me.jetby.clans.common.gui.impl.*;

@Getter
public class GuiFactory {

    public static Gui create(GuiFactoryRequest request) {

        ExtendedGui guiData = request.guiData();
        var player = request.player();
        var plugin = request.plugin();
        var clan   = request.clan();

        switch (guiData.getListenType()) {
            case QUESTS              -> { return new QuestsGui(player, guiData, plugin, clan); }
            case CHEST               -> { return new ChestGui(player, guiData, plugin, clan); }
            case CHOOSE_PLAYER_COLOR -> { return new ChoosePlayerColorGui(player, guiData, plugin, clan); }
            case MEMBERS             -> { return new MembersGui(player, guiData, plugin, clan); }
            case RANKS               -> { return new me.jetby.clans.common.gui.core.RanksGui(player, guiData, plugin, clan); }
            case RANK_PERMISSIONS    -> { return new me.jetby.clans.common.gui.core.RankPermissionGui(player, guiData, plugin, clan, request.rank()); }
            case TOP_CLANS           -> { return new TopClansGui(player, guiData, plugin, clan, LeaderboardService.TopType.KILLS); }
            default                  -> { return new Gui(player, guiData, plugin, clan); }
        }
    }

}