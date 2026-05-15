package me.jetby.clans.common.addon.configuration;

import me.jetby.clans.api.addons.configuration.ServiceConfiguration;
import me.jetby.clans.common.addon.service.ServiceManagerImpl;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

public class ServiceConfigurationImpl implements ServiceConfiguration {

    private final File dataFolder;
    private final JavaPlugin javaPlugin;
    private final ClassLoader addonClassLoader;
    private FileConfiguration config;

    public ServiceConfigurationImpl(ServiceManagerImpl serviceManager, ClassLoader addonClassLoader) {
        this.dataFolder = serviceManager.getDataFolder();
        this.javaPlugin = serviceManager.getPlugin();
        this.addonClassLoader = addonClassLoader;
    }

    private void extractResource(String child, File file) {
        try {
            URL resourceUrl = ((URLClassLoader) addonClassLoader).findResource(child);
            if (resourceUrl != null) {
                file.getParentFile().mkdirs();
                try (InputStream in = resourceUrl.openStream()) {
                    Files.copy(in, file.toPath());
                }
            } else {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException e) {
            javaPlugin.getLogger().severe("Failed to create config file: " + child);
            e.printStackTrace();
        }
    }

    public FileConfiguration getFileConfiguration(String child) {
        File file = new File(dataFolder, child);
        if (!file.exists()) extractResource(child, file);
        return YamlConfiguration.loadConfiguration(file);
    }

    public File getFile(String child) {
        File file = new File(dataFolder, child);
        if (!file.exists()) extractResource(child, file);
        return file;
    }

    public FileConfiguration getConfig() {
        return config != null ? config : getFileConfiguration("config.yml");
    }

    public void saveDefaultConfig() {
        File configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) extractResource("config.yml", configFile);
        this.config = YamlConfiguration.loadConfiguration(configFile);
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