package org.jetby.clans.common.commands.admin.subcommands;


import org.jetby.clans.api.addons.commands.CommandService;
import org.jetby.clans.api.command.Subcommand;
import org.jetby.clans.common.TreexClans;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExpSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String sub,  @NotNull String[] args) {

        if (args.length==0) {
            sender.sendMessage("/xclan exp give/set/take <clan> <amount>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give": {
                if (args.length<2) break;
                String clanName = args[1];
                int amount = Integer.parseInt(args[2]);
                var clan = plugin.getClanManager().lookup().getClan(clanName);
                if (clan ==null) break;
                if (amount<1) break;
                clan.addExp(amount, plugin.getCfg().getLevels());
                sender.sendMessage("Clan "+clanName+" has "+ clan.getExp()+ " now.");
                break;
            }
            case "set": {
                if (args.length<2) break;
                String clanName = args[1];
                int amount = Integer.parseInt(args[2]);
                var clan = plugin.getClanManager().lookup().getClan(clanName);
                if (clan ==null) break;
                if (amount<0) amount = 0;
                clan.setExp(amount);
                sender.sendMessage("Clan "+clanName+" has "+ clan.getExp()+ " now.");
                break;
            }
            case "take": {
                if (args.length<2) break;
                String clanName = args[1];
                int amount = Integer.parseInt(args[2]);
                var clan = plugin.getClanManager().lookup().getClan(clanName);
                if (clan ==null) break;
                if (amount<1) break;
                clan.takeExp(amount);
                sender.sendMessage("Clan "+clanName+" has "+ clan.getExp()+ " now.");
                break;
            }
            default: {
                sender.sendMessage("/xclan exp give/set/take <clan> <amount>");
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
    public Subcommand.CommandType commandType() {
        return CommandType.ADMIN;
    }
}
