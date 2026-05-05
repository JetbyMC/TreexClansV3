package me.jetby.clans.common.tools.customactions;

import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.clan.model.ClanImpl;
import me.jetby.clans.common.gui.GuiFactory;
import me.jetby.clans.common.gui.GuiFactoryRequest;
import me.jetby.clans.common.gui.GuiLoader;
import me.jetby.clans.common.gui.GuiType;
import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionInput;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpenMenuAction implements Action {

    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {
        Player player = ctx.getPlayer();
        Clan clan = ctx.get(ClanImpl.class);
        FileConfiguration config = GuiLoader.getGuiConfiguration(input.rawText());
        if (config != null && player != null && clan != null) {
            GuiFactory.create(GuiFactoryRequest.builder()
                            .configuration(config)
                            .plugin(TreexClans.getInstance())
                            .player(player)
                            .permissionConfig(GuiLoader.getGuiConfiguration(GuiType.RANK_PERMISSIONS))
                            .clan(clan)
                            .build())
                    .open(player);
        }
    }
}
