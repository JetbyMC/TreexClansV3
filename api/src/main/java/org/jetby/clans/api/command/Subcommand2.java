package org.jetby.clans.api.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Subcommand2 {

    boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String sub, @NotNull String[] args);

    @Nullable
    default List<String> onTabCompleter(@NotNull CommandSender sender,
                                        @NotNull Command command,
                                        @NotNull String alias,
                                        @NotNull String[] args) {
        return List.of();
    }


}
