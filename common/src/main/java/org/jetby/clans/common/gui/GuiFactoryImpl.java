package org.jetby.clans.common.gui;

import lombok.Getter;
import org.jetby.clans.api.gui.*;
import org.jetby.clans.common.gui.core.ChestGui;
import org.jetby.clans.common.gui.core.MembersGui;
import org.jetby.clans.common.gui.core.RankPermissionGui;
import org.jetby.clans.common.gui.core.RanksGui;
import org.jetby.libb.action.record.ActionBlock;
import org.jetby.libb.action.record.Expression;
import org.jetby.libb.gui.parser.Item;
import org.jetby.libb.gui.parser.ParseUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Getter
public class GuiFactoryImpl implements GuiFactory {
    private final Map<String, GuiCreator> customTypes = new HashMap<>();

    @Override
    public Gui create(GuiContext ctx) {
        String renderer = ctx.getGui().getRenderer();

        String rendererKey = renderer.contains(":") ? renderer.split(":")[1].toUpperCase() : renderer.toUpperCase();
        GuiCreator custom = customTypes.get(rendererKey);
        if (custom != null) return custom.create(ctx);

        if (ctx.getGui().isNamespaced()) return new Gui(ctx);

        GuiModel type;
        try {
            type = GuiModel.valueOf(renderer.toUpperCase());
        } catch (Exception e) {
            return new Gui(ctx);
        }

        return switch (type) {
            case CHEST -> new ChestGui(ctx);
            case MEMBERS -> new MembersGui(ctx);
            case RANKS -> new RanksGui(ctx);
            case RANK_PERMISSIONS -> new RankPermissionGui(ctx);
            default -> new Gui(ctx);
        };
    }

    @Override
    public void register(String type, GuiCreator creator) {
        customTypes.put(type.toUpperCase(), creator);
    }

    @Override
    public void unregister(String type) {
        customTypes.remove(type.toUpperCase());
    }

    @Override
    public @Nullable ClanGuiData get(String id) {
        return GuiLoader.getGuiConfiguration(id);
    }

    @Override
    public @Nullable ClanGuiData get(GuiModel model) {
        return GuiLoader.getGuiConfiguration(model);
    }


    @Override
    public void add(String id, ClanGuiData gui) {
        GuiLoader.API_GUIS.put(id, gui);
    }

    @Override
    public void add(ClanGuiData gui) {
        GuiLoader.API_GUIS.put(gui.getId(), gui);
    }

    @Override
    public void remove(String id) {
        GuiLoader.API_GUIS.remove(id);
    }

    @Override
    public @Nullable ClanGuiData find(Predicate<ClanGuiData> predicate) {
        for (ClanGuiData gui : GuiLoader.API_GUIS.values()) {
            if (predicate.test(gui)) return gui;
        }
        for (ClanGuiData gui : GuiLoader.CUSTOM_GUIS.values()) {
            if (predicate.test(gui)) return gui;
        }
        for (ClanGuiData gui : GuiLoader.REQUIRED_GUIS.values()) {
            if (predicate.test(gui)) return gui;
        }
        return null;
    }

    @Override
    public ClanGuiData parse(FileConfiguration configuration) {
        try {
            String id = configuration.getString("id");
            String title = configuration.getString("title");
            String model = configuration.getString("model");
            int size = configuration.getInt("size");
            List<String> command = configuration.getStringList("command");
            List<Expression> preOpenExpressions = ParseUtil.getExpressions(configuration.getStringList("pre_open"));
            ActionBlock onOpen = ParseUtil.getActionBlock(configuration, "on_open");
            ActionBlock onClose = ParseUtil.getActionBlock(configuration, "on_close");
            List<String> args = configuration.getStringList("open_args");
            List<Item> items = ParseUtil.getItems(configuration);
            return new ClanGuiData(id, title, size, command, preOpenExpressions, onOpen, onClose, items, model, args);
        } catch (Exception e) {
            throw new RuntimeException("Error trying to load menu: " + e.getMessage());
        }
    }
}