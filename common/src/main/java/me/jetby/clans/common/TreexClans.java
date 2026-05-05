package me.jetby.clans.common;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import lombok.Getter;
import lombok.Setter;
import me.jetby.clans.api.InstanceFactory;
import me.jetby.clans.api.TreexClansAPI;
import me.jetby.clans.api.addons.AddonManager;
import me.jetby.clans.api.addons.commands.CommandService;
import me.jetby.clans.api.addons.listener.EventRegistrar;
import me.jetby.clans.api.service.ClanManager;
import me.jetby.clans.api.service.leaderboard.LeaderboardService;
import me.jetby.clans.common.addon.AddonManagerImpl;
import me.jetby.clans.common.clan.service.ClanManagerImpl;
import me.jetby.clans.common.commands.CommandServiceImpl;
import me.jetby.clans.common.commands.admin.AdminCommand;
import me.jetby.clans.common.commands.clan.ClanCommand;
import me.jetby.clans.common.configurations.Config;
import me.jetby.clans.common.configurations.Messages;
import me.jetby.clans.common.configurations.Modules;
import me.jetby.clans.common.configurations.QuestsLoader;
import me.jetby.clans.common.configurations.configupdater.UpdateConfig;
import me.jetby.clans.common.functions.glow.Glow;
import me.jetby.clans.common.functions.quests.QuestManager;
import me.jetby.clans.common.functions.tops.LeaderboardServiceImpl;
import me.jetby.clans.common.gui.GuiLoader;
import me.jetby.clans.common.hooks.ClanPlaceholder;
import me.jetby.clans.common.hooks.Vault;
import me.jetby.clans.common.listener.EventRegistryImpl;
import me.jetby.clans.common.listeners.ClanListeners;
import me.jetby.clans.common.listeners.QuestsListeners;
import me.jetby.clans.common.storage.Storage;
import me.jetby.clans.common.storage.YAML;
import me.jetby.clans.common.tools.FormatTime;
import me.jetby.clans.common.tools.Logger;
import me.jetby.clans.common.tools.Speedometer;
import me.jetby.clans.common.tools.customactions.Actions;
import me.jetby.libb.action.ActionRegistry;
import me.jetby.libb.util.Metrics;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

@Getter
public final class TreexClans extends JavaPlugin implements TreexClansAPI {

    private static TreexClans INSTANCE;
    private CommandService commandService;
//    private GuiFactory guiFactory;

    public static TreexClans getInstance() {
        return INSTANCE;
    }

    private JavaPlugin plugin;
    @NotNull
    private Economy economy;
    @Setter
    private Config cfg;
    private FormatTime formatTime;
    @Setter
    private Glow glow;
    private ClanManager clanManager;
    private LeaderboardService leaderboardService;
    private Storage storage;

    public static Logger LOGGER;
    public static NamespacedKey NAMESPACED_KEY;
    @Setter
    private GuiLoader guiLoader;

    @Setter
    private QuestsLoader questsLoader;
    private QuestManager questManager;
    private ClanPlaceholder clanPlaceholder;

    private Modules modules;

    private EventRegistrar eventRegistrar;
    private AddonManager addonManager;
    @Getter
    private Messages messages;

    @Override
    public void onLoad() {
        PacketEvents.getAPI().getEventManager().registerListener(
                glow = new Glow(this), PacketListenerPriority.NORMAL);
    }

