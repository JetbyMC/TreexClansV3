package me.jetby.clans.common.gui.core;

import me.jetby.clans.api.gui.Gui;
import me.jetby.clans.api.service.clan.member.Member;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.functions.quests.Quest;
import me.jetby.clans.common.functions.quests.QuestProgressType;
import me.jetby.clans.api.gui.GuiContext;
import me.jetby.libb.gui.item.ItemWrapper;
import me.jetby.libb.gui.parser.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.jetby.clans.common.TreexClans.NAMESPACED_KEY;

public class QuestsGui extends Gui {


    public QuestsGui(@NotNull GuiContext ctx) {
        super(ctx);

        setupQuestsPagination();

        addClickHandler("type", event -> {
            String type = event.getSection().getString("type");
            if (type == null) return;
            switch (type.toLowerCase()) {
                case "next_page" -> {
                    event.setCancelled(true);
                    nextPage();
                }
                case "prev_page" -> {
                    event.setCancelled(true);
                    prevPage();
                }
            }
        });
    }

    private void setupQuestsPagination() {
        List<Item> questItems = getBySectionOption("type").stream()
                .filter(b -> {
                    String type = b.section().getString("type", "");
                    return type.equals("all_quests") || type.startsWith("category-");
                })
                .toList();

        if (questItems.isEmpty()) return;

        Item template = questItems.get(0);
        String templateType = template.section().getString("type", "");

        List<Integer> questSlots = questItems.stream()
                .flatMap(i -> i.slots().stream())
                .distinct()
                .toList();

        contentSlots(questSlots.toArray(new Integer[0]));


        List<Quest> questsList = buildQuestList(templateType);
        if (questsList.isEmpty()) return;

        Member member = getClan().getMember(getViewer().getUniqueId());

        for (Quest quest : questsList) {
            int progress = getPlugin().getQuestManager().getProgress(member, quest);

            setReplace("%status%", status(member, quest));
            setReplace("%quest_name%", quest.name());
            setReplace("%quest_description%", quest.description());
            setReplace("%quest_progress%", String.valueOf(progress));
            setReplace("%quest_target%", String.valueOf(quest.target()));
            setReplace("%quest_progress_type%", progressType(quest));

            ItemStack itemStack = template.itemStack().clone();
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "menu_item");
            itemStack.setItemMeta(itemMeta);

            ItemWrapper wrapper = new ItemWrapper(itemStack);
            wrapper.onClick(event -> {
                event.setCancelled(true);
            });
            wrapper.serializer(defaultSerializer);
            wrapper.displayName(applyPlaceholders(template.section().getString("display_name", "")));

            List<String> lore = new ArrayList<>(template.section().getStringList("lore"));
            lore.addAll(quest.rewardsDescription());
            wrapper.setLore(lore.stream()
                    .map(this::applyPlaceholders)
                    .collect(Collectors.toList()));

            if (template.customModelData() != 0)
                wrapper.customModelData(template.customModelData());
            if (template.enchanted())
                wrapper.enchanted(true);

            addItem(wrapper);
        }
    }

    private List<Quest> buildQuestList(String templateType) {
        List<Quest> list = new ArrayList<>();
        if ("all_quests".equals(templateType)) {
            list.addAll(getPlugin().getQuestsLoader().getQuests().values());
        } else if (templateType.startsWith("category-")) {
            String catId = templateType.substring(9);
            Set<Quest> cat = getPlugin().getQuestsLoader().getCategories().get(catId);
            if (cat != null) list.addAll(cat);
        }
        return list;
    }

    @Override
    public boolean cancelRegistration(@NotNull Item item) {
        if (item.type()!=null && item.section() != null) {
            String type = item.section().getString("type", "");
            return type.equals("all_quests") || type.startsWith("category-");
        }
        return false;
    }

    private String status(Member member, Quest quest) {
        return getPlugin().getQuestManager().isQuestCompleted(member, quest)
                ? getPlugin().getMessages().getCleanMessage("quest-status-completed")
                : getPlugin().getMessages().getCleanMessage("quest-status-uncompleted");
    }

    private String progressType(Quest quest) {
        return quest.progressType().equals(QuestProgressType.INDIVIDUAL)
                ? getPlugin().getMessages().getCleanMessage("quest-progress-type-individual")
                : getPlugin().getMessages().getCleanMessage("quest-progress-type-global");
    }

    public TreexClans getPlugin() {
        return (TreexClans) super.getPlugin();
    }
}