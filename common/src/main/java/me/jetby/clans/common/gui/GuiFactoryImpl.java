package me.jetby.clans.common.gui;

import lombok.Getter;
import me.jetby.clans.api.gui.Gui;
import me.jetby.clans.api.gui.GuiFactory;
import me.jetby.clans.api.gui.GuiContext;
import me.jetby.clans.api.service.leaderboard.LeaderboardService;
import me.jetby.clans.common.gui.impl.*;

@Getter
public class GuiFactoryImpl implements GuiFactory {

    public Gui create(GuiContext ctx) {

        switch (ctx.getGui().getListenType()) {
            case QUESTS              -> { return new QuestsGui(ctx); }
            case CHEST               -> { return new ChestGui(ctx); }
            case MEMBERS             -> { return new MembersGui(ctx); }
            case RANKS               -> { return new me.jetby.clans.common.gui.core.RanksGui(ctx); }
            case RANK_PERMISSIONS    -> { return new me.jetby.clans.common.gui.core.RankPermissionGui(ctx); }
            case TOP_CLANS           -> { return new TopClansGui(ctx.with(LeaderboardService.TopType.KILLS)); }
            default                  -> { return new Gui(ctx); }
        }
    }

}