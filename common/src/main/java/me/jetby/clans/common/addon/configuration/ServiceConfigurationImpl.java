package me.jetby.clans.common.addon.configuration;

import me.jetby.clans.api.addons.configuration.ServiceConfiguration;
import me.jetby.clans.common.addon.service.ServiceManagerImpl;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ServiceConfigurationImpl implements ServiceConfiguration {

    private final File dataFolder;
    private final JavaPlugin javaPlugin;
    private FileConfiguration config;

    public ServiceConfigurationImpl(ServiceManagerImpl serviceManager) {
        this.dataFolder = serviceManager.getDataFolder();
        this.javaPlugin = serviceManager.getPlugin();
    }

    public FileConfiguration getFileConfiguration(String child) {
        File file = new File(dataFolder, child);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                javaPlugin.getLogger().severe("Failed to create config file: " + child);
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public File getFile(String child) {
        File file = new File(dataFolder, child);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                javaPlugin.getLogger().severe("Failed to create config file: " + child);
                e.printStackTrace();
            }
        }
        return file;
    }

    public FileConfiguration getConfig() {
        return config!=null ? config : getFileConfiguration("config.yml");
    }

    public void saveDefaultConfig() {
        this.config = getConfig();
    }
    public void saveConfig() {
        File configFile = new File(dataFolder, "config.yml");

        try {
            config.save(configFile);
        } catch (IOException e) {
            javaPlugin.getLogger().severe("Failed to save config file: config.yml");
            e.printStackTrace();
        }
    }
}
