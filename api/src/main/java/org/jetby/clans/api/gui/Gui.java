package org.jetby.clans.api.gui;

import lombok.Getter;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.libb.gui.item.ItemWrapper;
import org.jetby.libb.gui.parser.ConfigurableClickEvent;
import org.jetby.libb.gui.parser.Item;
import org.jetby.libb.gui.parser.ParsedGui;
import org.jetby.libb.gui.parser.ParserContext;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class Gui extends ParsedGui {

    private final ClanGuiData guiData;
    private final Clan clan;
    private final JavaPlugin plugin;

    public Gui(@NotNull GuiContext ctx) {
        // todo get serializer from config
        super(ctx.getPlayer(), ctx.getGui(), ctx.getPlugin(),
                ParserContext.of(ctx.getSerializer(), ctx.getClan())
        );
        // for [refresh] action
        getParserContext().actionsObjects().put(Gui.class, this);

        this.clan = ctx.getClan();
        this.plugin = ctx.getPlugin();
        this.guiData = ctx.getGui();
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