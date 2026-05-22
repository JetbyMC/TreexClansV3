package org.jetby.clans.common.commands.admin.subcommands;

import org.bukkit.entity.Player;
import org.jetby.clans.api.addons.commands.CommandService;
import org.jetby.clans.api.command.Subcommand;
import org.jetby.clans.api.gui.GuiContext;
import org.jetby.clans.api.gui.GuiFactory;
import org.jetby.clans.api.gui.GuiModel;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.gui.GuiLoader;
import org.jetby.clans.common.storage.Storage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StorageSubcommand implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String sub,  @NotNull String[] args) {

        if (sender instanceof Player player) {
            Clan clan = plugin.getClanManager().lookup().getClan(args[0]);
            if (clan==null) return true;

            plugin.getGuiFactory().create(GuiContext.of(
                    plugin,
                    GuiLoader.getGuiConfiguration(GuiModel.CHEST),
                    player,
                    clan)
            ).open(player);
        }


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
