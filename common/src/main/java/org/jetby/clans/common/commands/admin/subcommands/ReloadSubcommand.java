package org.jetby.clans.common.commands.admin.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetby.clans.api.TreexClansAPI;
import org.jetby.clans.api.addons.commands.CommandService;
import org.jetby.clans.api.command.Subcommand;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.addon.AddonManagerImpl;
import org.jetby.clans.common.configurations.Config;
import org.jetby.clans.common.gui.GuiLoader;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReloadSubcommand implements Subcommand {
    private final TreexClans plugin;

    public ReloadSubcommand(TreexClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String sub,  @NotNull String[] args) {

        try {
            long start = System.currentTimeMillis();

            if (plugin.getAddonManager() != null) {
                plugin.getAddonManager().disableAddons();
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.getOpenInventory().close();
            }
            plugin.getServer().getServicesManager().unregister(TreexClansAPI.class);

            plugin.loadApi();
            plugin.loadConfigurations();
            Config cfg = new Config();
            cfg.load();
            plugin.setCfg(cfg);

            sender.sendMessage("Reloaded by " + (System.currentTimeMillis() - start) + " ms");
        } catch (Exception e) {
            sender.sendMessage(e.getMessage());
        }
        return true;

    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }

    @Override
    public CommandService.CommandType type() {
        return CommandService.CommandType.ADMIN;
    }
}
