package me.jetby.clans.common.gui;

import me.jetby.clans.api.gui.ExtendedGui;
import me.jetby.clans.api.gui.ListenType;
import me.jetby.clans.common.TreexClans;
import me.jetby.libb.action.record.ActionBlock;
import me.jetby.libb.action.record.Expression;
import me.jetby.libb.command.CommandRegistrar;
import me.jetby.libb.gui.parser.Item;
import me.jetby.libb.gui.parser.ParseUtil;
import me.jetby.libb.util.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiLoader {
    public static final Map<ListenType, ExtendedGui> REQUIRED_GUIS = new HashMap<>();
    public static final Map<String, ExtendedGui> CUSTOM_GUIS = new HashMap<>();

    private final TreexClans plugin;

    public GuiLoader(TreexClans plugin) {
        this.plugin = plugin;
    }

    /**
     * @return Required gui only
     */
    public static ExtendedGui getGuiConfiguration(ListenType type) {
        return REQUIRED_GUIS.get(type);
    }

    /**
     * @param name Required gui type or Custom gui id
     * @return Custom gui if it's not Required
     */
    public static ExtendedGui getGuiConfiguration(@NotNull String name) {
        return CUSTOM_GUIS.get(name);
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

            if (isRequired) {
                ListenType listenType;
                try {
                    listenType = ListenType.valueOf(config.getString("listen").toUpperCase());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                loadRequiredGui(listenType, file);
            } else {
                loadCustomGui(id, file);

            }
        }
    }

    public void load() {
        createRequiredGuis();
        createCustomGuis();
    }

    public void createRequiredGuis() {
        REQUIRED_GUIS.clear();
        File folder = new File(plugin.getDataFolder(), "Menu/models");
        if (!folder.exists() && folder.mkdirs()) {
            String[] defaults = {
                    "quests.yml",
                    "members.yml",
                    "choose-player-color.yml",
                    "glow-color.yml",
                    "rank-perms.yml",
                    "ranks.yml",
                    "storage.yml",
                    "top-clans.yml"
            };

            for (String name : defaults) {
                File target = new File(folder, name);
                target.getParentFile().mkdirs();

                if (!target.exists()) {
                    plugin.saveResource("Menu/models/" + name, false);
                }
            }
        }
        loadFilesRecursive(folder, true);
    }

    public void createCustomGuis() {
        CUSTOM_GUIS.clear();
        File folder = new File(plugin.getDataFolder(), "Menu/optional");


        if (!folder.exists() && folder.mkdirs()) {
            String[] defaults = {
                    "main.yml", "shop.yml"
            };

            for (String name : defaults) {
                File target = new File(folder, name);
                target.getParentFile().mkdirs();

                if (!target.exists()) {
                    plugin.saveResource("Menu/optional/" + name, false);
                }
            }
        }

        loadFilesRecursive(folder, false);
    }

    private void loadCustomGui(String menuId, File file) {
        if (CUSTOM_GUIS.containsKey(menuId)) {
            Logger.error(plugin, "A duplicate of " + menuId + " was skipped");
            return;
        }
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            String id = config.getString("id");
            String title = config.getString("title");
            int size = config.getInt("size");
            List<String> command = config.getStringList("command");
            List<Expression> preOpenExpressions = ParseUtil.getExpressions(config.getStringList("pre_open"));
            ActionBlock onOpen = ParseUtil.getActionBlock(config, "on_open");
            ActionBlock onClose = ParseUtil.getActionBlock(config, "on_close");

            String listen = config.getString("listen", "default");
            ListenType listenType;
            try {
                listenType = ListenType.valueOf(listen.toUpperCase());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            List<String> args = config.getStringList("open_args");

            List<Item> items = ParseUtil.getItems(config);


            ExtendedGui gui = new ExtendedGui(
                    id, title, size, command,
                    preOpenExpressions, onOpen, onClose,
                    items,
                    listenType,
                    args
            );

            List<String> commands = config.getStringList("command");
            for (String cmd : commands) {
                CommandRegistrar.registerCommand(plugin, cmd, (sender, command1, label, args1) -> {
                    return true;
                });
            }
            CUSTOM_GUIS.put(menuId, gui);
        } catch (Exception e) {
            Logger.error(plugin, "Error trying to load menu: " + e.getMessage());
        }
    }

    private void loadRequiredGui(ListenType type, File file) {
        if (REQUIRED_GUIS.containsKey(type)) {
            Logger.error(plugin, "A duplicate of " + type + " was skipped");
            return;
        }
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            String id = config.getString("id");
            String title = config.getString("title");
            int size = config.getInt("size");
            List<String> command = config.getStringList("command");
            List<Expression> preOpenExpressions = ParseUtil.getExpressions(config.getStringList("pre_open"));
            ActionBlock onOpen = ParseUtil.getActionBlock(config, "on_open");
            ActionBlock onClose = ParseUtil.getActionBlock(config, "on_close");

            String listen = config.getString("listen", "default");
            ListenType listenType;
            try {
                listenType = ListenType.valueOf(listen.toUpperCase());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            List<String> args = config.getStringList("open_args");

            List<Item> items = ParseUtil.getItems(config);


            ExtendedGui gui = new ExtendedGui(
                    id, title, size, command,
                    preOpenExpressions, onOpen, onClose,
                    items,
                    listenType,
                    args
            );

            REQUIRED_GUIS.put(type, gui);
        } catch (Exception e) {
            Logger.error(plugin, "Error trying to load menu: " + e.getMessage());
        }
    }

}
