package org.jetby.clans.api.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetby.clans.api.addons.commands.CommandService;

import java.util.List;

/**
 * Represents a subcommand in the TreexClans command system.
 * <p>
 * Each subcommand handles its own execution logic and
 * tab-completion behavior. It is typically registered
 * through the {@link CommandService}.
 * </p>
 *
 * <p>
 * Example:
 * <pre>{@code
 * public class ClanCreateCommand implements Subcommand {
 *
 *     @Override
 *     public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
 *         // Your command logic here
 *         sender.sendMessage("Clan created!");
 *         return true;
 *     }
 *
 *     @Override
 *     public List<String> onTabCompleter(@NotNull CommandSender sender,
 *                                        @NotNull Command command,
 *                                        @NotNull String alias,
 *                                        @NotNull String[] args) {
 *         return List.of("example");
 *     }
 *
 *     @Override
 *     public CommandService.CommandType type() {
 *         return CommandService.CommandType.ADMIN;
 *     }
 * }
 * }</pre>
 * </p>
 */
public interface Subcommand {
    /**
     * Called when the subcommand is executed by a player or the console.
     * <p>
     * Implement this method to define the behavior of your subcommand.
     * Return {@code true} if the command was handled successfully.
     * </p>
     *
     * @param sender  The command sender (player or console).
     * @param command The command name that player used.
     * @param sub     The sub command name that player used.
     * @param args    The command arguments.
     * @return {@code true} if handled successfully, {@code false} otherwise.
     */
    boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String sub, @NotNull String[] args);

    /**
     * Provides tab-completion suggestions for this subcommand.
     * <p>
     * Return a list of possible argument completions or {@code null}
     * if this command does not support tab completion.
     * </p>
     *
     * @param sender  The command sender.
     * @param command The command being executed.
     * @param alias   The command alias used.
     * @param args    The arguments typed so far.
     * @return A list of suggestions, or {@code null} if none.
     */
    @Nullable
    List<String> onTabCompleter(@NotNull CommandSender sender,
                                @NotNull Command command,
                                @NotNull String alias,
                                @NotNull String[] args);

    /**
     * Defines the subcommand type.
     * <p>
     * Determines the command’s context or permission level,
     * such as player-only, admin, or console.
     * </p>
     *
     * @return The {@link CommandType} of this subcommand.
     */
    CommandType commandType();


    default boolean clanOnly() {
        return true;
    }

    /**
     * Defines available types of command categories managed by the service.
     */
    enum CommandType {
        /**
         * Represents player-level commands, e.g. "/clan create", "/clan join", etc.
         */
        CLAN,

        /**
         * Represents administrator-level commands, e.g. "/treexclans reload", "/treexclans give", etc.
         */
        ADMIN
    }

}
