package me.jetby.clans.api.gui;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public interface GuiFactory {
    Gui create(GuiContext ctx);
    void register(String type, GuiCreator creator);
    void unregister(String type);

    @Nullable
    ClanGuiData getGui(String id);
    @Nullable ClanGuiData getGui(GuiModel model);
    @Nullable ClanGuiData findGui(Predicate<ClanGuiData> predicate);
}
