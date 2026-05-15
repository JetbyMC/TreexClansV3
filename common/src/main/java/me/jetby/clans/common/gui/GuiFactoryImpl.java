package me.jetby.clans.common.gui;

import lombok.Getter;
import me.jetby.clans.api.gui.*;
import me.jetby.clans.common.gui.core.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
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
            case CHEST            -> new ChestGui(ctx);
            case MEMBERS          -> new MembersGui(ctx);
            case RANKS            -> new RanksGui(ctx);
            case RANK_PERMISSIONS -> new RankPermissionGui(ctx);
            default               -> new Gui(ctx);
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
    public @Nullable ClanGuiData getGui(String id) {
        return GuiLoader.getGuiConfiguration(id);
    }

    @Override
    public @Nullable ClanGuiData getGui(GuiModel model) {
        return GuiLoader.getGuiConfiguration(model);
    }

    @Override
    public @Nullable ClanGuiData findGui(Predicate<ClanGuiData> predicate) {
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
}