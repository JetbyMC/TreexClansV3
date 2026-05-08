package me.jetby.clans.common.tools.customactions;

import me.jetby.clans.api.gui.ExtendedGui;
import me.jetby.clans.api.gui.GuiContext;
import me.jetby.clans.api.gui.ListenType;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.clan.model.ClanImpl;
import me.jetby.clans.common.gui.*;
import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionInput;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpenMenuAction implements Action {

    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {
        Player player = ctx.getPlayer();
        Clan clan = ctx.get(ClanImpl.class);
        ExtendedGui gui;

        String raw = input.rawText();
        if (raw.startsWith("model:")) {
            ListenType type;
            try {
                type = ListenType.valueOf(raw.replace("model:", "").toUpperCase());
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
                            clan))
                    .open(player);
        }
    }
}
