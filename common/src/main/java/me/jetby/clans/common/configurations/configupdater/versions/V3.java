package me.jetby.clans.common.configurations.configupdater.versions;

import me.jetby.clans.common.configurations.configupdater.Updater;
import me.jetby.clans.common.tools.FileLoader;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class V3 implements Updater {
    private final File file = FileLoader.getFile("config.yml");
    private final FileConfiguration configuration;

    public V3() {
        this.configuration = YamlConfiguration.loadConfiguration(file);
    }
    @Override
    public void load() {
        try {
            configuration.set("config-version", 4);

            configuration.set("clan-storage.filter.type", "BLACKLIST");
            configuration.set("clan-storage.filter.materials", List.of("BEDROCK", "BARRIER"));

            configuration.save(file);
        } catch (IOException ignored) {
        }
    }

    @Override
    public int version() {
        return 3;
    }
}
