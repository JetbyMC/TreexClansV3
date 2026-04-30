package me.jetby.clans.common.tools.customactions;

import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionInput;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ButtonAction implements Action {


    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput actionInput) {
        Player player = ctx.getPlayer();
        if (player != null) {
//            Md5Button.send(player, context);
        }
    }
}
