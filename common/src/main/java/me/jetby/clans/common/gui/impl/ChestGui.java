package me.jetby.clans.common.gui.impl;

import me.jetby.clans.common.gui.Gui;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.common.TreexClans;
import me.jetby.libb.gui.parser.Item;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.jetby.clans.common.TreexClans.NAMESPACED_KEY;

public class ChestGui extends Gui {

    private static final Map<String, Set<ChestGui>> ACTIVE_CHESTS = new HashMap<>();

    private final Clan clan;
    private final Map<Integer, Integer> slotToGlobalIndex = new HashMap<>();
    private int currentPage = 0;
    private BukkitTask autoSaveTask;
    private boolean isInitialized = false;

    private List<Integer> configSlots = new ArrayList<>();
    private ItemStack blockedSlotItem = null;

    public ChestGui(Player player, FileConfiguration config, JavaPlugin plugin, Clan clan) {
        super(player, config, plugin, clan);
        this.clan = clan;
        lockEmptySlots(false);

        resolveConfigSlots();
        placeBlockedSlots();

        addClickHandler("next_page", event -> {
            event.setCancelled(true);
            if (currentPage < calcTotalPages() - 1) {
                saveToCloudData();
                currentPage++;
                loadPageFromCloudData();
            }
        });

        addClickHandler("prev_page", event -> {
            event.setCancelled(true);
            if (currentPage > 0) {
                saveToCloudData();
                currentPage--;
                loadPageFromCloudData();
            }
        });

        onClose(event -> {
            if (autoSaveTask != null) autoSaveTask.cancel();
            saveToCloudData();
            unregisterChest();
        });

        registerToActiveChests();

        autoSaveTask = Bukkit.getScheduler().runTaskTimer(plugin, this::saveToCloudData, 100L, 100L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            loadPageFromCloudData();
            isInitialized = true;
        }, 1L);

