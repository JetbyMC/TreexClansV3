package me.jetby.clans.common.tools.customactions;


import me.jetby.libb.action.ActionRegistry;

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

        // why if we have mini message in new versions? so its unusable anymore cuz we move from 1.16 to 1.20
//        ActionRegistry.register("treexclans", "BUTTON", new ButtonAction());

    }
}
