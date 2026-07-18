package org.jetby.clans.common.addon.service;

import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetby.clans.api.addons.AddonManager;
import org.jetby.clans.api.addons.annotations.ClanAddon;
import org.jetby.clans.api.addons.commands.CommandService;
import org.jetby.clans.api.addons.configuration.ServiceConfiguration;
import org.jetby.clans.api.addons.listener.EventRegistrar;
import org.jetby.clans.api.addons.service.ServiceManager;
import org.jetby.clans.api.gui.GuiFactory;
import org.jetby.clans.api.service.ClanManager;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.addon.configuration.ServiceConfigurationImpl;

import java.io.File;

@Getter
public class ServiceManagerImpl implements ServiceManager {

    private final JavaPlugin plugin;
    private final File dataFolder;

    private final Economy economy;
    private final ClanManager clanManager;
    private final CommandService commandService;
    private final GuiFactory guiFactory;

    private final EventRegistrar eventRegistrar;
    private final AddonManager addonManager;
    private final ServiceConfiguration serviceConfiguration;
    private final ClanAddon addon;


    public ServiceManagerImpl(AddonManager addonManager, File dataFolder, TreexClans plugin, ClanAddon addon, ClassLoader addonClassLoader) {
        this.plugin = plugin;
        this.dataFolder = new File(dataFolder, addon.id());
        if (!this.dataFolder.exists()) this.dataFolder.mkdirs();

        this.economy = plugin.getEconomy();
        this.clanManager = plugin.getClanManager();
        this.commandService = plugin.getCommandService();
        this.guiFactory = plugin.getGuiFactory();

        this.eventRegistrar = plugin.getEventRegistrar();
        this.addonManager = addonManager;
        this.serviceConfiguration = new ServiceConfigurationImpl(this, addonClassLoader);
        this.addon = addon;
    }
}
