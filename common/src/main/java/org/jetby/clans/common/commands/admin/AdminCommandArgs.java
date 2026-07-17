package org.jetby.clans.common.commands.admin;

import lombok.Getter;
import org.jetby.clans.api.command.Subcommand;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.commands.admin.subcommands.*;

public enum AdminCommandArgs {
    COIN(new CoinSubcommand()),
    EXP(new ExpSubcommand()),
    CLAN(new ClanSubcommand()),
    RELOAD(new ReloadSubcommand(TreexClans.getInstance()));

    @Getter
    private final Subcommand subcommand;

    AdminCommandArgs(Subcommand subcommand) {
        this.subcommand = subcommand;
    }
}
