package me.jetby.clans.common.gui;

import lombok.Getter;
import lombok.Setter;
import me.jetby.libb.action.record.ActionBlock;
import me.jetby.libb.gui.parser.Gui;
import me.jetby.libb.gui.parser.Item;

import java.util.List;


@Getter
@Setter
public class ExtendedGui extends Gui {

    private final String id;
    private final String title;
    private final int size;
    private final List<String> command;
    private final List<String> preOpenExpressions;
    private final ActionBlock onOpen;
    private final ActionBlock onClose;
    private final List<Item> items;

    private final ListenType listenType;
    private final List<String> args;

    public ExtendedGui(String id,
                       String title,
                       int size,
                       List<String> command,
                       List<String> preOpenExpressions,
                       ActionBlock onOpen,
                       ActionBlock onClose,
                       List<Item> items,
                       ListenType listenType,
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
        this.listenType = listenType;
        this.args = args;

    }
}
