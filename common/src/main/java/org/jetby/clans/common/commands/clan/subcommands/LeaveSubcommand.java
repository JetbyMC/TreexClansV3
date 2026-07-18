package org.jetby.clans.common.commands.clan.subcommands;


import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetby.clans.api.command.Subcommand;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.common.TreexClans;

import java.util.List;

;

public class LeaveSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String sub, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getMessages().of(player, "your-not-in-clan")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();
                return true;
            }
            Clan clan = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            if (clan.getLeader().equals(clan.getMember(player.getUniqueId()))) {
                plugin.getMessages().of(player, "you-cant-leave-leader")
                        .replace("{cmd}", command.getName())
                        .with(clan)
                        .run();
                return true;
            }

            clan.removeMember(clan.getMember(player.getUniqueId()));
            plugin.getMessages().of(player, "clan-leave")
                    .replace("{clan}", clan.getId())
                    .replace("{player}", player.getName())
                    .replace("{cmd}", command.getName())
                    .replace("{arg}", sub)
                    .with(clan)
                    .run();
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }

    @Override
    public CommandType commandType() {
        return CommandType.CLAN;
    }
}
