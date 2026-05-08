package me.jetby.clans.common.commands.clan.subcommands;


import me.jetby.clans.api.addons.commands.CommandService;
import me.jetby.clans.api.command.Subcommand;
import me.jetby.clans.api.service.clan.member.rank.RankPerm;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.configurations.MessagesConfiguration;
import me.jetby.clans.common.tools.Cooldown;
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {

        if (sender instanceof Player player) {
            if (args.length == 0) {
                plugin.getMessages().sendActions(player, null, "commands.invite");
                return true;
            }
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getMessages().sendActions(player, null, "your-not-in-clan");
                return true;
            }
            var clan = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            if (!clan.getMember(player.getUniqueId()).getRank().perms().contains(RankPerm.INVITE)) {
                plugin.getMessages().sendActions(player, clan, "your-rank-is-not-allowed-to-do-that");
                return true;
            }
            if (clan.getMembers().size() >= clan.getLevel().maxMembers()) {
                plugin.getMessages().sendActions(player, clan, "clan-invite-limit");
                return true;
            }

            Player target = player.getServer().getPlayer(args[0]);
            if (target == null) {
                plugin.getMessages().sendActions(player, clan, "player-not-found");
                return true;
            }

            if (plugin.getClanManager().lookup().isInClan(target.getUniqueId())) {
                plugin.getMessages().sendActions(player, clan, "clan-player-already-in-clan");
                return true;
            }
            if (Cooldown.isOnCooldown("denied_" + target.getUniqueId() + "_" + clan.getId())) {
                plugin.getMessages().sendActions(player, clan, "clan-invite-denied", new MessagesConfiguration.ReplaceString("{target}", target.getName()));
                return true;
            }

            if (Cooldown.isOnCooldown("invite_" + target.getUniqueId() + "_" + clan.getId())) {
                plugin.getMessages().sendActions(player, clan, "clan-already-invited");
            } else {
                Cooldown.setCooldown("invite_" + target.getUniqueId() + "_" + clan.getId(), 60);
                plugin.getMessages().sendActions(player, clan, "clan-invite", new MessagesConfiguration.ReplaceString("{target}", target.getName()));

                plugin.getMessages().sendActions(target, null, "clan-join-request",
                        new MessagesConfiguration.ReplaceString("{clan}", clan.getId()),
                        new MessagesConfiguration.ReplaceString("{player}", player.getName())
                );

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
