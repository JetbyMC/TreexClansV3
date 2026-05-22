package org.jetby.clans.common.commands.admin.subcommands;

import org.jetby.clans.api.addons.commands.CommandService;
import org.jetby.clans.api.command.Subcommand;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.member.Member;
import org.jetby.clans.common.TreexClans;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

;

// TODO: coin gvieall <clan> <amount>
// TODO: coin gvieall <amount>
public class CoinSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String sub,  @NotNull String[] args) {

        if (args.length == 0) {
            sender.sendMessage("/xclan coin give/set/take <player> <amount>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give": {
                if (args.length < 2) break;
                String playerName = args[1];
                int amount = Integer.parseInt(args[2]);
                UUID uuid;
                Player target = Bukkit.getPlayer(playerName);
                if (target == null) {
                    String string = "OfflinePlayer:" + playerName;
                    uuid = UUID.nameUUIDFromBytes(string.getBytes(StandardCharsets.UTF_8));
                } else {
                    uuid = target.getUniqueId();
                }
                Clan clan = plugin.getClanManager().lookup().getClanByMember(uuid);
                if (clan == null) break;
                Member member = clan.getMember(uuid);
                if (member == null) break;
                if (amount < 1) break;
                member.addCoin(amount);
                sender.sendMessage(playerName + " has " + member.getCoin() + " coins now.");
                break;
            }
            case "set": {
                if (args.length < 2) break;
                String playerName = args[1];
                int amount = Integer.parseInt(args[2]);
                UUID uuid;
                Player target = Bukkit.getPlayer(playerName);
                if (target == null) {
                    String string = "OfflinePlayer:" + playerName;
                    uuid = UUID.nameUUIDFromBytes(string.getBytes(StandardCharsets.UTF_8));
                } else {
                    uuid = target.getUniqueId();
                }
                var clan = plugin.getClanManager().lookup().getClanByMember(uuid);
                if (clan == null) break;
                var member = clan.getMember(uuid);
                if (member == null) break;
                if (amount < 0) amount = 0;
                member.setCoin(amount);
                sender.sendMessage(playerName + " has " + member.getCoin() + " coins now.");
                break;

            }
            case "take": {
                if (args.length < 2) break;
                String playerName = args[1];
                int amount = Integer.parseInt(args[2]);
                UUID uuid;
                Player target = Bukkit.getPlayer(playerName);
                if (target == null) {
                    String string = "OfflinePlayer:" + playerName;
                    uuid = UUID.nameUUIDFromBytes(string.getBytes(StandardCharsets.UTF_8));
                } else {
                    uuid = target.getUniqueId();
                }
                var clan = plugin.getClanManager().lookup().getClanByMember(uuid);
                if (clan == null) break;
                var member = clan.getMember(uuid);
                if (member == null) break;
                if (amount < 1) break;
                member.takeCoin(amount);
                sender.sendMessage(playerName + " has " + member.getCoin() + " coins now.");
                break;

            }
            default: {
                sender.sendMessage("/xclan coin give/set/take <player> <amount>");
                break;
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
        return CommandService.CommandType.ADMIN;
    }
}
