package org.jetby.clans.api.service;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.Lifecycle;
import org.jetby.clans.api.service.clan.Lookup;
import org.jetby.clans.api.service.clan.member.Member;

import java.util.List;

/**
 * Central API for interacting with clans in TreexClans.
 * <p>
 * Provides access to all major clan-related features such as:
 * creation, deletion, economy, chat, validation, and visual customization.
 * </p>
 *
 * <p>
 * The {@link ClanManager} acts as the main entry point for addons and plugins
 * that need to interact with the TreexClans system.
 * </p>
 */
public interface ClanManager {

    @NotNull List<Clan> getClanList(int limit);

    /**
     * Provides access to the clan creation and deletion lifecycle.
     *
     * @return the lifecycle management API.
     */
    @NotNull Lifecycle lifecycle();

    /**
     * Provides access to name and prefix validation rules.
     *
     * @return the validation API.
     */
    @NotNull Validation validation();

    /**
     * Provides access to the clan chat and message broadcasting system.
     *
     * @return the chat API.
     */
    @NotNull Chat chat();

    /**
     * Provides access to clan balance and economy-related operations.
     *
     * @return the economy API.
     */
    @NotNull Economy economy();


    /**
     * Provides access to clan and member lookup utilities.
     *
     * @return the lookup API.
     */
    @NotNull Lookup lookup();

    // ------------------------------------------------------------------------
    // Nested interfaces
    // ------------------------------------------------------------------------

    /**
     * Validation and naming rules for clans and prefixes.
     * <p>
     * This interface ensures that all clan names, prefixes,
     * and input values meet the plugin’s configuration standards.
     * </p>
     */
    interface Validation {

        /**
         * Checks whether a clan name is allowed for creation.
         *
         * @param player   the player attempting to create the clan.
         * @param clanName the proposed clan name.
         * @return true if valid, false otherwise.
         */
        boolean isAllowedName(@NotNull Player player, @NotNull String clanName);

        /**
         * Checks whether a clan prefix is allowed.
         *
         * @param player the player attempting to set the prefix.
         * @param prefix the prefix to validate.
         * @return true if valid, false otherwise.
         */
        boolean isAllowedPrefix(@NotNull Player player, @NotNull String prefix);

        /**
         * Checks whether a clan slogan is allowed.
         *
         * @param player the player attempting to set the slogan.
         * @param slogan the slogan to validate.
         * @return true if valid, false otherwise.
         */
        boolean isAllowedSlogan(@NotNull Player player, @NotNull String slogan);

        /**
         * Validates text using a custom regex pattern.
         *
         * @param text  the text to validate.
         * @param regex the regex rule.
         * @return true if the text matches the regex.
         */
        boolean isAllowedRegex(@NotNull String text, @NotNull String regex);
    }

    /**
     * Handles message broadcasting and clan chat communication.
     * <p>
     * Provides convenient methods for sending formatted messages
     * to entire clans or handling player chat within clan channels.
     * </p>
     */
    interface Chat {

        /**
         * Sends a message to all online members of the specified clan.
         *
         * @param clan    the target clan.
         * @param message the message to send.
         */
        void sendMessage(@NotNull Clan clan, @NotNull String message);

        /**
         * Sends a chat message from one player to all members of their clan.
         *
         * @param clan    the target clan.
         * @param sender  the player sending the message.
         * @param message the chat message content.
         */
        void sendChat(@NotNull Clan clan, @NotNull Player sender, @NotNull String message);
    }

    /**
     * Provides access to clan balance and economy management.
     * <p>
     * Supports adding, removing, and retrieving clan balance values,
     * typically synchronized with an external economy plugin (Vault).
     * </p>
     */
    interface Economy {

        /**
         * Adds balance to a clan’s account.
         *
         * @param amount the amount to add.
         * @param clan   the target clan.
         */
        void addBalance(double amount, @NotNull Clan clan);

        /**
         * Deducts balance from a clan’s account.
         *
         * @param amount the amount to deduct.
         * @param clan   the target clan.
         */
        void takeBalance(double amount, @NotNull Clan clan);

        /**
         * Retrieves the current balance of a clan.
         *
         * @param clan the target clan.
         * @return the clan’s current balance.
         */
        double getBalance(@NotNull Clan clan);
    }

    /**
     * Returns a formatted last online time for a member.
     *
     * @param member the member instance.
     * @return a human-readable last online string.
     */
    @NotNull String getLastOnlineFormatted(@NotNull Member member);
}
