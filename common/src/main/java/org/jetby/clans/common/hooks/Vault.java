package org.jetby.clans.common.hooks;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import static org.jetby.clans.common.TreexClans.LOGGER;


public class Vault {

    public Economy load() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return null;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            LOGGER.error("Vault economy plugin was not found!");
            return null;
        }
        return rsp.getProvider();
    }
}
