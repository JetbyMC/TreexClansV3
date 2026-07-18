package org.jetby.clans.common.gui.core;

import com.google.common.annotations.Beta;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetby.clans.api.gui.Gui;
import org.jetby.clans.api.gui.GuiContext;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.common.TreexClans;
import org.jetby.libb.gui.item.ItemWrapper;
import org.jetby.libb.gui.parser.Item;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Beta
public class ChestGui extends Gui {

    private static final Map<Clan, Map<UUID, ChestGui>> OPEN_GUIS = new ConcurrentHashMap<>();
    private static final ItemStack AIR_ITEM = new ItemStack(Material.AIR);

    public ChestGui(@NotNull GuiContext ctx) {
        super(ctx);

        lockEmptySlots(false);

        List<Item> items = getBySectionOption("type").stream()
                .filter(b -> "items".equalsIgnoreCase(b.type()))
                .toList();

        List<Integer> slots = items.stream()
                .flatMap(item -> item.slots().stream())
                .toList();

        contentSlots(slots.toArray(new Integer[0]));

        int totalSlots = getClan().getLevel().chest();
        int perPage = slots.size();
        int totalWithPadding = totalSlots + (perPage - totalSlots % perPage) % perPage;

        EnumSet<Material> allowedMaterials = ((TreexClans) getPlugin()).getCfg().getAvailableStorageMaterials();

        OPEN_GUIS.computeIfAbsent(getClan(), k -> new ConcurrentHashMap<>())
                .put(ctx.getPlayer().getUniqueId(), this);

        for (int i = 0; i < totalWithPadding; i++) {
            addItem(new ItemWrapper(AIR_ITEM));
        }

        Consumer<InventoryClickEvent> onClick = onClick();
        onClick(event -> {
            if (onClick != null) onClick.accept(event);

            boolean clickedGui = getInventory().equals(event.getClickedInventory());
            boolean clickedPlayer = event.getWhoClicked().getInventory().equals(event.getClickedInventory());

            if (clickedPlayer && event.getClick().isShiftClick()) {
                event.setCancelled(true);
                ItemStack moving = event.getCurrentItem();
                if (moving == null || moving.getType() == Material.AIR) return;
                if (!allowedMaterials.contains(moving.getType())) return;

                int firstFree = findFirstFreeSlot(slots, totalSlots, perPage);
                if (firstFree == -1) return;

                int guiSlot = slots.get(firstFree % perPage);
                int index = (getCurrentPage() * perPage) + firstFree % perPage;

                getInventory().setItem(guiSlot, moving);
                event.getWhoClicked().getInventory().setItem(event.getSlot(), AIR_ITEM);

                getClan().getChest().put(index, moving);
                syncToOpenViewers(getCurrentPage());
                return;
            }
            if (clickedGui && event.getClick() == ClickType.NUMBER_KEY) {
                int guiSlot = event.getSlot();
                if (!slots.contains(guiSlot)) {
                    event.setCancelled(true);
                    return;
                }
                int pageOffset = slots.indexOf(guiSlot);
                int index = getCurrentPage() * perPage + pageOffset;
                if (index >= totalSlots) {
                    event.setCancelled(true);
                    return;
                }
                ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                if (hotbarItem != null && hotbarItem.getType() != Material.AIR && !allowedMaterials.contains(hotbarItem.getType())) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (!clickedGui) return;

            int guiSlot = event.getSlot();

            if (!slots.contains(guiSlot)) {
                event.setCancelled(true);
                return;
            }

            int pageOffset = slots.indexOf(guiSlot);
            int index = getCurrentPage() * perPage + pageOffset;

            if (index >= totalSlots) {
                event.setCancelled(true);
                return;
            }

            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR && !allowedMaterials.contains(cursor.getType())) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(false);

            getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
                ItemStack current = getInventory().getItem(guiSlot);
                getClan().getChest().put(index, current != null ? current : AIR_ITEM);
                syncToOpenViewers(getCurrentPage());
            });
        });

        Consumer<InventoryDragEvent> onDrag = onDrag();
        onDrag(event -> {
            if (onDrag != null) onDrag.accept(event);

            ItemStack dragged = event.getOldCursor();
            if (!allowedMaterials.contains(dragged.getType())) {
                event.setCancelled(true);
                return;
            }

            boolean hasInvalidSlot = event.getRawSlots().stream().anyMatch(slot -> {
                if (slot >= getInventory().getSize()) return false;
                if (!slots.contains(slot)) return true;
                int pageOffset = slots.indexOf(slot);
                int index = getCurrentPage() * perPage + pageOffset;
                return index >= totalSlots;
            });

            if (hasInvalidSlot) {
                event.setCancelled(true);
            } else {
                getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
                    for (int slot : event.getRawSlots()) {
                        if (slot >= getInventory().getSize()) continue;
                        int pageOffset = slots.indexOf(slot);
                        if (pageOffset == -1) continue;
                        int index = getCurrentPage() * perPage + pageOffset;
                        if (index >= totalSlots) continue;
                        ItemStack current = getInventory().getItem(slot);
                        getClan().getChest().put(index, current != null ? current : AIR_ITEM);
                    }
                    syncToOpenViewers(getCurrentPage());
                });
            }
        });

        onClose(event -> {
            OPEN_GUIS.getOrDefault(getClan(), Map.of())
                    .remove(ctx.getPlayer().getUniqueId());
        });
    }

    private int findFirstFreeSlot(List<Integer> slots, int totalSlots, int perPage) {
        for (int i = 0; i < totalSlots; i++) {
            int page = i / perPage;
            int pageOffset = i % perPage;
            if (page != getCurrentPage()) continue;
            int guiSlot = slots.get(pageOffset);
            ItemStack current = getInventory().getItem(guiSlot);
            if (current == null || current.getType() == Material.AIR) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void openPage(int page) {
        Integer[] contentSlots = getContentSlots();
        if (contentSlots == null) return;

        setCurrentPage(page);

        List<Integer> slots = List.of(contentSlots);
        int perPage = slots.size();
        int totalSlots = getClan().getLevel().chest();
        int from = page * perPage;

        List<Integer> blockedSlots = new ArrayList<>();
        for (int i = 0; i < perPage; i++) {
            int chestIndex = from + i;
            int guiSlot = slots.get(i);

            if (chestIndex < totalSlots) {
                ItemStack item = getClan().getChest().getOrDefault(chestIndex, AIR_ITEM);
                getInventory().setItem(guiSlot, item);
            } else {
                blockedSlots.add(guiSlot);
            }
        }

        Item item = getBySectionOption("type")
                .stream()
                .filter(b -> "blocked-slot".equalsIgnoreCase(b.type()))
                .findFirst()
                .orElse(null);

        if (item != null) {
            ItemWrapper wrapper = buildItemWrapper(item);
            wrapper.slots(blockedSlots.toArray(new Integer[0]));
            setItem("blocked-slot", wrapper);
        }
    }

    private void syncToOpenViewers(int page) {
        Map<UUID, ChestGui> clanGuis = OPEN_GUIS.get(getClan());
        if (clanGuis == null) return;
        clanGuis.values().stream()
                .filter(gui -> gui != this)
                .filter(gui -> gui.getCurrentPage() == page)
                .forEach(gui -> getPlugin().getServer().getScheduler().runTask(getPlugin(), () ->
                        gui.openPage(gui.getCurrentPage())));
    }
}