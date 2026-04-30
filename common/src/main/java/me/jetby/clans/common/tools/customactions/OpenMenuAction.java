package me.jetby.clans.common.tools.customactions;

import me.jetby.clans.api.InstanceFactory;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.clan.model.ClanImpl;
import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionInput;
import me.jetby.libb.gui.parser.Gui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenMenuAction implements Action {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {
        Player player = ctx.getPlayer();
        ClanImpl clanImpl = ctx.get(ClanImpl.class);
        // todo sex
        //
        //        var menu = plugin.getGuiLoader().getGuis().get(message);
//        if (menu != null && player != null && clanImpl != null) {
//            Gui gui = InstanceFactory.GUI_FACTORY.create(plugin, menu, player, clanImpl);
//            Bukkit.getScheduler().runTaskLater(plugin, () -> gui.open(player), 1L);
//        }
    }
}
