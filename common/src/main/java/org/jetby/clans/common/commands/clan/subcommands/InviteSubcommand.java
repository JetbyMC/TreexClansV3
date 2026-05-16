package org.jetby.clans.common.commands.clan.subcommands;


import org.jetby.clans.api.addons.commands.CommandService;
import org.jetby.clans.api.command.Subcommand;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.member.rank.RankPerm;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.tools.Cooldown;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

;

public class InviteSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String sub, @NotNull String[] args) {

        if (sender instanceof Player player) {
            if (args.length == 0) {
                plugin.getMessages().of(player, "commands.invite")
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
            if (!clan.getMember(player.getUniqueId()).getRank().perms().contains(RankPerm.INVITE)) {
                plugin.getMessages().of(player, "your-rank-is-not-allowed-to-do-that")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();
                return true;
            }
            if (clan.getMembers().size() >= clan.getLevel().maxMembers()) {
                plugin.getMessages().of(player, "clan-invite-limit")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();
                return true;
            }

            Player target = player.getServer().getPlayer(args[0]);
            if (target == null) {
                plugin.getMessages().of(player, "player-not-found")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();
                return true;
            }

            if (plugin.getClanManager().lookup().isInClan(target.getUniqueId())) {
                plugin.getMessages().of(player, "clan-player-already-in-clan")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();
                return true;
            }
            if (Cooldown.isOnCooldown("denied_" + target.getUniqueId() + "_" + clan.getId())) {
                plugin.getMessages().of(player, "clan-invite-denied")
                        .replace("{target}", target.getName())
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();
                return true;
            }

            if (Cooldown.isOnCooldown("invite_" + target.getUniqueId() + "_" + clan.getId())) {
                plugin.getMessages().of(player, "clan-already-invited")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();
            } else {
                Cooldown.setCooldown("invite_" + target.getUniqueId() + "_" + clan.getId(), plugin.getCfg().getInviteCooldown());
                plugin.getMessages().of(player, "clan-invite")
                        .replace("{target}", target.getName())
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .with(clan)
                        .run();

                plugin.getMessages().of(target, "clan-join-request")
                        .replace("{cmd}", command.getName())
                        .replace("{clan}", clan.getId())
                        .replace("{player}", player.getName())
                        .with(clan)
                        .run();


            }
            return true;
        }

        return true;
    }


    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length > 0) {
            return new ArrayList<>((Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList())));
        }
        return List.of();
    }

    @Override
    public CommandService.CommandType type() {
        return CommandService.CommandType.CLAN;
    }
}
