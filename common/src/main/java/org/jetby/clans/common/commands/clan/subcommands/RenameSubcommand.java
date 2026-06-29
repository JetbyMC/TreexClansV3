package org.jetby.clans.common.commands.clan.subcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetby.clans.api.addons.commands.CommandService;
import org.jetby.clans.api.command.Subcommand;
import org.jetby.clans.api.service.ClanManager;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.common.TreexClans;

import java.util.List;

public class RenameSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    private final ClanManager clanManager = plugin.getClanManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String sub, @NotNull String[] args) {

        if (sender instanceof Player player) {
            if (args.length == 0) {
                plugin.getMessages().of(player, "commands.rename")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();
                return true;
            }
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getMessages().of(player, "your-not-in-clan")
                        .replace("{cmd}", command.getName())
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();
                return true;
            }
            Clan clan = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());

            String newTag = args[0].toLowerCase();

            if (clanManager.lifecycle().clanExists(newTag)) {
                plugin.getMessages().of(player,  "clan-is-already-exists")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .replace("{old}", clan.getId())
                        .replace("{new}", newTag)
                        .run();
                return true;
            }

            if (clanManager.validation().isAllowedName(player, newTag)) {
                plugin.getMessages().of(player,  "clan-renamed")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .replace("{old}", clan.getId())
                        .replace("{new}", newTag)
                        .run();
                clan.rename(newTag);
            }

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
