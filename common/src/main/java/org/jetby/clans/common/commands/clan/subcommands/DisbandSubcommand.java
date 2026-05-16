package org.jetby.clans.common.commands.clan.subcommands;


import org.jetby.clans.api.addons.commands.CommandService;
import org.jetby.clans.api.command.Subcommand;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.tools.Cooldown;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DisbandSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender,  @NotNull Command command,@NotNull String sub,  @NotNull String[] args) {

        if (sender instanceof Player player) {
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getMessages().of(player, "your-not-in-clan")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();
                return true;
            }
            Clan clan = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            if (Cooldown.isOnCooldown("delete_" + player.getUniqueId())) {
                if (clan.getLeader().getUuid().equals(player.getUniqueId())) {
                    if (plugin.getClanManager().lifecycle().deleteClan(clan, player)) {
                        plugin.getMessages().of(player, "clan-disband")
                                .replace("{cmd}", command.getName())
                                .replace("{arg}", sub)
                                .with(clan)
                                .run();
                    }
                }
            } else {
                if (clan.getLeader().getUuid().equals(player.getUniqueId())) {
                    plugin.getMessages().of(player, "clan-disband-confirm")
                            .replace("{cmd}", command.getName())
                            .replace("{arg}", sub)
                            .with(clan)
                            .run();
                    Cooldown.setCooldown("delete_" + player.getUniqueId(), plugin.getCfg().getDisbandCooldown());
                }
            }
            return true;

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
