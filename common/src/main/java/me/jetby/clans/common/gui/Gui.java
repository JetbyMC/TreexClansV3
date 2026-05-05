package me.jetby.clans.common.gui;

import lombok.Getter;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.libb.color.Serializer;
import me.jetby.libb.gui.parser.Item;
import me.jetby.libb.gui.parser.ParsedGui;
import me.jetby.libb.gui.parser.ParserContext;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class Gui extends ParsedGui {

    private final String listen;
    private final GuiType type;
    private final List<String> args;

    private final Clan clan;

    public Gui(@NotNull Player viewer, @NotNull FileConfiguration config, @NotNull JavaPlugin plugin, Clan clan) {
        // todo get serializer from config
        super(viewer, config, plugin, ParserContext.of(Serializer.UNIFIED, clan));

        this.clan = clan;
        this.type = GuiType.valueOf(config.getString("type", "default").toUpperCase());
        this.listen = config.getString("listen");
        this.args = config.getStringList("open_args");
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