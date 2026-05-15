package me.jetby.clans.common.gui.core;

import me.jetby.clans.api.gui.Gui;
import me.jetby.clans.api.gui.GuiContext;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.libb.InstanceFactory;
import me.jetby.libb.gui.item.ItemWrapper;
import me.jetby.libb.gui.parser.Item;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChestGui extends Gui {

    private static final Map<Clan, Map<UUID, ChestGui>> openGuis = new ConcurrentHashMap<>();

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

        openGuis.computeIfAbsent(getClan(), k -> new ConcurrentHashMap<>())
                .put(ctx.getPlayer().getUniqueId(), this);

        for (int i = 0; i < totalWithPadding; i++) {
            addItem(new ItemWrapper(new ItemStack(AIR_ITEM)));
        }

        Consumer<InventoryClickEvent> onClick = onClick();
        onClick(event -> {
            if (onClick != null) onClick.accept(event);


            if (!slots.contains(event.getSlot())) return;
            int guiSlot = event.getSlot();
            int pageOffset = slots.indexOf(guiSlot);
            if (pageOffset == -1) return;
            int index = getCurrentPage() * perPage + pageOffset;
            if (index >= totalSlots) {
                event.setCancelled(true);
                return;
            }
            event.setCancelled(false);
            getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
                ItemStack current = getInventory().getItem(guiSlot);
                getClan().getChest().put(index, current != null ? current : new ItemStack(AIR_ITEM));
                syncToOpenViewers(getCurrentPage());
            });
        });

        onClose(event -> {
            openGuis.getOrDefault(getClan(), Map.of())
                    .remove(ctx.getPlayer().getUniqueId());
        });
    }

    private static final ItemStack AIR_ITEM = new ItemStack(Material.AIR);

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

        if (item!=null) {
            ItemWrapper wrapper = buildItemWrapper(item);
            wrapper.slots(blockedSlots.toArray(new Integer[0]));
            setItem("blocked-slot", wrapper);
        }



    }

    private void syncToOpenViewers(int page) {
        Map<UUID, ChestGui> clanGuis = openGuis.get(getClan());
        if (clanGuis == null) return;
        clanGuis.values().stream()
                .filter(gui -> gui != this)
                .filter(gui -> gui.getCurrentPage() == page)
                .forEach(gui -> getPlugin().getServer().getScheduler().runTask(getPlugin(), () ->
                        gui.openPage(gui.getCurrentPage())));
    }
}