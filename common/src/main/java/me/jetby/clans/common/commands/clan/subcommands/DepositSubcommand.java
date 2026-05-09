package me.jetby.clans.common.commands.clan.subcommands;


import me.jetby.clans.api.addons.commands.CommandService;
import me.jetby.clans.api.command.Subcommand;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.member.rank.RankPerm;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.configurations.MessagesConfiguration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

;

public class DepositSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender,  @NotNull Command command,@NotNull String sub,  @NotNull String[] args) {


        if (sender instanceof Player player) {
            if (args.length == 0) {
                plugin.getMessages().of(player, "commands.deposit")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();
                return true;
            }
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getMessages().of(player,  "your-not-in-clan")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();
                return true;
            }

            Clan clan = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            if (!clan.getMember(player.getUniqueId()).getRank().perms().contains(RankPerm.DEPOSIT)) {
                plugin.getMessages().of(player, "your-rank-is-not-allowed-to-do-that")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();
                return true;
            }
            double balance = Double.parseDouble(args[0]);
            if (plugin.getEconomy().has(player, balance)) {
                if (clan.getBalance() + balance > clan.getLevel().maxBalance()) {
                    plugin.getMessages().of(player, "clan-balance-limit")
                            .replace("{max-balance}", String.valueOf(clan.getLevel().maxBalance()))
                            .replace("{sum}", String.valueOf(balance))
                            .replace("{cmd}", command.getName())
                            .replace("{arg}", sub)
                            .with(clan)
                            .run();
                    return true;
                }
                plugin.getEconomy().withdrawPlayer(player, balance);
                plugin.getClanManager().economy().addBalance(balance, clan);
                plugin.getMessages().of(player, "clan-balance-deposit")
                        .replace("{max-balance}", String.valueOf(clan.getLevel().maxBalance()))
                        .replace("{sum}", String.valueOf(balance))
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();
            } else {
                plugin.getMessages().of(player, "clan-deposit-no-money")
                        .replace("{max-balance}", String.valueOf(clan.getLevel().maxBalance()))
                        .replace("{sum}", String.valueOf(balance))
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();
            }

        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }

    @Override
    public CommandService.CommandType type() {
        return CommandService.CommandType.CLAN;
    }
}
