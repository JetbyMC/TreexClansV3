package me.jetby.clans.common.commands.clan.subcommands;


import me.jetby.clans.api.addons.commands.CommandService;
import me.jetby.clans.api.command.Subcommand;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.tools.Cooldown;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DenySubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String sub,  @NotNull String[] args) {


        if (sender instanceof Player player) {
            if (args.length == 0) {
                plugin.getMessages().of(player,  "commands.deny")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();
                return true;
            }

            var clanId = args[0];
            if (!plugin.getClanManager().lifecycle().clanExists(clanId)) {
                plugin.getMessages().of(player,  "clan-does-not-exist")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();
                return true;
            }

            if (!Cooldown.isOnCooldown("invite_" + player.getUniqueId() + "_" + clanId)) {
                plugin.getMessages().of(player, "no-invite")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();
                return true;
            }

            Cooldown.setCooldown("denied_" + player.getUniqueId() + "_" + clanId, 120);
            Cooldown.removeCooldown("invite_" + player.getUniqueId() + "_" + clanId);

            plugin.getMessages().of(player, "clan-deny")
                    .replace("{cmd}", command.getName())
                    .replace("{arg}", sub)
                    .run();
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
