package org.jetby.clans.api.gui;

import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public interface GuiFactory {
    Gui create(GuiContext ctx);

    void register(String type, GuiCreator creator);

    void unregister(String type);

    @Nullable
    ClanGuiData get(String id);

    @Nullable
    ClanGuiData get(GuiModel model);

    @Nullable
    ClanGuiData find(Predicate<ClanGuiData> predicate);

    ClanGuiData parse(FileConfiguration configuration);

    void add(String id, ClanGuiData gui);

    void add(ClanGuiData gui);

    void remove(String id);
}
