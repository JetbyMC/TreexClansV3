package org.jetby.clans.common.commands.clan;

import lombok.Getter;
import org.jetby.clans.api.command.Subcommand;
import org.jetby.clans.common.commands.clan.subcommands.*;
import org.jetby.clans.common.commands.clan.subcommands.*;

public enum ClanSubcommand {
    CREATE(new CreateSubcommand()),
    INVITE(new InviteSubcommand()),
    ACCEPT(new AcceptSubcommand()),
    DENY(new DenySubcommand()),
    KICK(new KickSubcommand()),
    DISBAND(new DisbandSubcommand()),
    DEPOSIT(new DepositSubcommand()),
    BALANCE(new BalanceSubcommand()),
    INVEST(new DepositSubcommand()),
    WITHDRAW(new WithdrawSubcommand()),
    SETBASE(new SetBaseSubcommand()),
    SETRANK(new SetRankSubcommand()),
    BASE(new BaseSubcommand()),
    LEAVE(new LeaveSubcommand()),
    CHAT(new ChatSubcommand()),
    SETSLOGAN(new SetSloganSubcommand()),
    SETPREFIX(new SetPrefixSubcommand()),
    PVP(new PvpSubcommand()),
    RENAME(new RenameSubcommand());

    @Getter
    private final Subcommand subcommand;

    ClanSubcommand(Subcommand subcommand) {
        this.subcommand = subcommand;
    }
}
