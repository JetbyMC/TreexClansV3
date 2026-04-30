package me.jetby.clans.common.tools.customactions;

import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.clan.model.ClanImpl;
import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionInput;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.jetby.clans.common.TreexClans.LOGGER;

public class ClanExpGiveAction implements Action {
    private final TreexClans plugin = TreexClans.getInstance();


    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {
        Player player = ctx.getPlayer();
        ClanImpl clanImpl = ctx.get(ClanImpl.class);

        if (clanImpl != null) {
            if (player != null) {
                try {
                    int amount = Integer.parseInt(input.rawText());
                    var memberImpl = clanImpl.getMember(player.getUniqueId());
                    clanImpl.addExp(amount, memberImpl, plugin.getCfg().getLevels());
                } catch (NumberFormatException e) {
                    LOGGER.warn(e.getMessage());
                }
            } else {
                try {
                    int amount = Integer.parseInt(input.rawText());
                    clanImpl.addExp(amount, plugin.getCfg().getLevels());
                } catch (NumberFormatException e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        }
    }
}
