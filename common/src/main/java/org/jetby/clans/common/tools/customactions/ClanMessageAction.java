package org.jetby.clans.common.tools.customactions;

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
        ClanImpl clanImpl = ctx.get(ClanImpl.class);
        if (clanImpl == null) return;
        plugin.getClanManager().chat().sendMessage(clanImpl, input.rawText());
    }
}