    @Override
    public void onEnable() {
        this.plugin = this;
        INSTANCE = this;
        LOGGER = new Logger(this);

//        this.guiFactory = new GuiFactoryImpl();
//        InstanceFactory.GUI_FACTORY = guiFactory;
        InstanceFactory.ITEM_KEY = new NamespacedKey("treexclans", "item");
        NAMESPACED_KEY = InstanceFactory.ITEM_KEY;

        //            new TreexAutoDownload(this);
        new Actions().registerCustomActions();


        LOGGER.info("&6┏ Loading hooks:");
        Speedometer.start();
        clanPlaceholder = new ClanPlaceholder(this);
        if (clanPlaceholder.isPapi()) {
            clanPlaceholder.register();
            LOGGER.success(" └  ✔  PlaceholderAPI");
        } else {
            LOGGER.warn(" └  ✘  PlaceholderAPI");
        }

        Economy economy = new Vault().load();
        if (economy==null) {
            LOGGER.error(" └  ✘  Vault");
            LOGGER.error("&4&lThis plugin cannot work without Vault plugin!");
            LOGGER.error("&4&lYou can download it here https://www.spigotmc.org/resources/vault.34315/");
        } else {
            this.economy = economy;
            LOGGER.success(" └  ✔  Vault");
        }
        LOGGER.success("┗ Hooks loaded ("+Speedometer.result()+"ms)");
        LOGGER.info("--------------");
        LOGGER.info("&6┏ Loading configurations:");
        Speedometer.start();
        new UpdateConfig(getConfig().getInt("config-version", 1));
        cfg = new Config();
        cfg.load();
        LOGGER.success(" └  ✔  config.yml");

        messages = new Messages(this);
        LOGGER.success(" └  ✔  messages.yml");

        formatTime = new FormatTime(this);

        modules = new Modules();
        modules.load();
        LOGGER.success(" └  ✔  modules.yml");

        guiLoader = new GuiLoader(this);
        guiLoader.load();
        LOGGER.success(" └  ✔  Menu");

        questsLoader = new QuestsLoader();
        questsLoader.load();
        LOGGER.success(" └  ✔  quests.yml");

        storage = new YAML(this);
        storage.load();
        LOGGER.success(" └  ✔  Storage");
        LOGGER.success("Configuration loaded ("+Speedometer.result()+"ms)");
        LOGGER.info("--------------");
        LOGGER.info("&6┏ Loading commands:");
        Speedometer.start();
        PluginCommand xClanCommand = this.getCommand("xclan");
        if (xClanCommand != null) {
            AdminCommand cmd = new AdminCommand(commandService);
            xClanCommand.setExecutor(cmd);
            xClanCommand.setTabCompleter(cmd);
        }
        PluginCommand clanCommand = this.getCommand("clan");
        if (clanCommand != null) {
            ClanCommand cmd = new ClanCommand(this);
            clanCommand.setExecutor(cmd);
            clanCommand.setTabCompleter(cmd);
        }
        LOGGER.success("┗ Commands created ("+Speedometer.result()+"ms)");
        LOGGER.info("--------------");
        LOGGER.info("&6┏ Loading API:");
        Speedometer.start();

        clanManager = new ClanManagerImpl(this);

        this.commandService = new CommandServiceImpl();

        questManager = new QuestManager(this);
        leaderboardService = new LeaderboardServiceImpl(this);

        eventRegistrar = new EventRegistryImpl();
        addonManager = new AddonManagerImpl(this, true);

        getServer().getServicesManager().register(
                TreexClansAPI.class,
                this,
                this,
                ServicePriority.Normal
        );

        ((AddonManagerImpl) addonManager).loadAddons();
        LOGGER.success("┗ API loaded ("+Speedometer.result()+"ms)");
        LOGGER.info("--------------");
        LOGGER.info("&6Last details");
        getServer().getPluginManager().registerEvents(new ClanListeners(this), this);
        getServer().getPluginManager().registerEvents(new QuestsListeners(this), this);

        new Metrics(this, 27749);

        LOGGER.success("⚡ TreexClans is ready");
    }

    @Override
    public void onDisable() {
        ActionRegistry.unregisterAll("treexclans");
        if (addonManager != null) {
            addonManager.disableAddons();
        }
        getServer().getServicesManager().unregister(TreexClansAPI.class);
        if (storage != null) storage.save();
        disableGlowForAll();
        if (clanPlaceholder != null) {
            if (clanPlaceholder.isPapi()) {
                clanPlaceholder.unregister();
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getOpenInventory().close();
        }
    }

    private void disableGlowForAll() {
        for (var clan : cfg.getClans().values()) {
            var memberImpls = new HashSet<>(clan.getMembers());
            memberImpls.add(clan.getLeader());
            for (var memberImpl : memberImpls) {
                Player player = Bukkit.getPlayer(memberImpl.getUuid());
                if (player != null) {
                    if (glow.hasObserver(player)) {
                        glow.removeObserver(player);
                    }
                }
            }
        }
    }
}
