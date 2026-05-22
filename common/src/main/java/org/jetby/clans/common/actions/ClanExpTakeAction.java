package org.jetby.clans.common.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.member.Member;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.clan.model.ClanImpl;
import org.jetby.libb.action.Action;
import org.jetby.libb.action.ActionContext;
import org.jetby.libb.action.ActionInput;

import static org.jetby.clans.common.TreexClans.LOGGER;


public class ClanExpTakeAction implements Action {

    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {
        Player player = ctx.getPlayer();
        Clan clan = ctx.get(ClanImpl.class);
        if (clan == null) {
            if (player!=null) {
                clan = TreexClans.getInstance().getClanManager().lookup().getClanByMember(player.getUniqueId());
            }
        }
        if (clan != null) {
            try {
                int amount = Integer.parseInt(input.rawText());
                Member member = clan.getMember(player.getUniqueId());
                clan.takeExp(amount, member);
            } catch (NumberFormatException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }
}
