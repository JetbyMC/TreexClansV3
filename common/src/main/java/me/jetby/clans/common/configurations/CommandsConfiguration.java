package me.jetby.clans.common.configurations;

import lombok.AccessLevel;
import lombok.Getter;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.commands.clan.ClanCommandArgs;
import me.jetby.clans.common.tools.FileLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class CommandsConfiguration {

    @Getter(AccessLevel.NONE)
    private final FileConfiguration configuration;

    public CommandsConfiguration(TreexClans plugin) {
        this.configuration = FileLoader.getFileConfiguration("commands.yml");
    }

    private List<String> commands;
    public static final Map<ClanCommandArgs, List<String>> SUBCOMMAND_ALIASES = new HashMap<>();

    public void load() {
        this.commands = configuration.getStringList("clan");


        ConfigurationSection argsSection = configuration.getConfigurationSection("args");
        if (argsSection==null) {
            throw new RuntimeException("Section 'args' is missing");
        }
        for (String key : argsSection.getKeys(false)) {
            try {
                ClanCommandArgs arg = ClanCommandArgs.valueOf(key.toUpperCase());
                List<String> commands = argsSection.getStringList(key);
                SUBCOMMAND_ALIASES.put(arg, commands);

            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static List<String> getAliases(ClanCommandArgs sub) {
        return SUBCOMMAND_ALIASES.get(sub);
    }
}
