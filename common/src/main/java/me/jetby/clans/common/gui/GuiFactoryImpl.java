package me.jetby.clans.common.gui;

import lombok.Getter;
import me.jetby.clans.api.gui.*;
import me.jetby.clans.api.service.leaderboard.LeaderboardService;
import me.jetby.clans.common.gui.core.ChestGui;
import me.jetby.clans.common.gui.core.QuestsGui;
import me.jetby.clans.common.gui.core.RankPermissionGui;
import me.jetby.clans.common.gui.core.RanksGui;
import me.jetby.clans.common.gui.impl.*;

import java.util.HashMap;
import java.util.Map;

@Getter
public class GuiFactoryImpl implements GuiFactory {
    private final Map<String, GuiCreator> customTypes = new HashMap<>();

    @Override
    public Gui create(GuiContext ctx) {
        String renderer = ctx.getGui().getRenderer();

        GuiCreator custom = customTypes.get(renderer.toUpperCase());
        if (custom != null) return custom.create(ctx);

        if (ctx.getGui().isNamespaced()) return new Gui(ctx);

        GuiModel type;
        try {
            type = GuiModel.valueOf(renderer.toUpperCase());
        } catch (Exception e) {
            return new Gui(ctx);
        }

        return switch (type) {
            case CHEST            -> new ChestGui(ctx);
            case MEMBERS          -> new MembersGui(ctx);
            case RANKS            -> new RanksGui(ctx);
            case RANK_PERMISSIONS -> new RankPermissionGui(ctx);
            default               -> new Gui(ctx);
        };
    }

    @Override
    public void register(String type, GuiCreator creator) {
        customTypes.put(type.toUpperCase(), creator);
    }

    @Override
    public void unregister(String type) {
        customTypes.remove(type.toUpperCase());
    }
}