package me.jetby.clans.common.gui.core;

import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.common.gui.ExtendedGui;
import me.jetby.clans.common.gui.Gui;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class QuestsGui extends Gui {


    public QuestsGui(@NotNull Player viewer, @NotNull ExtendedGui guiData, @NotNull JavaPlugin plugin, Clan clan) {
        super(viewer, guiData, plugin, clan);
    }

}
