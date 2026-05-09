package me.jetby.clans.common.commands.clan.subcommands;

import me.jetby.clans.api.addons.commands.CommandService;
import me.jetby.clans.api.command.Subcommand;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.api.service.clan.member.Member;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.clan.model.MemberImpl;
import me.jetby.clans.common.tools.Cooldown;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class AcceptSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String sub, @NotNull String[] args) {


        if (sender instanceof Player player) {
            if (args.length == 0) {
                plugin.getMessages().of(player, "commands.accept")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();
                return true;
            }
            if (plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getMessages().of(player, "your-already-in-clan")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();
                return true;
            }
            if (!plugin.getClanManager().lifecycle().clanExists(args[0])) {
                plugin.getMessages().of(player, "clan-does-not-exist")
                        .replace("{clan}", args[0])
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();

                return true;
            }
            if (!Cooldown.isOnCooldown("invite_" + player.getUniqueId() + "_" + args[0])) {
                plugin.getMessages().of(player, "no-invite")
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();
                return true;
            } else {
                Cooldown.removeCooldown("invite_" + player.getUniqueId() + "_" + args[0]);
                Clan clan = plugin.getClanManager().lookup().getClan(args[0]);
                Member member = new MemberImpl(
                        player.getUniqueId(),
                        plugin.getCfg().getDefaultRank(),
                        System.currentTimeMillis(),
                        System.currentTimeMillis(),
                        false, false,
                        0, 0, new HashMap<>(),
                        0, 0
                );
                plugin.getMessages().of(player, "clan-join")
                        .with(clan)
                        .replace("{player}", player.getName())
                        .replace("{clan}", clan.getId())
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();
                clan.addMember(member);
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
