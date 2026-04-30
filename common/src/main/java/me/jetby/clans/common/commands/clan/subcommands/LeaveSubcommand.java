package me.jetby.clans.common.commands.clan.subcommands;


import me.jetby.clans.api.addons.commands.CommandService;
import me.jetby.clans.api.command.Subcommand;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.configurations.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

;

public class LeaveSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getMessages().sendActions(player, null, "your-not-in-clan");
                return true;
            }
            var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            if (clanImpl.getLeader().equals(clanImpl.getMember(player.getUniqueId()))) {
                player.sendMessage(plugin.getMessages().getMessage("you-cant-leave-leader"));
                return true;
            }

            clanImpl.removeMember(clanImpl.getMember(player.getUniqueId()));
            plugin.getMessages().sendActions(player, clanImpl, "clan-leave",
                    new Messages.ReplaceString("{player}", player.getName()),
                    new Messages.ReplaceString("{clan}", clanImpl.getId())
            );
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
