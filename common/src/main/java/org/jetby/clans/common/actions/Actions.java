package org.jetby.clans.common.actions;


import org.jetby.clans.api.gui.Gui;
import org.jetby.libb.action.ActionRegistry;
import org.bukkit.entity.Player;

public class Actions {

    public void registerCustomActions() {

        ActionRegistry.register("treexclans", "clan_message", new ClanMessageAction());
        ActionRegistry.register("treexclans", "MESSAGE_CLAN", new ClanMessageAction());
        ActionRegistry.register("treexclans", "TEAM_MSG", new ClanMessageAction());

        ActionRegistry.register("treexclans", "OPEN_MENU", new OpenMenuAction());
        ActionRegistry.register("treexclans", "OPEN_GUI", new OpenMenuAction());
        ActionRegistry.register("treexclans", "MENU", new OpenMenuAction());

        ActionRegistry.override("treexclans", "OPEN", new OpenMenuAction());


        ActionRegistry.register("treexclans", "CLAN_EXP_GIVE", new ClanExpGiveAction());
        ActionRegistry.register("treexclans", "CLAN_EXP_TAKE", new ClanExpTakeAction());

        ActionRegistry.register("treexclans", "MONEY_GIVE", new MoneyGiveAction());
        ActionRegistry.register("treexclans", "MONEY_TAKE", new MoneyTakeAction());

        ActionRegistry.register("treexclans", "COIN_GIVE", new CoinAddAction());
        ActionRegistry.register("treexclans", "COIN_TAKE", new CoinTakeAction());

        ActionRegistry.override("treexclans", "refresh", (ctx, s) -> {
            Gui gui = ctx.get(Gui.class);

            Player player = ctx.getPlayer();
            if (player == null) return;

            if (gui == null) return;
            gui.refresh();
        });
        ActionRegistry.override("treexclans", "next_page", (ctx, s) -> {
            Gui gui = ctx.get(Gui.class);

            Player player = ctx.getPlayer();
            if (player == null) return;

            if (gui == null) return;
            gui.nextPage();
        });
        ActionRegistry.override("treexclans", "prev_page", (ctx, s) -> {
            Gui gui = ctx.get(Gui.class);

            Player player = ctx.getPlayer();
            if (player == null) return;

            if (gui == null) return;
            gui.prevPage();
        });


    }
}
