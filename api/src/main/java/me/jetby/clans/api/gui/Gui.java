package me.jetby.clans.api.gui;

import lombok.Getter;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.libb.color.Serializer;
import me.jetby.libb.gui.parser.Item;
import me.jetby.libb.gui.parser.ParsedGui;
import me.jetby.libb.gui.parser.ParserRule;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class Gui extends ParsedGui {

    private final String listen;
    private final List<String> args;

    public Gui(@NotNull Player viewer, @NotNull FileConfiguration config, @NotNull JavaPlugin plugin, Clan clan) {
        super(viewer, config, plugin, ParserRule.of(Serializer.UNIFIED));

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