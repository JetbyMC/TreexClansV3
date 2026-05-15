package me.jetby.clans.api.gui;

import lombok.Getter;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.libb.color.Serializer;
import me.jetby.libb.gui.item.ItemWrapper;
import me.jetby.libb.gui.parser.ConfigurableClickEvent;
import me.jetby.libb.gui.parser.Item;
import me.jetby.libb.gui.parser.ParsedGui;
import me.jetby.libb.gui.parser.ParserContext;
import org.bukkit.configuration.file.FileConfiguration;
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
    public void refresh() {
        super.refresh();
    }

    @Override
    public void nextPage() {
        super.nextPage();
    }

    @Override
    public void prevPage() {
        super.prevPage();
    }

    @Override
    public void open(@NotNull Player player) {
        super.open(player);
    }

    @Override
    public void openPage(int page) {
        super.openPage(page);
    }

    @Override
    public void setupLifecycleListeners() {
        super.setupLifecycleListeners();
    }

    @Override
    public void clearInventory() {
        super.clearInventory();
    }

    @Override
    public ItemWrapper buildItemWrapper(Item item, List<Integer> wonSlots) {
        return super.buildItemWrapper(item, wonSlots);
    }

    @Override
    public ItemWrapper buildItemWrapper(Item item) {
        return super.buildItemWrapper(item);
    }

    @Override
    public void dispatchItemClick(@NotNull Player clicker, @NotNull ItemWrapper wrapper, @NotNull Item item, @NotNull InventoryClickEvent event) {
        super.dispatchItemClick(clicker, wrapper, item, event);
    }

    @Override
    public ParsedGui setReplace(String key, String input) {
        return super.setReplace(key, input);
    }

    @Override
    public ParsedGui setReplace(Item item, String key, String input) {
        return super.setReplace(item, key, input);
    }

    @Override
    public String applyPlaceholders(String line) {
        return super.applyPlaceholders(line);
    }

    @Override
    public List<String> applyPlaceholders(List<String> lines) {
        return super.applyPlaceholders(lines);
    }

    @Override
    public ParsedGui addClickHandler(String sectionKey, Consumer<ConfigurableClickEvent> handler) {
        return super.addClickHandler(sectionKey, handler);
    }

    @Override
    public List<Item> getBySectionOption(@NotNull String sectionKey) {
        return super.getBySectionOption(sectionKey);
    }

    @Override
    public Player getViewer() {
        return super.getViewer();
    }

    @Override
    public me.jetby.libb.gui.parser.Gui getGui() {
        return super.getGui();
    }

    @Override
    public Map<String, Consumer<ConfigurableClickEvent>> getClickHandlers() {
        return super.getClickHandlers();
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return super.getPlaceholders();
    }

    @Override
    public Map<Item, Map<String, String>> getIndividualPlaceholders() {
        return super.getIndividualPlaceholders();
    }

    @Override
    public ParserContext getParserContext() {
        return super.getParserContext();
    }

    @Override
    public void lockEmptySlots(boolean cancel) {
        super.lockEmptySlots(cancel);
    }

    @Override
    public @Nullable ItemWrapper getItem(@NotNull String key) {
        return super.getItem(key);
    }

    @Override
    public @Nullable ItemWrapper getItemBySlot(int slot) {
        return super.getItemBySlot(slot);
    }

    @Override
    public void setItem(@NotNull String key, @NotNull ItemWrapper wrapper) {
        super.setItem(key, wrapper);
    }

    @Override
    public InventoryHolder getHolder() {
        return super.getHolder();
    }

    @Override
    public @Nullable Consumer<InventoryClickEvent> onClick() {
        return super.onClick();
    }

    @Override
    public @Nullable Consumer<InventoryDragEvent> onDrag() {
        return super.onDrag();
    }

    @Override
    public @Nullable Consumer<InventoryOpenEvent> onOpen() {
        return super.onOpen();
    }

    @Override
    public @Nullable Consumer<InventoryCloseEvent> onClose() {
        return super.onClose();
    }

    @Override
    public void onClick(Consumer<InventoryClickEvent> event) {
        super.onClick(event);
    }

    @Override
    public void onDrag(Consumer<InventoryDragEvent> event) {
        super.onDrag(event);
    }

    @Override
    public void onOpen(Consumer<InventoryOpenEvent> event) {
        super.onOpen(event);
    }

    @Override
    public void onClose(Consumer<InventoryCloseEvent> event) {
        super.onClose(event);
    }

    @Override
    public void updateItem(@NotNull String key) {
        super.updateItem(key);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return super.getInventory();
    }

    @Override
    public Map<String, ItemWrapper> getWrappers() {
        return super.getWrappers();
    }

    @Override
    public boolean isLockEmptySlots() {
        return super.isLockEmptySlots();
    }

    @Override
    public LinkedList<ItemWrapper> getPageItems() {
        return super.getPageItems();
    }

    @Override
    public Integer[] getContentSlots() {
        return super.getContentSlots();
    }

    @Override
    public int getCurrentPage() {
        return super.getCurrentPage();
    }

    @Override
    public void everyPageLogic() {
        super.everyPageLogic();
    }

    @Override
    public void addItem(ItemWrapper wrapper) {
        super.addItem(wrapper);
    }

    @Override
    public void contentSlots(Integer... slots) {
        super.contentSlots(slots);
    }

    @Override
    public void buildItems(List<Item> items) {
        if (items == null) return;
        super.buildItems(items.stream()
                .filter(item -> !cancelRegistration(item))
                .toList());
    }


}