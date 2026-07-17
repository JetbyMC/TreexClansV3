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
import org.jetby.clans.common.storage.MySQLStorageImpl;
import org.jetby.clans.common.storage.SQLiteStorageImpl;
import org.jetby.clans.common.storage.StorageCore;
import org.jetby.clans.common.storage.YamlStorageImpl;
import org.jetby.clans.common.tools.FormatTime;
import org.jetby.clans.common.tools.Logger;
import org.jetby.libb.action.ActionRegistry;
import org.jetby.libb.util.LibraryLoader;
import org.jetby.libb.util.Metrics;
import org.jetby.libb.util.VersionUtil;

import java.util.List;

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
    private StorageCore storage;

    public static Logger LOGGER;
    @Setter
    private GuiLoader guiLoader;

    private ClanPlaceholder clanPlaceholder;

    private ModulesConfiguration modules;

    private EventRegistrar eventRegistrar;
    private AddonManager addonManager;
    @Getter
    private MessagesConfiguration messages;

    @Override
    public void onEnable() {
        this.plugin = this;
        INSTANCE = this;
        LOGGER = new Logger(this);

        LOGGER.info("<#1BD9FB>╔════════════════════╗");
        LOGGER.info("<#1BD9FB>║  &b⚡ <#1BD9FB>&lTreexClans &b⚡  <#1BD9FB>║");
        LOGGER.info("<#1BD9FB>╚════════════════════╝");
        LOGGER.info("&b► &fAuthor: <#1BD9FB>&lMrJetby");
        LOGGER.info("&b► &fDiscord: <#1BD9FB>https://dsc.gg/jmdev");
        LOGGER.info("&b► &fVersion: <#1BD9FB>"+getDescription().getVersion());
        LOGGER.info("<#1BD9FB>══════════════════════");

        LibraryLoader.load(this, "hikari", "https://repo.maven.apache.org/maven2/",
                List.of(new LibraryLoader.Dependency("com.zaxxer", "HikariCP",
                        "7.0.2",
                        "com.zaxxer.hikari.HikariConfig"))
        );
        new UpdateConfig(getConfig().getInt("config-version", 1));
        cfg = new Config();
        cfg.load();
        loadHooks();
        storage = switch (cfg.getStorageType().toLowerCase()) {
            case "sqlite" -> new SQLiteStorageImpl();
            case "mysql" -> new MySQLStorageImpl(cfg);
            default -> new YamlStorageImpl();
        };
        storage.initialize();
        new Actions().registerCustomActions();
        loadApi();
        loadConfigurations();
        loadCommands();
        getServer().getPluginManager().registerEvents(new ClanListeners(this), this);
        new VersionUtil(this, getDescription().getVersion(), "https://raw.githubusercontent.com/JetbyMC/TreexClansV3/refs/heads/master/VERSION", "treexclans.admin");
        new Metrics(this, 27749);
        LOGGER.success("<#1BD9FB>▶ TreexClans is ready");
    }

    public void loadHooks() {
        clanPlaceholder = new ClanPlaceholder(this);
        if (clanPlaceholder.isPapi()) {
            LOGGER.info("<#1BD9FB>✓ PlaceholderAPI");
            clanPlaceholder.register();
        } else {
            LOGGER.info("<#1BD9FB>✘ PlaceholderAPI");
        }

        Economy economy = new Vault().load();
        if (economy == null) {
            LOGGER.error("✘ Vault");
            LOGGER.error("&4&lThis plugin cannot work without Vault plugin!");
            LOGGER.error("&4&lYou can download it here https://www.spigotmc.org/resources/vault.34315/");
        } else {
            this.economy = economy;
            LOGGER.info("<#1BD9FB>✓ Vault");
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
        messages = new MessagesConfiguration(this);
        formatTime = new FormatTime(this);
        modules = new ModulesConfiguration();
        modules.load();
        guiLoader = new GuiLoader(this);
        guiLoader.load();
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
