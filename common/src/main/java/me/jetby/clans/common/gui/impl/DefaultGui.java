package me.jetby.clans.common.gui.impl;

import me.jetby.clans.api.gui.Gui;
import me.jetby.clans.api.service.clan.Clan;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class DefaultGui extends Gui {

    public DefaultGui(@NotNull Player viewer, @NotNull FileConfiguration config, @NotNull JavaPlugin plugin, Clan clan) {
        super(viewer, config, plugin, clan);
    }

}
