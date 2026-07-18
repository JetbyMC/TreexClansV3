package org.jetby.clans.common.configurations;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetby.clans.common.commands.clan.ClanSubcommand;
import org.jetby.clans.common.tools.FileLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class CommandsConfiguration {

    @Getter(AccessLevel.NONE)
    private final FileConfiguration configuration;

    public CommandsConfiguration() {
        this.configuration = FileLoader.getFileConfiguration("commands.yml");
    }

    private List<String> commands;
    public static final Map<ClanSubcommand, List<String>> SUBCOMMAND_ALIASES = new HashMap<>();

    public void load() {
        this.commands = configuration.getStringList("clan");


        ConfigurationSection argsSection = configuration.getConfigurationSection("args");
        if (argsSection == null) {
            throw new RuntimeException("Section 'args' is missing");
        }
        for (String key : argsSection.getKeys(false)) {
            try {
                ClanSubcommand arg = ClanSubcommand.valueOf(key.toUpperCase());
                List<String> commands = argsSection.getStringList(key);
                SUBCOMMAND_ALIASES.put(arg, commands);

            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static List<String> getAliases(ClanSubcommand sub) {
        return SUBCOMMAND_ALIASES.get(sub);
    }
}
