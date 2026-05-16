package org.jetby.clans.common.commands.clan.subcommands;

import org.jetby.clans.api.addons.commands.CommandService;
import org.jetby.clans.api.command.Subcommand;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.member.Member;
import org.jetby.clans.api.service.clan.member.rank.Rank;
import org.jetby.clans.api.service.clan.member.rank.RankPerm;
import org.jetby.clans.common.TreexClans;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

;

public class SetRankSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender,  @NotNull Command command,@NotNull String sub,  @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (args.length < 2) {
                plugin.getMessages().of(player, "commands.setrank")
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

            if (!clan.getMember(player.getUniqueId()).getRank().perms().contains(RankPerm.SETRANK)) {
                plugin.getMessages().of(player, "your-rank-is-not-allowed-to-do-that")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();
                return true;
            }

            String rankName = args[0].toLowerCase();
            Rank rank = clan.getRanks().get(rankName);
            if (rank != null) {
                if (plugin.getCfg().getLeaderRank().equals(rank)) {
                    return true;
                }
                String targetName = args[1];
                UUID uuid;
                Player target = Bukkit.getPlayer(targetName);
                if (target == null) {
                    String string = "OfflinePlayer:" + targetName;
                    uuid = UUID.nameUUIDFromBytes(string.getBytes(StandardCharsets.UTF_8));
                } else {
                    uuid = target.getUniqueId();
                }
                Member targetMember = clan.getMember(uuid);

                if (target != null && clan.getMember(player.getUniqueId()).equals(targetMember)) {
                    plugin.getMessages().of(player, "clan-you-cant-setrank-yourself")
                            .replace("{cmd}", command.getName())
                            .replace("{arg}", sub)
                            .with(clan)
                            .run();
                    return true;
                }

                if (clan.getLeader().equals(targetMember)) {
                    plugin.getMessages().of(player, "you-cant-do-that-with-leader")
                            .replace("{cmd}", command.getName())
                            .replace("{arg}", sub)
                            .with(clan)
                            .run();
                    return true;
                }

                plugin.getMessages().of(player, "clan-setrank")
                        .replace("{rank_prefix}", rank.name())
                        .replace("{player}", player.getName())
                        .replace("{target}", targetName)
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();
                targetMember.setRank(rank);
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                return null;
            }
            Clan clan = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            if (args.length == 3) {
                List<String> playerNames = new ArrayList<>();
                for (Member member : clan.getMembers()) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(member.getUuid());
                    String name = offlinePlayer.getName();
                    if (name != null) {
                        playerNames.add(name);
                    }
                }
                return playerNames;
            } else if (args.length == 2) {
                return clan.getRanks().values().stream()
                        .filter(s1 -> !plugin.getCfg().getLeaderRank().equals(s1))
                        .map(rank -> rank.id())
                        .map(String::toLowerCase)
                        .toList();
            }
        }
        return null;
    }

    @Override
    public CommandService.CommandType type() {
        return CommandService.CommandType.CLAN;
    }
}
