package org.jetby.clans.common.gui;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetby.clans.api.gui.ClanGuiData;
import org.jetby.clans.api.gui.GuiModel;
import org.jetby.clans.common.TreexClans;
import org.jetby.libb.action.record.ActionBlock;
import org.jetby.libb.action.record.Expression;
import org.jetby.libb.command.CommandRegistrar;
import org.jetby.libb.gui.parser.Item;
import org.jetby.libb.gui.parser.ParseUtil;
import org.jetby.libb.util.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiLoader {
    public static final Map<GuiModel, ClanGuiData> REQUIRED_GUIS = new HashMap<>();
    public static final Map<String, ClanGuiData> CUSTOM_GUIS = new HashMap<>();
    public static final Map<String, ClanGuiData> API_GUIS = new HashMap<>();

    private final TreexClans plugin;

    public GuiLoader(TreexClans plugin) {
        this.plugin = plugin;
    }

    /**
     * @return Required gui only
     */
    public static ClanGuiData getGuiConfiguration(GuiModel type) {
        return REQUIRED_GUIS.get(type);
    }

    /**
     * @param id GUI ID — searches across all maps: custom, api, required.
     */
    public static ClanGuiData getGuiConfiguration(@NotNull String id) {
        ClanGuiData gui = CUSTOM_GUIS.get(id);
        if (gui != null) return gui;
        gui = API_GUIS.get(id);
        if (gui != null) return gui;
        return REQUIRED_GUIS.values().stream()
                .filter(g -> id.equals(g.getId()))
                .findFirst().orElse(null);
    }

    public void load() {
        createRequiredGuis();
        createCustomGuis();
    }

    public void createRequiredGuis() {
        REQUIRED_GUIS.clear();
        File folder = new File(plugin.getDataFolder(), "Menu/models");
        if (!folder.exists() && folder.mkdirs()) {
            for (String name : new String[]{"members.yml", "rank-perms.yml", "ranks.yml", "chest.yml"}) {
                saveDefault("Menu/models/" + name, folder, name);
            }
            saveDefault("Menu/models/README.md", folder, "README.md");
        }
        loadFilesRecursive(folder, true);
    }

    public void createCustomGuis() {
        CUSTOM_GUIS.clear();
        File folder = new File(plugin.getDataFolder(), "Menu/custom");
        if (!folder.exists() && folder.mkdirs()) {
            for (String name : new String[]{"main.yml", "shop.yml"}) {
                saveDefault("Menu/custom/" + name, folder, name);
            }
            saveDefault("Menu/custom/README.md", folder, "README.md");
        }
        loadFilesRecursive(folder, false);
    }

    private void saveDefault(String resourcePath, File folder, String name) {
        File target = new File(folder, name);
        target.getParentFile().mkdirs();
        if (!target.exists()) plugin.saveResource(resourcePath, false);
    }

    private void loadFilesRecursive(File folder, boolean isRequired) {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                loadFilesRecursive(file, isRequired);
                continue;
            }
            if (!file.getName().endsWith(".yml")) continue;

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String id = config.getString("id", file.getName().replace(".yml", ""));
            String model = config.getString("model", "default");

            if (isRequired) {
                GuiModel guiModel = parseBuiltinModel(model);
                if (guiModel == null || guiModel == GuiModel.DEFAULT) continue;
                if (REQUIRED_GUIS.containsKey(guiModel)) {
                    Logger.error(plugin, "A duplicate of " + guiModel + " was skipped");
                    continue;
                }
                ClanGuiData gui = parseGui(config, model);
                if (gui != null) REQUIRED_GUIS.put(guiModel, gui);
            } else if (model.contains(":")) {
                if (API_GUIS.containsKey(id)) {
                    Logger.error(plugin, "A duplicate of " + id + " was skipped");
                    continue;
                }
                GuiFactoryImpl factory = (GuiFactoryImpl) plugin.getGuiFactory();
                if (!factory.getCustomTypes().containsKey(model.split(":")[1].toUpperCase())) {
                    Logger.error(plugin, "Unknown renderer '" + model + "' for gui '" + id + "', is the addon loaded?");
                    continue;
                }
                ClanGuiData gui = parseGui(config, model);
                if (gui != null) API_GUIS.put(id, gui);
            } else {
                if (CUSTOM_GUIS.containsKey(id)) {
                    Logger.error(plugin, "A duplicate of " + id + " was skipped");
                    continue;
                }
                ClanGuiData gui = parseGui(config, model);
                if (gui == null) continue;
                registerCommands(config);
                CUSTOM_GUIS.put(id, gui);
            }
        }
    }

    @Nullable
    private GuiModel parseBuiltinModel(String model) {
        try {
            return GuiModel.valueOf(model.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private ClanGuiData parseGui(FileConfiguration config, String renderer) {
        try {
            String id = config.getString("id");
            String title = config.getString("title");
            int size = config.getInt("size");
            List<String> command = config.getStringList("command");
            List<Expression> preOpenExpressions = ParseUtil.getExpressions(config.getStringList("pre_open"));
            ActionBlock onOpen = ParseUtil.getActionBlock(config, "on_open");
            ActionBlock onClose = ParseUtil.getActionBlock(config, "on_close");
            List<String> args = config.getStringList("open_args");
            List<Item> items = ParseUtil.getItems(config);
            return new ClanGuiData(id, title, size, command, preOpenExpressions, onOpen, onClose, items, renderer, args);
        } catch (Exception e) {
            Logger.error(plugin, "Error trying to load menu: " + e.getMessage());
            return null;
        }
    }

    private void registerCommands(FileConfiguration config) {
        for (String cmd : config.getStringList("command")) {
            CommandRegistrar.registerCommand(plugin, cmd, (sender, command, label, args) -> true);
        }
    }
}