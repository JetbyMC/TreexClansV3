package me.jetby.clans.common.tools.customactions;

import me.jetby.clans.common.clan.model.ClanImpl;
import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionInput;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.jetby.clans.common.TreexClans.LOGGER;


public class ClanExpTakeAction implements Action {

    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {
        Player player = ctx.getPlayer();
        ClanImpl clanImpl = ctx.get(ClanImpl.class);

        if (player != null && clanImpl != null) {
            try {
                int amount = Integer.parseInt(input.rawText());
                var memberImpl = clanImpl.getMember(player.getUniqueId());
                clanImpl.takeExp(amount, memberImpl);
            } catch (NumberFormatException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }
}
