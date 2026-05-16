package org.jetby.clans.common.tools.customactions;

import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.common.clan.model.ClanImpl;
import org.jetby.libb.action.Action;
import org.jetby.libb.action.ActionContext;
import org.jetby.libb.action.ActionInput;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static org.jetby.clans.common.TreexClans.LOGGER;


public class CoinAddAction implements Action {

    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {
        Player player = ctx.getPlayer();
        Clan clan = ctx.get(ClanImpl.class);

        if (player != null  && clan != null) {
            try {
                int amount = Integer.parseInt(input.rawText());
                var memberImpl = clan.getMember(player.getUniqueId());
                memberImpl.addCoin(amount);
            } catch (NumberFormatException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }
}
