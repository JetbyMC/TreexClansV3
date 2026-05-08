package me.jetby.clans.common.commands;

import me.jetby.clans.common.TreexClans;
import me.jetby.libb.command.AdvancedCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClanCommand1 extends AdvancedCommand {

    public ClanCommand1(@NotNull String commandName, @NotNull TreexClans plugin) {
        super(commandName, plugin);
    }

    @Override
    protected boolean onExecute(CommandSender sender, Command command, String label, String[] args) {

        return true;
    }

    @Override
    protected List<String> onTab(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }
}
