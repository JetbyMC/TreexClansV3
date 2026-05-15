package me.jetby.clans.common.commands.clan.subcommands;


import me.jetby.clans.api.addons.commands.CommandService;
import me.jetby.clans.api.command.Subcommand;
import me.jetby.clans.api.service.ClanManager;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.common.TreexClans;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CreateSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    private final ClanManager clanManager = plugin.getClanManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String sub,  @NotNull String[] args) {
        if (sender instanceof Player player) {

            if (clanManager.lookup().isInClan(player.getUniqueId())) {
                plugin.getMessages().of(player, "commands.create")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();
                return true;
            } else {
                if (args.length < 1) {
                    sender.sendMessage("§cUsage: /clan create <clanName>");
                    return true;
                }
                String clanName = args[0].toLowerCase();
                if (clanManager.lifecycle().clanExists(clanName)) {
                    plugin.getMessages().of(player,  "clan-is-already-exists")
                            .replace("{cmd}", command.getName())
                            .replace("{arg}", sub)
                            .run();
                    return true;
                }
                if (!clanManager.validation().isAllowedName(player, clanName)) {
                    return true;
                }

                if (clanManager.lifecycle().createClan(clanName, player)) {
                    Clan clan = plugin.getClanManager().lookup().getClan(clanName);
                    plugin.getMessages().of(player, "clan-create")
                            .replace("{clan}", clanName)
                            .replace("{cmd}", command.getName())
                            .replace("{arg}", sub)
                            .with(clan)
                            .run();
                }
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


    @Override
    public boolean clanOnly() {
        return false;
    }
}
