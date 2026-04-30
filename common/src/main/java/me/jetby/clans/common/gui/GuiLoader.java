package me.jetby.clans.common.gui;

import me.jetby.clans.common.TreexClans;
import me.jetby.libb.command.CommandRegistrar;
import me.jetby.libb.util.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiLoader {
    public static final Map<String, FileConfiguration> ALL_GUIS = new HashMap<>();

    private final TreexClans plugin;

    public GuiLoader(TreexClans plugin) {
        this.plugin = plugin;
    }

    private void loadFilesRecursive(File folder) {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                loadFilesRecursive(file);
                continue;
            }

            if (!file.getName().endsWith(".yml")) continue;

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String id = config.getString("id", file.getName().replace(".yml", ""));

            loadGui(id, file);
        }
    }

    public void loadGuis() {
        ALL_GUIS.clear();
        File folder = new File(plugin.getDataFolder(), "Menu");


        if (!folder.exists() && folder.mkdirs()) {
            String[] defaults = {
                    "main.yml", "quests.yml", "members.yml", "choose-player-color.yml",
                    "glow-color.yml", "rank-perms.yml", "ranks.yml", "storage.yml", "top-clans.yml", "shop.yml"
            };

            for (String name : defaults) {
                File target = new File(folder, name);
                target.getParentFile().mkdirs();

                if (!target.exists()) {
                    plugin.saveResource("Menu/" + name, false);
                }
            }
        }

        loadFilesRecursive(folder);
    }

    private void loadGui(String menuId, File file) {
        if (ALL_GUIS.containsKey(menuId)) {
            Logger.error(plugin, "A duplicate of " + menuId + " was found");
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
            ALL_GUIS.put(menuId, config);
        } catch (Exception e) {
            Logger.error(plugin, "Error trying to load menu: " + e.getMessage());
        }
    }

}
