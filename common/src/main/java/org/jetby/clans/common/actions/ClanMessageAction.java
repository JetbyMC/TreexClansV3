package org.jetby.clans.common.actions;

import org.bukkit.entity.Player;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.clan.model.ClanImpl;
import org.jetby.libb.action.Action;
import org.jetby.libb.action.ActionContext;
import org.jetby.libb.action.ActionInput;
import org.jetbrains.annotations.NotNull;

public class ClanMessageAction implements Action {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {
        Clan clan = ctx.get(ClanImpl.class);
        if (clan==null) {
            Player player = ctx.getPlayer();
            if (player!=null) {
                clan = TreexClans.getInstance().getClanManager().lookup().getClanByMember(player.getUniqueId());
            }
        }

        plugin.getClanManager().chat().sendMessage(clan, input.rawText());
    }
}
