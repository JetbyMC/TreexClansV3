package me.jetby.clans.common.tools.customactions;

import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.member.Member;
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
        Clan clan = ctx.get(ClanImpl.class);

        if (player != null && clan != null) {
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
