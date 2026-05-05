package me.jetby.clans.common.configurations;

import lombok.Getter;
import me.jetby.clans.api.service.clan.Clan;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.tools.FileLoader;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionExecute;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;


public class Messages {

    @Getter
    private final FileConfiguration config = FileLoader.getFileConfiguration("messages.yml");

    private final TreexClans plugin;

    public Messages(TreexClans plugin) {
        this.plugin = plugin;
    }

    public void sendActions(Player player, Clan clan, String path) {

        String prefix = config.getString("prefix", "");

        ActionExecute.run(ActionContext.of(player, plugin)
                        .replace("{prefix}", prefix)
                        .with(clan),
                getMessageList(path));
    }

    public void sendActions(Player player, Clan clan, String path, ReplaceString... replaceStrings) {

        String prefix = config.getString("prefix", "");
        List<String> actions = getMessageList(path).stream()
                .map(str -> {
                    for (ReplaceString replace : replaceStrings) {
                        str = str.replace(replace.target(), replace.replacement());
                    }
                    return str;
                })
                .toList();


        if (clan == null) {
            ActionExecute.run(ActionContext.of(player, plugin).replace("{prefix}", prefix), actions);
        } else {
            ActionExecute.run(ActionContext.of(player, plugin).replace("{prefix}", prefix).with(clan), actions);

        }
    }

    public Component getMessage(String path) {
        return Config.CONFIG_COLORIZER.deserialize(config.getString(path));
    }

    public String getCleanMessage(String path) {
        return config.getString(path);
    }

    public List<String> getMessageList(String path) {
        return config.getStringList(path);
    }

    public record ReplaceString(String target, String replacement) {
    }
}
