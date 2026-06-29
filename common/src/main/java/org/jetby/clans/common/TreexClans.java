package org.jetby.clans.common;

import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetby.clans.api.TreexClansAPI;
import org.jetby.clans.api.addons.AddonManager;
import org.jetby.clans.api.addons.commands.CommandService;
import org.jetby.clans.api.addons.listener.EventRegistrar;
import org.jetby.clans.api.gui.Gui;
import org.jetby.clans.api.gui.GuiFactory;
import org.jetby.clans.api.service.ClanManager;
import org.jetby.clans.api.storage.Storage;
import org.jetby.clans.common.actions.Actions;
import org.jetby.clans.common.addon.AddonManagerImpl;
import org.jetby.clans.common.clan.service.ClanManagerImpl;
import org.jetby.clans.common.commands.CommandServiceImpl;
import org.jetby.clans.common.commands.admin.AdminCommand;
import org.jetby.clans.common.commands.clan.ClanCommand;
import org.jetby.clans.common.configurations.CommandsConfiguration;
import org.jetby.clans.common.configurations.Config;
import org.jetby.clans.common.configurations.MessagesConfiguration;
import org.jetby.clans.common.configurations.ModulesConfiguration;
import org.jetby.clans.common.configurations.configupdater.UpdateConfig;
import org.jetby.clans.common.gui.GuiFactoryImpl;
import org.jetby.clans.common.gui.GuiLoader;
import org.jetby.clans.common.hooks.ClanPlaceholder;
import org.jetby.clans.common.hooks.Vault;
import org.jetby.clans.common.listener.EventRegistryImpl;
import org.jetby.clans.common.listeners.ClanListeners;
import org.jetby.clans.common.storage.YamlStorageCore;
import org.jetby.clans.common.storage.trash.manager.DatabaseManager;
import org.jetby.clans.common.storage.trash.simple.ClanTable;
import org.jetby.clans.common.tools.FormatTime;
import org.jetby.clans.common.tools.Logger;
import org.jetby.clans.common.tools.Speedometer;
import org.jetby.libb.action.ActionRegistry;
import org.jetby.libb.util.Metrics;

@Getter
public final class TreexClans extends JavaPlugin implements TreexClansAPI {

    private static TreexClans INSTANCE;
    private CommandService commandService;
    private GuiFactory guiFactory;

    public static TreexClans getInstance() {
        return INSTANCE;
    }

    private JavaPlugin plugin;
    @NotNull
    private Economy economy;
    @Setter
    private Config cfg;
    private FormatTime formatTime;
    private ClanManager clanManager;
    private Storage storage;

    public static Logger LOGGER;
    @Setter
    private GuiLoader guiLoader;

    private ClanPlaceholder clanPlaceholder;

    private ModulesConfiguration modules;

    private EventRegistrar eventRegistrar;
    private AddonManager addonManager;
    @Getter
    private MessagesConfiguration messages;

    private DatabaseManager db;
    private ClanTable clanTable;

    @Override
    public void onEnable() {
        this.plugin = this;
        INSTANCE = this;
        LOGGER = new Logger(this);

        new UpdateConfig(getConfig().getInt("config-version", 1));

        cfg = new Config();
        cfg.load();

        storage = new YamlStorageCore(this);
        storage.initialize();

        LOGGER.info("&6┏ Loading hooks:");
        Speedometer.start();
        loadHooks();
        LOGGER.success("┗ Hooks loaded (" + Speedometer.result() + "ms)");
        LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        LOGGER.info("&6┏ Loading API:");
        Speedometer.start();
        loadApi();
        new Actions().registerCustomActions();
        LOGGER.success("┗ API loaded (" + Speedometer.result() + "ms)");
        LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        LOGGER.info("&6┏ Loading configurations:");
        Speedometer.start();
        loadConfigurations();
        LOGGER.success("Configuration loaded (" + Speedometer.result() + "ms)");
        LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        LOGGER.info("&6┏ Loading commands:");
        Speedometer.start();
        loadCommands();
        LOGGER.success("┗ Commands created (" + Speedometer.result() + "ms)");
        LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        LOGGER.info("&6Last details");
        getServer().getPluginManager().registerEvents(new ClanListeners(this), this);
        new Metrics(this, 27749);

        LOGGER.success("⚡ TreexClans is ready");
    }

    public void loadHooks() {
        clanPlaceholder = new ClanPlaceholder(this);
        if (clanPlaceholder.isPapi()) {
            clanPlaceholder.register();
            LOGGER.success(" └  ✔  PlaceholderAPI");
        } else {
            LOGGER.warn(" └  ✘  PlaceholderAPI");
        }

        Economy economy = new Vault().load();
        if (economy == null) {
            LOGGER.error(" └  ✘  Vault");
            LOGGER.error("&4&lThis plugin cannot work without Vault plugin!");
            LOGGER.error("&4&lYou can download it here https://www.spigotmc.org/resources/vault.34315/");
        } else {
            this.economy = economy;
            LOGGER.success(" └  ✔  Vault");
        }
    }

    public void loadApi() {
        guiFactory = new GuiFactoryImpl();

        clanManager = new ClanManagerImpl(this);

        eventRegistrar = new EventRegistryImpl();
        this.commandService = new CommandServiceImpl();
        addonManager = new AddonManagerImpl(this);

        getServer().getServicesManager().register(
                TreexClansAPI.class,
                this,
                this,
                ServicePriority.Normal
        );

        ((AddonManagerImpl) addonManager).loadAddons();
    }

    public void loadConfigurations() {
        LOGGER.success(" └  ✔  config.yml");

        messages = new MessagesConfiguration(this);
        LOGGER.success(" └  ✔  messages.yml");
        formatTime = new FormatTime(this);

        modules = new ModulesConfiguration();
        modules.load();
        LOGGER.success(" └  ✔  modules.yml");

        guiLoader = new GuiLoader(this);
        guiLoader.load();
        LOGGER.success(" └  ✔  Menus");
//
//        storage = new YAML(this);
//        storage.load();

        LOGGER.success(" └  ✔  Storage");
    }

    public void loadCommands() {
        PluginCommand xClanCommand = this.getCommand("treexclans");
        if (xClanCommand != null) {
            AdminCommand cmd = new AdminCommand(commandService);
            xClanCommand.setExecutor(cmd);
            xClanCommand.setTabCompleter(cmd);
        }

        CommandsConfiguration commandsConfiguration = new CommandsConfiguration();
        commandsConfiguration.load();
        new ClanCommand(commandsConfiguration, this).register();

    }

    @Override
    public void onDisable() {


//        for (Clan clan : Storage.CLANS.values()) {
//            clanTable.saveClan(clan);
//            for (Member member : clan.getMembersWithLeader()) {
//                clanTable.saveMember(clan.getId(), member);
//            }
//        }
//        db.disconnect();


        ActionRegistry.unregisterAll("treexclans");
        if (addonManager != null) {
            addonManager.disableAddons();
        }
        getServer().getServicesManager().unregister(TreexClansAPI.class);
        if (storage != null) storage.shutdown();
        if (clanPlaceholder != null) {
            if (clanPlaceholder.isPapi()) {
                clanPlaceholder.unregister();
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof Gui) {
                player.closeInventory();
            }
        }
    }
}
