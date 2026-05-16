package org.jetby.clans.common.configurations;

import lombok.Getter;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.tools.FileLoader;
import org.jetby.libb.action.ActionContext;
import org.jetby.libb.action.ActionExecute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;


public class MessagesConfiguration {

    @Getter
    private final FileConfiguration config = FileLoader.getFileConfiguration("messages.yml");

    private final TreexClans plugin;

    public MessagesConfiguration(TreexClans plugin) {
        this.plugin = plugin;
    }

    public Action of(Player player, String path) {
        return new Action(ActionContext.of(player, plugin)
                .replace("{prefix}", config.getString("prefix", "")), path);
    }

    public class Action {
        private ActionContext ctx;
        private final String path;

        public Action(ActionContext ctx, String path) {
            this.ctx = ctx;
            this.path = path;
        }
        public Action replace(String target, String replacement) {
            ctx = ctx.replace(target, replacement);
            return this;
        }

        public Action with(Clan clan) {
            ctx = ctx.with(clan);
            return this;
        }
        public void run() {
            ActionExecute.run(ctx, getMessageList(path));
        }
    }

    public String getCleanMessage(String path) {
        return config.getString(path);
    }

    public List<String> getMessageList(String path) {
        return config.getStringList(path);
    }

}
