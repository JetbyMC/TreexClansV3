package me.jetby.clans.common.tools.customactions;

import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.clan.model.ClanImpl;
import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClanMessageAction implements Action {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {
        ClanImpl clanImpl = ctx.get(ClanImpl.class);
        if (clanImpl == null) return;
        plugin.getClanManager().chat().sendMessage(clanImpl, input.rawText());
    }
}
