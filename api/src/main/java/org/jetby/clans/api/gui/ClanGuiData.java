package org.jetby.clans.api.gui;

import lombok.Getter;
import lombok.Setter;
import org.jetby.libb.action.record.ActionBlock;
import org.jetby.libb.action.record.Expression;
import org.jetby.libb.gui.parser.Gui;
import org.jetby.libb.gui.parser.Item;

import java.util.List;

@Getter
@Setter
public class ClanGuiData extends Gui {

    private final String id;
    private final String title;
    private final int size;
    private final List<String> command;
    private final List<Expression> preOpenExpressions;
    private final ActionBlock onOpen;
    private final ActionBlock onClose;
    private final List<Item> items;
    private final String renderer;
    private final List<String> args;

    public ClanGuiData(String id,
                       String title,
                       int size,
                       List<String> command,
                       List<Expression> preOpenExpressions,
                       ActionBlock onOpen,
                       ActionBlock onClose,
                       List<Item> items,
                       String renderer,
                       List<String> args
    ) {
        super(id, title, size, command, preOpenExpressions, onOpen, onClose, items);
        this.id = id;
        this.title = title;
        this.size = size;
        this.command = command;
        this.preOpenExpressions = preOpenExpressions;
        this.onOpen = onOpen;
        this.onClose = onClose;
        this.items = items;
        this.renderer = renderer;
        this.args = args;
    }

    public boolean isNamespaced() {
        return renderer != null && renderer.contains(":");
    }
}