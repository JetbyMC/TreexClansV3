package org.jetby.clans.common.commands.clan.subcommands;

import org.jetby.clans.api.addons.commands.CommandService;
import org.jetby.clans.api.command.Subcommand;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.member.Member;
import org.jetby.clans.common.TreexClans;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChatSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String sub,  @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
                plugin.getMessages().of(player, "your-not-in-clan")
                        .replace("{cmd}", command.getName())
                        .replace("{cmd}", command.getName())
                        .replace("{arg}", sub)
                        .run();
                return true;
            }
            Clan clan = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());

            if (args.length == 0) {
                Member member = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId()).getMember(player.getUniqueId());
                if (!member.isChat()) {
                    plugin.getMessages().of(player, "clan-chat-on")
                            .replace("{cmd}", command.getName())
                            .replace("{cmd}", command.getName())
                            .replace("{arg}", sub)
                            .with(clan)
                            .run();
                    member.setChat(true);
                } else {
                    plugin.getMessages().of(player, "clan-chat-off")
                            .replace("{cmd}", command.getName())
                            .replace("{cmd}", command.getName())
                            .replace("{arg}", sub)
                            .with(clan)
                            .run();
                    member.setChat(false);
                }
                return true;
            } else {
                StringBuilder message = new StringBuilder();
                for (String str : args) message.append(str).append(" ");

                plugin.getClanManager().chat().sendChat(clan, player, message.toString());
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
