package org.jetby.clans.common.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetby.clans.common.TreexClans;
import org.jetby.libb.action.Action;
import org.jetby.libb.action.ActionContext;
import org.jetby.libb.action.ActionInput;

import static org.jetby.clans.common.TreexClans.LOGGER;

public class MoneyGiveAction implements Action {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {
        Player player = ctx.getPlayer();
        if (player != null) {
            try {
                int amount = Integer.parseInt(input.rawText());
                plugin.getEconomy().depositPlayer(player, amount);
            } catch (NumberFormatException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }
}
