package me.jetby.clans.common.commands.admin;

import lombok.Getter;
import me.jetby.clans.api.command.Subcommand;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.commands.admin.subcommands.CoinSubcommand;
import me.jetby.clans.common.commands.admin.subcommands.ExpSubcommand;
import me.jetby.clans.common.commands.admin.subcommands.ReloadSubcommand;
import me.jetby.clans.common.commands.admin.subcommands.StorageSubcommand;

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
