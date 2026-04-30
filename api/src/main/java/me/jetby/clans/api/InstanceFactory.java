package me.jetby.clans.api;

import lombok.experimental.UtilityClass;
import me.jetby.clans.api.service.clan.Clan;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;

/**
 * A global static registry (factory) of commonly used plugin instances and constants.
 * <p>
 * This class can hold any static objects — NamespacedKeys, managers,
 * configurations, singletons, or helper utilities.
 * <p>
 * Use it as a central place to store shared references initialized during plugin startup.
 */
@UtilityClass
public class InstanceFactory {

    /**
     * Example: global NamespacedKey factory base
     */
    public NamespacedKey ITEM_KEY;

    // todo idk, хз нужно не нужно ну по идее да но каждый плагин хочет по своему хранить кланы
    /**
     * @see Clan
     */
    public Map<String, Clan> CLAN_LIST = new HashMap<>();
}
