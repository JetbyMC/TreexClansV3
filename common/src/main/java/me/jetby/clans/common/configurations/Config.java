package me.jetby.clans.common.configurations;


import lombok.AccessLevel;
import lombok.Getter;
import me.jetby.clans.api.service.clan.level.Level;
import me.jetby.clans.api.service.clan.member.rank.Rank;
import me.jetby.clans.api.service.clan.member.rank.RankPerm;
import me.jetby.clans.common.tools.FileLoader;
import me.jetby.libb.action.record.Expression;
import me.jetby.libb.color.HashedSerializer;
import me.jetby.libb.color.SerializerType;
import me.jetby.libb.gui.parser.ParseUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.*;

@Getter
public class Config {

    @Getter(AccessLevel.NONE)
    private final FileConfiguration configuration;

    @Getter(AccessLevel.NONE)
    private final File file;

    @Getter(AccessLevel.NONE)
    private final FileConfiguration level;

    private final Map<Integer, Level> levels = new LinkedHashMap<>();

    private final Map<String, Rank> defaultRanks = new HashMap<>();
    private Rank defaultRank;
    private Rank leaderRank;

    private boolean debug;

    private String chatFormat;

    private String prefixPlaceholder_hasPrefix;
    private String prefixPlaceholder_noPrefix;
    private String prefixPlaceholder_noClan;
    private int prefixMinLength;
    private int prefixMaxLength;
    private String prefixRegex;
    private String lengthIgnoredSymbols;

    private String tagPlaceholder_hasClan;
    private String tagPlaceholder_noClan;

    private String formattedTimeFormat;

    private int minTagLength;
    private int maxTagLength;
    private String regex;
    private List<String> blockedTags;
    private List<Expression> requirements = new ArrayList<>();
    private boolean gradualQuest;

    public static HashedSerializer CONFIG_COLORIZER;

    public Config() {
        this.configuration = FileLoader.getFileConfiguration("config.yml");
        this.level = FileLoader.getFileConfiguration("levels.yml");
        this.file = FileLoader.getFile("config.yml");

        CONFIG_COLORIZER = new HashedSerializer(
                SerializerType.valueOf(configuration.getString("serializer.type", "UNIFIED").toUpperCase()),
                configuration.getBoolean("serializer.cache"));
    }

    private String getStorageFilterType;
    private List<String> getStorageFilterMaterials;

