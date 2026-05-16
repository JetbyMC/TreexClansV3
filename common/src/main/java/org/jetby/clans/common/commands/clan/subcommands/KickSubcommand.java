package org.jetby.clans.common.commands.clan.subcommands;


import org.jetby.clans.api.addons.commands.CommandService;
import org.jetby.clans.api.command.Subcommand;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.member.Member;
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
import java.util.List;
import java.util.UUID;

;

public class KickSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String sub, @NotNull String[] args) {


        if (sender instanceof Player player) {
            if (args.length == 0) {
                plugin.getMessages().of(player, "commands.kick")
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

            if (!clan.getMember(player.getUniqueId()).getRank().perms().contains(RankPerm.KICK)) {
                plugin.getMessages().of(player, "your-rank-is-not-allowed-to-do-that")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();
                return true;
            }

            String targetName = args[0];
            UUID uuid;
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                String string = "OfflinePlayer:" + targetName;
                uuid = UUID.nameUUIDFromBytes(string.getBytes(StandardCharsets.UTF_8));
            } else {
                uuid = target.getUniqueId();
            }
            Member member = clan.getMember(uuid);

            if (member == null) {
                plugin.getMessages().of(player, "player-not-found")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();
                return true;
            }
            if (target != null && clan.getMember(player.getUniqueId()).equals(member)) {
                plugin.getMessages().of(player, "clan-you-cant-kick-yourself")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();
                return true;
            }
            if (clan.getLeader().equals(member)) {
                plugin.getMessages().of(player, "you-cant-do-that-with-leader")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();
                return true;
            }

            clan.removeMember(member);
            plugin.getMessages().of(player, "clan-player-kick")
                    .replace("{target}", targetName)
                    .replace("{cmd}", command.getName())
                    .replace("{arg}", sub)
                    .with(clan)
                    .run();
            if (target != null && target.isOnline()) {
                plugin.getMessages().of(target, "clan-you-was-kicked")
                        .replace("{target}", targetName)
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
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                return List.of();
            }
            Clan clan = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            if (args.length > 0) {
                return clan.getMembers()
                        .stream()
                        .map(member -> {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(member.getUuid());
                            return offlinePlayer.getName();
                        })
                        .toList();
            }
        }
        return List.of();
    }

    @Override
    public CommandService.CommandType type() {
        return CommandService.CommandType.CLAN;
    }
}
