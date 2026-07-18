package org.jetby.clans.common.commands;

import lombok.Getter;
import org.jetby.clans.api.addons.commands.CommandService;
import org.jetbrains.annotations.NotNull;
import org.jetby.clans.api.command.Subcommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class CommandServiceImpl implements CommandService {
    private final Map<String, Subcommand> commands = new ConcurrentHashMap<>();

    @Override
    public void registerCommand(@NotNull String name, @NotNull Subcommand subcommand) {
        if (name.isBlank())
            throw new IllegalArgumentException("Command name and instance cannot be null or empty.");

        commands.put(name.toLowerCase(), subcommand);
    }

    @Override
    public void unregisterCommand(@NotNull String name) {
        commands.remove(name.toLowerCase());
    }
}