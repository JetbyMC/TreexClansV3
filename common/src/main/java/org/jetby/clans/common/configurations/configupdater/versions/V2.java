package org.jetby.clans.common.configurations.configupdater.versions;

import org.jetby.clans.common.configurations.configupdater.Updater;
import org.jetby.clans.common.tools.FileLoader;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class V2 implements Updater {
    private final File file = FileLoader.getFile("config.yml");
    private final FileConfiguration configuration;

    public V2() {
        this.configuration = YamlConfiguration.loadConfiguration(file);
    }
    @Override
    public void load() {
        try {
            configuration.set("config-version", 3);
            configuration.set("prefix.length-ignored-symbols", List.of("&", "#"));
            configuration.save(file);
        } catch (IOException ignored) {
        }
    }

    @Override
    public int version() {
        return 2;
    }
}
