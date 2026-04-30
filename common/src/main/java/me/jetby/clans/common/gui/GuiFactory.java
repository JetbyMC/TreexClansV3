package me.jetby.clans.common.gui;

import lombok.Getter;
import me.jetby.clans.api.gui.Gui;
import me.jetby.clans.api.service.leaderboard.LeaderboardService;
import me.jetby.clans.common.gui.impl.*;

@Getter
public class GuiFactory {

    public static Gui create(GuiFactoryRequest request) {
        GuiType type;
        try {
            type = GuiType.valueOf(request.configuration().getString("type", "default").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }

        var player = request.player();
        var config = request.configuration();
        var plugin = request.plugin();
        var clan   = request.clan();

        switch (type) {
            case QUESTS              -> { return new QuestsGui(player, config, plugin, clan); }
            case CHEST               -> { return new ChestGui(player, config, plugin, clan); }
            case CHOOSE_COLOR        -> { return new ChooseColorGui(player, config, plugin, clan, request.target()); }
            case CHOOSE_PLAYER_COLOR -> { return new ChoosePlayerColorGui(player, config, plugin, clan); }
            case MEMBERS             -> { return new MembersGui(player, config, plugin, clan); }
            case RANKS               -> { return new RanksGui(player, config, plugin, clan); }
            case TOP_CLANS           -> { return new TopClansGui(player, config, plugin, clan, LeaderboardService.TopType.KILLS); }
            case RANK_PERMISSIONS    -> { return new RankPermissionsGui(player, config, plugin, clan, request.rank()); }
            default                  -> { return new DefaultGui(player, config, plugin, clan); }
        }
    }

}