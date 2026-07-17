package org.jetby.clans.api.service.clan;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Clan creation and deletion lifecycle management.
 * <p>
 * Handles the creation, registration, and removal of clans.
 * This API ensures that all related data and members
 * are properly handled when clans are created or deleted.
 * </p>
 */
public interface Lifecycle {

    /**
     * Creates a new clan with the specified name and data.
     *
     * @param name the clan name.
     * @param clan the clan instance.
     * @return true if successfully created, false otherwise.
     */
    boolean createClan(@NotNull String name, @NotNull Clan clan);

    /**
     * Creates a new clan with the given name and leader.
     *
     * @param name   the clan name.
     * @param leader the player who becomes the clan leader.
     * @return true if the clan was successfully created.
     */
    boolean createClan(@NotNull String name, @NotNull Player leader);

    /**
     * Deletes a clan and performs all necessary cleanup.
     *
     * @param clan      the clan to delete.
     * @param initiator the player who initiated deletion (nullable).
     */
    boolean deleteClan(@NotNull Clan clan, @Nullable Player initiator);

    /**
     * Deletes a clan by its name.
     *
     * @param name the clan name.
     * @return true if deletion succeeded, false otherwise.
     */
    boolean deleteClan(@NotNull String name);

    /**
     * Checks whether a clan with the given name exists.
     *
     * @param name the clan name.
     * @return true if a clan with that name exists.
     */
    boolean clanExists(@NotNull String name);

    /**
     * Renames the tag of the clan
     *
     * @param clan the clan.
     * @param newId new tag.
     * @return true if a clan with that name exists.
     */
    boolean renameClan(@NotNull Clan clan, @NotNull String newId);

}