    public EnumSet<Material> getAvailableStorageMaterials() {
        if (getStorageFilterType.equalsIgnoreCase("blacklist")) {
            EnumSet<Material> set = EnumSet.copyOf(Arrays.asList(Material.values()));
            for (String str : getStorageFilterMaterials) {
                try {
                    Material material = Material.valueOf(str.toUpperCase());
                    set.remove(material);
                } catch (IllegalArgumentException ignored) {}
            }
            return set;
        } else if (getStorageFilterType.equalsIgnoreCase("whitelist")) {
            EnumSet<Material> set = EnumSet.noneOf(Material.class);
            for (String str : getStorageFilterMaterials) {
                try {
                    Material material = Material.valueOf(str.toUpperCase());
                    set.add(material);
                } catch (IllegalArgumentException ignored) {}
            }
            return set;
        }
        return EnumSet.noneOf(Material.class);
    }
    public void load() {
        requirements.clear();
        blockedTags = null;
        defaultRanks.clear();
        levels.clear();

        debug = configuration.getBoolean("debug", false);

        getStorageFilterType = configuration.getString("clan-storage.filter.type", "BLACKLIST");
        getStorageFilterMaterials = configuration.getStringList("clan-storage.filter.materials");

        ConfigurationSection prefix = configuration.getConfigurationSection("prefix");
        if (prefix == null) prefix = configuration.createSection("prefix");
        prefixMinLength = prefix.getInt("min-clan-prefix-length", 3);
        prefixMaxLength = prefix.getInt("max-clan-prefix-length", 16);
        prefixRegex = prefix.getString("regex", "^[A-Za-z0-9]+$");
        lengthIgnoredSymbols = prefix.getString("length-ignored-symbols");

        ConfigurationSection prefixPlaceholder = prefix.getConfigurationSection("placeholder");
        if (prefixPlaceholder == null) prefixPlaceholder = prefix.createSection("placeholder");
        prefixPlaceholder_hasPrefix = prefixPlaceholder.getString("has_prefix", "");
        prefixPlaceholder_noPrefix = prefixPlaceholder.getString("no_prefix", "");
        prefixPlaceholder_noClan = prefixPlaceholder.getString("no_clan", "");


        ConfigurationSection tag = configuration.getConfigurationSection("tag-placeholder");
        if (tag == null) tag = configuration.createSection("tag-placeholder");
        tagPlaceholder_hasClan = tag.getString("has_clan");
        tagPlaceholder_noClan = tag.getString("no_clan");


        formattedTimeFormat = configuration.getString("placeholder-show-format", "%weeks% %days% %hours% %minutes% %seconds%");

        ConfigurationSection ranks = configuration.getConfigurationSection("ranks");
        if (ranks != null) {
            for (String key : ranks.getKeys(false)) {
                ConfigurationSection rank = ranks.getConfigurationSection(key);
                if (rank == null) continue;
                String name = rank.getString("display-name");
                ConfigurationSection permission = rank.getConfigurationSection("permissions");
                Set<RankPerm> perms = new HashSet<>();
                if (permission != null) {
                    for (String perm : permission.getKeys(false)) {
                        switch (perm.toLowerCase()) {
                            case "invite" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.INVITE);
                            }
                            case "kick" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.KICK);
                            }
                            case "base" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.BASE);
                            }
                            case "setbase" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.SETBASE);
                            }
                            case "setrank" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.SETRANK);
                            }
                            case "deposit" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.DEPOSIT);
                            }
                            case "withdraw" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.WITHDRAW);
                            }
                            case "pvp" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.PVP);
                            }
                            case "setslogan" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.SETSLOGAN);
                            }
                            case "setprefix" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerm.SETPREFIX);
                            }
                        }

                    }

                }
                defaultRanks.put(key.toLowerCase(), new Rank(key.toLowerCase(), name, perms));
            }
        }


        ConfigurationSection clanCreate = configuration.getConfigurationSection("clan-create");
        if (clanCreate != null) {

            requirements = ParseUtil.getExpressions(clanCreate.getList("requirements"));


            defaultRank = defaultRanks.get(clanCreate.getString("member-rank", "member").toLowerCase());
            leaderRank = defaultRanks.get(clanCreate.getString("leader-rank", "leader").toLowerCase());
            minTagLength = clanCreate.getInt("min-clan-tag-length", 3);
            maxTagLength = clanCreate.getInt("max-clan-tag-length", 6);
            blockedTags = clanCreate.getStringList("blocked-tags");
            regex = clanCreate.getString("regex", "^[A-Za-z0-9]+$");
        }

        for (String id : level.getKeys(false)) {
            ConfigurationSection lSection = level.getConfigurationSection(id);
            if (lSection == null) continue;
            int exp = lSection.getInt("exp", 0);
            int chest = lSection.getInt("chest", 10);
            int maxMembers = lSection.getInt("max-members", 1);
            int maxBalance = lSection.getInt("max-balance", 0);
            List<String> quests = lSection.getStringList("quests");
            List<String> levelUpActions = lSection.getStringList("level-up-actions");
            levels.put(Integer.parseInt(id), new Level(id, exp, maxMembers, maxBalance, chest, quests, levelUpActions));
        }

        gradualQuest = configuration.getBoolean("gradual-quest", false);
        chatFormat = configuration.getString("chat-format", "<#FFE259>&l[TreexClans]</#FFA751> &e&l{player} &7▶ &f{message}");
    }

}
