package me.jetby.clans.common.commands.admin.subcommands;

import me.jetby.clans.api.addons.commands.CommandService;
import me.jetby.clans.api.command.Subcommand;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.storage.Storage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StorageSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String sub,  @NotNull String[] args) {

        // TODO sex
//        if (sender instanceof Player player) {
//
//            Menu menu = plugin.getGuiLoader().getMenus().values().stream()
//                    .filter(m -> m.type().equalsIgnoreCase("chest"))
//                    .findFirst()
//                    .orElse(null);
//            if (menu != null) {
//
//                if (args.length > 0) {
//                    String clanName = args[0].toLowerCase();
//                    var clanImpl = plugin.getClanManager().lookup().getClan(clanName);
//                    if (clanImpl == null) return true;
//
//                    Gui gui = new ChestGui(plugin, menu, player, clanImpl);
//                    gui.open(player);
//
//                    return true;
//                } else {
//                    sender.sendMessage("/xclan storage <clan>");
//                }
//
//            }
//        }


        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        List<String> completions = new ArrayList<>(Storage.CLANS.keySet());

        return completions.stream()
                .filter(cmd -> cmd.startsWith(args[1].toLowerCase()))
                .toList();

    }

    @Override
    public CommandService.CommandType type() {
        return CommandService.CommandType.ADMIN;
    }
}