        onClick(this::handleClick);
    }

    @Override
    public boolean cancelRegistration(@NotNull Item item) {
        if (item.type()!=null && item.section() == null) return false;
        String type = item.section().getString("type", "");
        return type.equals("item") || type.equals("chest") || type.equals("blocked-slot");
    }

    private void resolveConfigSlots() {
        List<Item> allItems = getBySectionOption("type");

        allItems.stream()
                .filter(i -> i.section().getString("type", "").equals("blocked-slot"))
                .findFirst()
                .ifPresent(i -> {
                    ItemStack stack = i.itemStack();
                        blockedSlotItem = stack.clone();
                        ItemMeta meta = blockedSlotItem.getItemMeta();
                        if (meta != null) {
                            meta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "blocked_slot");
                            blockedSlotItem.setItemMeta(meta);
                        
                    }
                });

        configSlots = allItems.stream()
                .filter(i -> {
                    String t = i.section().getString("type", "");
                    return t.equals("item") || t.equals("chest");
                })
                .flatMap(i -> i.slots().stream())
                .distinct()
                .sorted()
                .toList();
    }

    private void placeBlockedSlots() {
        if (configSlots.isEmpty() || blockedSlotItem == null) return;

        int slotsPerPage = configSlots.size();
        int maxChestSlots = clan.getLevel().chest();

        for (int i = 0; i < slotsPerPage; i++) {
            int globalIndex = currentPage * slotsPerPage + i;
            if (globalIndex >= maxChestSlots) {
                getInventory().setItem(configSlots.get(i), blockedSlotItem.clone());
            }
        }
    }

    private void handleClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!p.equals(getViewer())) return;

        Inventory topInv = getInventory();
        Inventory clickedInv = e.getClickedInventory();
        int rawSlot = e.getRawSlot();
        ClickType click = e.getClick();

        if (clickedInv != null && clickedInv.equals(topInv)) {
            if (isBlockedSlot(topInv.getItem(rawSlot))) {
                e.setCancelled(true);
                return;
            }

            if (!slotToGlobalIndex.containsKey(rawSlot)) {
                e.setCancelled(true);
                return;
            }

            ItemStack cursor = e.getCursor();
            if (cursor.getType() != Material.AIR) {
                if (!getAllowedMaterials().contains(cursor.getType()) || isGuiItem(cursor)) {
                    e.setCancelled(true);
                    return;
                }
            }

            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                saveToCloudData();
                notifyOtherViewers();
            }, 1L);
            return;
        }

        if ((click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT)
                && clickedInv != null && clickedInv.equals(p.getInventory())) {

            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            if (!getAllowedMaterials().contains(clicked.getType()) || isGuiItem(clicked)) {
                e.setCancelled(true);
                return;
            }

            e.setCancelled(true);

            int remaining = clicked.getAmount();
            List<Integer> availableSlots = new ArrayList<>(slotToGlobalIndex.keySet());
            availableSlots.sort(Integer::compare);

            for (int guiSlot : availableSlots) {
                ItemStack slotItem = topInv.getItem(guiSlot);

                if (slotItem == null || slotItem.getType() == Material.AIR) {
                    int toPlace = Math.min(remaining, clicked.getMaxStackSize());
                    ItemStack toSet = clicked.clone();
                    toSet.setAmount(toPlace);
                    topInv.setItem(guiSlot, toSet);
                    remaining -= toPlace;
                    if (remaining <= 0) {
                        e.setCurrentItem(null);
                        break;
                    }
                    continue;
                }

                if (slotItem.isSimilar(clicked) && slotItem.getAmount() < slotItem.getMaxStackSize()) {
                    int space = slotItem.getMaxStackSize() - slotItem.getAmount();
                    int toAdd = Math.min(space, remaining);
                    slotItem.setAmount(slotItem.getAmount() + toAdd);
                    remaining -= toAdd;
                    if (remaining <= 0) {
                        e.setCurrentItem(null);
                        break;
                    }
                }
            }

            if (remaining > 0 && remaining < clicked.getAmount()) {
                ItemStack leftover = clicked.clone();
                leftover.setAmount(remaining);
                e.setCurrentItem(leftover);
            }

            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                saveToCloudData();
                notifyOtherViewers();
            }, 1L);
        }
    }

    private boolean isBlockedSlot(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null
                && "blocked_slot".equals(meta.getPersistentDataContainer().get(NAMESPACED_KEY, PersistentDataType.STRING));
    }

    private boolean isGuiItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(NAMESPACED_KEY, PersistentDataType.STRING);
    }

    private void updateSlotMapping() {
        slotToGlobalIndex.clear();
        if (configSlots.isEmpty()) return;

        int slotsPerPage = configSlots.size();
        int maxChestSlots = clan.getLevel().chest();

        for (int i = 0; i < slotsPerPage; i++) {
            int globalIndex = currentPage * slotsPerPage + i;
            if (globalIndex < maxChestSlots) {
                slotToGlobalIndex.put(configSlots.get(i), globalIndex);
            }
        }
    }

    private void saveToCloudData() {
        if (!isInitialized) return;

        updateSlotMapping();

        Inventory inv = getInventory();
        List<ItemStack> chestData = clan.getChest();

        int maxIndex = slotToGlobalIndex.values().stream().max(Integer::compare).orElse(-1);
        while (chestData.size() <= maxIndex) chestData.add(null);

        for (Map.Entry<Integer, Integer> entry : slotToGlobalIndex.entrySet()) {
            int guiSlot = entry.getKey();
            int globalIndex = entry.getValue();

            ItemStack item = inv.getItem(guiSlot);
            if (isBlockedSlot(item) || isGuiItem(item)) continue;

            chestData.set(globalIndex, (item == null || item.getType() == Material.AIR) ? null : item.clone());
        }

        while (!chestData.isEmpty() && chestData.get(chestData.size() - 1) == null) {
            chestData.remove(chestData.size() - 1);
        }

        notifyOtherViewers();
    }

    private void loadPageFromCloudData() {
        updateSlotMapping();

        Inventory inv = getInventory();
        List<ItemStack> chestData = clan.getChest();
        EnumSet<Material> allowed = getAllowedMaterials();
        int maxChestSlots = clan.getLevel().chest();
        int slotsPerPage = configSlots.size();

        for (Map.Entry<Integer, Integer> entry : slotToGlobalIndex.entrySet()) {
            int guiSlot = entry.getKey();
            int globalIndex = entry.getValue();

            ItemStack item = globalIndex < chestData.size() ? chestData.get(globalIndex) : null;
            if (item == null || item.getType() == Material.AIR || !allowed.contains(item.getType())) {
                inv.setItem(guiSlot, null);
            } else {
                inv.setItem(guiSlot, item.clone());
            }
        }

        for (int i = 0; i < slotsPerPage; i++) {
            int globalIndex = currentPage * slotsPerPage + i;
            if (globalIndex >= maxChestSlots && blockedSlotItem != null) {
                inv.setItem(configSlots.get(i), blockedSlotItem.clone());
            }
        }
    }

    private int calcTotalPages() {
        if (configSlots.isEmpty()) return 1;
        return Math.max(1, (int) Math.ceil((double) clan.getLevel().chest() / configSlots.size()));
    }

    private void registerToActiveChests() {
        ACTIVE_CHESTS.computeIfAbsent(clan.getId(), k -> new HashSet<>()).add(this);
    }

    private void unregisterChest() {
        Set<ChestGui> chests = ACTIVE_CHESTS.get(clan.getId());
        if (chests != null) {
            chests.remove(this);
            if (chests.isEmpty()) ACTIVE_CHESTS.remove(clan.getId());
        }
    }

    private void notifyOtherViewers() {
        Set<ChestGui> chests = ACTIVE_CHESTS.get(clan.getId());
        if (chests == null) return;
        for (ChestGui chest : chests) {
            if (chest != this && chest.currentPage == this.currentPage) {
                Bukkit.getScheduler().runTask(getPlugin(), chest::loadPageFromCloudData);
            }
        }
    }

    private EnumSet<Material> getAllowedMaterials() {
        return getPlugin().getCfg().getAvailableStorageMaterials();
    }

    public TreexClans getPlugin() {
        return (TreexClans) super.getPlugin();
    }
}