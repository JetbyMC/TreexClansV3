package org.jetby.clans.api.gui;

@FunctionalInterface
public interface GuiCreator {
    Gui create(GuiContext ctx);
}
