package me.jetby.clans.common.gui;

import me.jetby.clans.common.TreexClans;
import me.jetby.libb.command.CommandRegistrar;
import me.jetby.libb.util.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiLoader {
    public static final Map<GuiType, FileConfiguration> REQUIRED_GUIS = new HashMap<>();
    public static final Map<String, FileConfiguration> CUSTOM_GUIS = new HashMap<>();

    private final TreexClans plugin;

    public GuiLoader(TreexClans plugin) {
        this.plugin = plugin;
    }

    /**
     * @return Required gui only
     */
    public static FileConfiguration getGuiConfiguration(GuiType type) {
        return REQUIRED_GUIS.get(type);
    }

    /**
     * @param name Required gui type or Custom gui id
     * @return Custom gui if it's not Required
     */
    public static FileConfiguration getGuiConfiguration(@NotNull String name) {
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
                GuiType guiType;
                try {
                    guiType = GuiType.valueOf(config.getString("listen").toUpperCase());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                loadRequiredGui(guiType, file);
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

            List<String> commands = config.getStringList("command");
            for (String cmd : commands) {
                CommandRegistrar.registerCommand(plugin, cmd, (sender, command, label, args) -> {
                    return true;
                });
            }
            CUSTOM_GUIS.put(menuId, config);
        } catch (Exception e) {
            Logger.error(plugin, "Error trying to load menu: " + e.getMessage());
        }
    }

    private void loadRequiredGui(GuiType type, File file) {
        if (REQUIRED_GUIS.containsKey(type)) {
            Logger.error(plugin, "A duplicate of " + type + " was skipped");
            return;
        }
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            REQUIRED_GUIS.put(type, config);
        } catch (Exception e) {
            Logger.error(plugin, "Error trying to load menu: " + e.getMessage());
        }
    }

}
