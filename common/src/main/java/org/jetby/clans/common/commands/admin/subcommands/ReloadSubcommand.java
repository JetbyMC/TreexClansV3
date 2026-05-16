package org.jetby.clans.common.commands.admin.subcommands;

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

            plugin.getStorage().save();

            Config cfg = new Config();
            cfg.load();
            plugin.setCfg(cfg);

            GuiLoader guiLoader = new GuiLoader(plugin);
            guiLoader.load();

            plugin.setGuiLoader(guiLoader);
//            todo why you do that?
//            if (plugin.getClanCommand() != null) {
//                ClanCommand cmd = new ClanCommand(plugin);
//                plugin.getClanCommand().setExecutor(cmd);
//                plugin.getClanCommand().setTabCompleter(cmd);
//            }

            plugin.getAddonManager().disableAddons();
            ((AddonManagerImpl) plugin.getAddonManager()).loadAddons();

            plugin.getStorage().load();

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
