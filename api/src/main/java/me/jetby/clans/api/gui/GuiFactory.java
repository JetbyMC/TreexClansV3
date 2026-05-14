package me.jetby.clans.api.gui;

public interface GuiFactory {
    Gui create(GuiContext ctx);
    void register(String type, GuiCreator creator);
    void unregister(String type);
}
