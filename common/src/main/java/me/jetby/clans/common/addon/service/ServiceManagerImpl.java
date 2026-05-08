package me.jetby.clans.common.addon.service;

import lombok.Getter;
import me.jetby.clans.api.addons.AddonManager;
import me.jetby.clans.api.addons.annotations.ClanAddon;
import me.jetby.clans.api.addons.commands.CommandService;
import me.jetby.clans.api.addons.configuration.ServiceConfiguration;
import me.jetby.clans.api.addons.listener.EventRegistrar;
import me.jetby.clans.api.addons.service.ServiceManager;
import me.jetby.clans.api.gui.GuiFactory;
import me.jetby.clans.api.service.ClanManager;
import me.jetby.clans.api.service.leaderboard.LeaderboardService;
import me.jetby.clans.common.TreexClans;
import me.jetby.clans.common.addon.configuration.ServiceConfigurationImpl;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@Getter
public class ServiceManagerImpl implements ServiceManager {

    private final JavaPlugin plugin;
    private final File dataFolder;

    private final Economy economy;
    private final ClanManager clanManager;
    private final LeaderboardService leaderboardService;
    private final CommandService commandService;
    private final GuiFactory guiFactory;

    private final EventRegistrar eventRegistrar;
    private final AddonManager addonManager;
    private final ServiceConfiguration serviceConfiguration;
    private final ClanAddon addon;


    public ServiceManagerImpl(AddonManager addonManager, File dataFolder, TreexClans plugin, ClanAddon addon) {
        this.plugin = plugin;
        this.dataFolder = new File(dataFolder, addon.id());
        if (!dataFolder.exists()) dataFolder.mkdirs();

        this.economy = plugin.getEconomy();
        this.clanManager = plugin.getClanManager();
        this.leaderboardService = plugin.getLeaderboardService();
        this.commandService = plugin.getCommandService();
        this.guiFactory = plugin.getGuiFactory();

        this.eventRegistrar = plugin.getEventRegistrar();
        this.addonManager = addonManager;
        this.serviceConfiguration = new ServiceConfigurationImpl(this);
        this.addon = addon;
    }
}
