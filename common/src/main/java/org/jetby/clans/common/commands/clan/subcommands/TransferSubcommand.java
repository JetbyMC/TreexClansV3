package org.jetby.clans.common.commands.clan.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetby.clans.api.addons.commands.CommandService;
import org.jetby.clans.api.command.Subcommand;
import org.jetby.clans.api.service.ClanManager;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.member.rank.RankPerm;
import org.jetby.clans.common.TreexClans;

import java.util.List;

public class TransferSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    private final ClanManager clanManager = plugin.getClanManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String sub, @NotNull String[] args) {

        if (sender instanceof Player player) {
            if (args.length == 0) {
                plugin.getMessages().of(player, "commands.transfer")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();
                return true;
            }
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getMessages().of(player, "your-not-in-clan")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();
                return true;
            }

            Clan clan = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());

            if (clan.getLeader()!=clan.getMember(player.getUniqueId())) {
                plugin.getMessages().of(player, "your-rank-is-not-allowed-to-do-that")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();
                return true;
            }

            OfflinePlayer target = player.getServer().getOfflinePlayer(args[0]);
            if (clan.getMember(target.getUniqueId()) == null) {
                plugin.getMessages().of(player, "player-not-in-clan")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();
                return true;
            }

            clan.transfer(clan.getMember(target.getUniqueId()));
            plugin.getMessages().of(player, "clan-transfer")
                    .replace("{cmd}", command.getName())
                    .replace("{arg}", sub)
                    .replace("{target}", target.getName())
                    .run();

        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return List.of();
    }

    @Override
    public CommandService.CommandType type() {
        return CommandService.CommandType.CLAN;
    }
}
