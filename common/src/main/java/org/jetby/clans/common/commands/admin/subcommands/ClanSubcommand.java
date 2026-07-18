package org.jetby.clans.common.commands.admin.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetby.clans.api.addons.commands.CommandService;
import org.jetby.clans.api.command.Subcommand;
import org.jetby.clans.api.gui.GuiContext;
import org.jetby.clans.api.gui.GuiModel;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.gui.GuiLoader;

import java.util.Arrays;
import java.util.List;

public class ClanSubcommand implements Subcommand {

    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String sub, @NotNull String[] args) {

        if (args.length==0) return true;

        Clan clan = plugin.getClanManager().lookup().getClan(args[0]);
        if (clan==null) {
            sender.sendMessage("Clan not found");
            return true;
        }

        if (args.length<2) return true;
        switch (args[1].toLowerCase()) {
            case "disband" -> {
                if (sender instanceof Player initiator) {
                    disband(clan, initiator);
                } else {
                    sender.sendMessage("Players only command");
                }
            }
            case "rename" -> {
                String newTag = args[2];
                if (!newTag.isEmpty()) {
                    rename(clan, newTag);
                    sender.sendMessage("Clan successfully renamed");
                } else {
                    sender.sendMessage("Please provide new tag");
                }
            }
            case "setprefix" -> {
                String newPrefix = args[2];
                if (!newPrefix.isEmpty()) {
                    setPrefix(clan, newPrefix);
                    sender.sendMessage("Prefix successfully changed");
                } else {
                    sender.sendMessage("Please provide new prefix");
                }
            }
            case "setslogan" -> {
                String slogan = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();
                if (!slogan.isEmpty()) {
                    setSlogan(clan, slogan);
                    sender.sendMessage("Slogan successfully changed");
                } else {
                    sender.sendMessage("Please provide new prefix");
                }
            }
            case "balance" -> {
                sender.sendMessage(String.valueOf(clan.getBalance()));
            }
            case "withdraw" -> {
                double toRemove = Double.parseDouble(args[2]);
                clan.setBalance(clan.getBalance()-toRemove);
                sender.sendMessage("Successfully withdrawn "+toRemove+" money");
            }
            case "deposit" -> {
                double toAdd = Double.parseDouble(args[2]);
                clan.setBalance(clan.getBalance()+toAdd);
                sender.sendMessage("Successfully deposited "+toAdd+" money");
            }
            case "base" -> {
                if (sender instanceof Player player) {
                    if (clan.getBase()==null) {
                        sender.sendMessage("Clan has no base set yet");
                        break;
                    }
                    player.teleport(clan.getBase());
                } else {
                    sender.sendMessage("Players only command");
                }
            }
            case "resetbase" -> {
                clan.setBase(null);
                sender.sendMessage("Successfully removed the base");
            }
            case "transfer" -> {
                String target = args[2];
                if (target.isEmpty()) break;
                transfer(clan, sender, target);
            }
            case "chest" -> {
                if (sender instanceof Player player) {
                    plugin.getGuiFactory().create(GuiContext.of(
                            plugin,
                            GuiLoader.getGuiConfiguration(GuiModel.CHEST),
                            player,
                            clan)
                    ).open(player);
                } else {
                    sender.sendMessage("Players only command");
                }
            }
        }
        return true;
    }

    private static final List<String> SUBCOMMANDS = List.of(
            "disband", "rename", "setprefix", "setslogan",
            "balance", "withdraw", "deposit",
            "base", "resetbase", "transfer", "chest"
    );

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        if (args.length == 2) {
            return plugin.getClanManager().getClanList(Integer.MAX_VALUE).stream()
                    .map(Clan::getId)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }

        if (args.length == 3) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .toList();
        }

        if (args.length == 4) {
            return switch (args[2].toLowerCase()) {
                case "transfer" -> null;
                default -> List.of();
            };
        }

        return List.of();
    }

    @Override
    public CommandType commandType() {
        return CommandType.ADMIN;
    }

    public void disband(Clan clan, Player initiator) {
        if (plugin.getClanManager().lifecycle().deleteClan(clan, initiator)) {
            plugin.getMessages().of(initiator, "clan-disband")
                    .with(clan)
                    .run();
        }
    }
    public void rename(Clan clan, String newTag) {
        plugin.getClanManager().lifecycle().renameClan(clan, newTag);
    }
    public void setPrefix(Clan clan, String prefix) {
        clan.setPrefix(prefix);
    }
    public void setSlogan(Clan clan, String slogan) {
        clan.setSlogan(slogan);
    }

    public void transfer(Clan clan, CommandSender sender, String targetName) {
        OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(targetName);
        if (clan.getMember(target.getUniqueId()) == null) {
            if (sender instanceof Player player) {
                plugin.getMessages().of(player, "player-not-in-clan")
                        .with(clan)
                        .run();
            } else {
                sender.sendMessage("That player should be in the same clan");
            }
            return;
        }

        clan.transfer(clan.getMember(target.getUniqueId()));

        if (sender instanceof Player player) {
            plugin.getMessages().of(player, "clan-transfer")
                    .replace("{target}", target.getName())
                    .run();
        } else {
            sender.sendMessage("Clan successfully transferred");
        }
    }


}
