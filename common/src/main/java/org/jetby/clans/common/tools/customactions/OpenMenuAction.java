package org.jetby.clans.common.tools.customactions;

import org.jetby.clans.api.gui.ClanGuiData;
import org.jetby.clans.api.gui.GuiContext;
import org.jetby.clans.api.gui.GuiModel;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.clan.model.ClanImpl;
import org.jetby.clans.common.configurations.Config;
import org.jetby.clans.common.gui.GuiLoader;
import org.jetby.libb.action.Action;
import org.jetby.libb.action.ActionContext;
import org.jetby.libb.action.ActionInput;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpenMenuAction implements Action {

    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {
        Player player = ctx.getPlayer();
        Clan clan = ctx.get(ClanImpl.class);
        ClanGuiData gui;

        String raw = input.rawText();
        if (raw.startsWith("model:")) {
            GuiModel type;
            try {
                type = GuiModel.valueOf(raw.replace("model:", "").toUpperCase());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            gui = GuiLoader.getGuiConfiguration(type);
        } else {
            gui = GuiLoader.getGuiConfiguration(input.rawText());
        }

        if (gui != null && player != null && clan != null) {
            TreexClans.getInstance().getGuiFactory().create(GuiContext.of(
                            TreexClans.getInstance(),
                            gui,
                            player,
                            clan, Config.CONFIG_COLORIZER))
                    .open(player);
        }
    }
}