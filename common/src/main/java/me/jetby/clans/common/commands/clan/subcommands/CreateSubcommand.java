package me.jetby.clans.common.commands.clan.subcommands;


import me.jetby.clans.api.addons.commands.CommandService;
import me.jetby.clans.api.command.Subcommand;
import me.jetby.clans.api.service.ClanManager;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.configurations.MessagesConfiguration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CreateSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    private final ClanManager clanManagerImpl = plugin.getClanManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player) {

            if (clanManagerImpl.lookup().isInClan(player.getUniqueId())) {
                plugin.getMessages().sendActions(player, null, "commands.create");
                return true;
            } else {
                if (args.length < 1) {
                    sender.sendMessage("§cUsage: /clan create <clanName>");
                    return true;
                }
                String clanName = args[0].toLowerCase();
                if (clanManagerImpl.lifecycle().clanExists(clanName)) {
                    plugin.getMessages().sendActions(player, null, "clan-is-already-exists");
                    return true;
                }
                if (!clanManagerImpl.validation().isAllowedName(player, clanName)) {
                    return true;
                }

                if (clanManagerImpl.lifecycle().createClan(clanName, player)) {
                    Clan clan = plugin.getClanManager().lookup().getClan(clanName);
                    plugin.getMessages().sendActions(player, clan, "clan-create", new MessagesConfiguration.ReplaceString("{clan}", clanName));
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


}
