package me.jetby.clans.common.tools.customactions;

import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.clan.model.ClanImpl;
import me.jetby.clans.common.gui.*;
import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionInput;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpenRequiredMenuAction implements Action {

    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {
        Player player = ctx.getPlayer();
        Clan clan = ctx.get(ClanImpl.class);

        ListenType type;
        try {
            type = ListenType.valueOf(input.rawText().toUpperCase());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        ExtendedGui gui = GuiLoader.getGuiConfiguration(type);
        if (gui != null && player != null && clan != null) {
            GuiFactory.create(GuiFactoryRequest.builder()
                            .guiData(gui)
                            .plugin(TreexClans.getInstance())
                            .player(player)
                            .clan(clan)
                            .build())
                    .open(player);
        }
    }
}
