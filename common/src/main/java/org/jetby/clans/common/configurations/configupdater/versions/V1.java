package org.jetby.clans.common.configurations.configupdater.versions;

import org.jetby.clans.common.configurations.configupdater.Updater;
import org.jetby.clans.common.tools.FileLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class V1 implements Updater {

    private final File file = FileLoader.getFile("config.yml");
    private final FileConfiguration configuration;

    public V1() {
        this.configuration = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void load() {
        try {

            ConfigurationSection ranks = configuration.getConfigurationSection("ranks");
            for (String key : ranks.getKeys(false)) {
                configuration.set("ranks."+key+".permissions.setslogan", false);
                configuration.set("ranks."+key+".permissions.setprefix", false);
            }
            configuration.set("config-version", 2);

            configuration.set("prefix.min-clan-prefix-length", 3);
            configuration.set("prefix.max-clan-prefix-length", 16);
            configuration.set("prefix.regex", "^[A-Za-z0-9]+$");

            configuration.set("prefix.placeholder.has_prefix", "&7[&6{prefix}&7]");
            configuration.set("prefix.placeholder.no_prefix", "&8[&e{tag}&8]");
            configuration.set("prefix.placeholder.no_clan", "");

            configuration.set("tag-placeholder.has_clan", "&8[&e{tag}&8]");
            configuration.set("tag-placeholder.no_clan", "");

            configuration.save(file);
        } catch (IOException ignored) {}
    }

    @Override
    public int version() {
        return 1;
    }
}
