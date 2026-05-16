package org.jetby.clans.common.commands.admin;

import lombok.Getter;
import org.jetby.clans.api.command.Subcommand;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.commands.admin.subcommands.CoinSubcommand;
import org.jetby.clans.common.commands.admin.subcommands.ExpSubcommand;
import org.jetby.clans.common.commands.admin.subcommands.ReloadSubcommand;
import org.jetby.clans.common.commands.admin.subcommands.StorageSubcommand;

public enum AdminCommandArgs {
    COIN(new CoinSubcommand()),
    EXP(new ExpSubcommand()),
    STORAGE(new StorageSubcommand()),
    RELOAD(new ReloadSubcommand(TreexClans.getInstance()));

    @Getter
    private final Subcommand subcommand;

    AdminCommandArgs(Subcommand subcommand) {
        this.subcommand = subcommand;
    }
}
