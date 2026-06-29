package org.jetby.clans.api.service.clan;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetby.clans.api.service.clan.member.Member;

import java.util.UUID;

/**
 * Clan and member lookup utilities.
 * <p>
 * Provides fast, cache-aware access to clan and member
 * information based on UUID, name, or member object.
 * </p>
 */
public interface Lookup {

    /**
     * Checks whether a player with the given UUID is in a clan.
     *
     * @param uuid the player’s UUID.
     * @return true if the player belongs to a clan.
     */
    boolean isInClan(@NotNull UUID uuid);

    /**
     * Retrieves a clan by its name.
     *
     * @param name the clan name.
     * @return the {@link Clan} instance or {@code null} if not found.
     */
    @Nullable Clan getClan(@NotNull String name);

    /**
     * Retrieves a clan by a member’s UUID.
     *
     * @param uuid the member’s UUID.
     * @return the {@link Clan} instance or {@code null} if not found.
     */
    @Nullable Clan getClanByMember(@NotNull UUID uuid);

    /**
     * Retrieves a clan by a {@link Member} instance.
     *
     * @param member the member.
     * @return the {@link Clan} instance or {@code null} if not found.
     */
    @Nullable Clan getClanByMember(@NotNull Member member);

}