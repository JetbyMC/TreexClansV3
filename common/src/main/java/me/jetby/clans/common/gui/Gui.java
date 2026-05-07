package me.jetby.clans.common.gui;

import lombok.Getter;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.libb.color.Serializer;
import me.jetby.libb.gui.parser.Item;
import me.jetby.libb.gui.parser.ParsedGui;
import me.jetby.libb.gui.parser.ParserContext;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class Gui extends ParsedGui {

    private final ExtendedGui guiData;
    private final Clan clan;
    private final JavaPlugin plugin;

    public Gui(@NotNull Player viewer, @NotNull ExtendedGui guiData, @NotNull JavaPlugin plugin, Clan clan) {
        // todo get serializer from config
        super(viewer, guiData, plugin, ParserContext.of(Serializer.UNIFIED, clan));
        this.guiData = guiData;

        this.plugin = plugin;
        this.clan = clan;
    }

    public boolean cancelRegistration(@NotNull Item item) {
        return false;
    }

    @Override
    public void buildItems(List<Item> items) {
        if (items == null) return;
        super.buildItems(items.stream()
                .filter(item -> !cancelRegistration(item))
                .toList());
    }


}