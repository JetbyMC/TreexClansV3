package me.jetby.clans.common.commands.clan.subcommands;

import me.jetby.clans.api.addons.commands.CommandService;
import me.jetby.clans.api.command.Subcommand;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.clan.model.MemberImpl;
import me.jetby.clans.common.configurations.Messages;
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {


        if (sender instanceof Player player) {
            if (args.length == 0) {
                plugin.getMessages().sendActions(player, null, "commands.accept");
                return true;
            }
            if (plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getMessages().sendActions(player, null, "your-already-in-clan");
                return true;
            }
            if (!plugin.getClanManager().lifecycle().clanExists(args[0])) {
                plugin.getMessages().sendActions(player, null, "clan-does-not-exist");

                return true;
            }
            if (!Cooldown.isOnCooldown("invite_" + player.getUniqueId() + "_" + args[0])) {
                plugin.getMessages().sendActions(player, null, "no-invite");
                return true;
            } else {
                Cooldown.removeCooldown("invite_" + player.getUniqueId() + "_" + args[0]);
                var clanImpl = plugin.getClanManager().lookup().getClan(args[0]);
                MemberImpl memberImpl = new MemberImpl(
                        player.getUniqueId(),
                        plugin.getCfg().getDefaultRank(),
                        System.currentTimeMillis(),
                        System.currentTimeMillis(),
                        false, false,
                        0, 0, new HashMap<>(),
                        0, 0
                );
                plugin.getMessages().sendActions(player, clanImpl, "clan-join",
                        new Messages.ReplaceString("{player}", player.getName()),
                        new Messages.ReplaceString("{clan}", clanImpl.getId())
                );
                clanImpl.addMember(memberImpl);
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
