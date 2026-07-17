package org.jetby.clans.common.configurations;


import lombok.AccessLevel;
import lombok.Getter;
import org.jetby.clans.api.service.clan.level.Level;
import org.jetby.clans.api.service.clan.member.rank.Permission;
import org.jetby.clans.api.service.clan.member.rank.PermissionRegistry;
import org.jetby.clans.api.service.clan.member.rank.Rank;
import org.jetby.clans.api.service.clan.member.rank.RankPerm;
import org.jetby.clans.common.tools.FileLoader;
import org.jetby.libb.action.record.Expression;
import org.jetby.libb.color.HashedSerializer;
import org.jetby.libb.color.SerializerType;
import org.jetby.libb.gui.parser.ParseUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.*;

@Getter
public class Config {

    private final FileConfiguration configuration;

    @Getter(AccessLevel.NONE)
    private final File file;

    @Getter(AccessLevel.NONE)
    private final FileConfiguration level;

    private final Map<Integer, Level> levels = new LinkedHashMap<>();

    private final Map<String, Rank> ranks = new HashMap<>();
    private Rank defaultRank;
    private Rank leaderRank;

    private boolean debug;

    private String chatFormat;

    private String prefixPlaceholder_hasPrefix;
    private String prefixPlaceholder_noPrefix;
    private String prefixPlaceholder_noClan;

    private StringValidationRules prefixValidation;
    private StringValidationRules createValidation;
    private StringValidationRules sloganValidation;

    private String tagPlaceholder_hasClan;
    private String tagPlaceholder_noClan;

    private String formattedTimeFormat;

    private List<String> blockedTags;
    private List<Expression> requirements = new ArrayList<>();

    private int inviteCooldown = 60;
    private int disbandCooldown = 15;
    private int denyCooldown = 120;

    public static HashedSerializer CONFIG_COLORIZER;

    public Config() {
        this.configuration = FileLoader.getFileConfiguration("config.yml");
        this.level = FileLoader.getFileConfiguration("levels.yml");
        this.file = FileLoader.getFile("config.yml");

        CONFIG_COLORIZER = new HashedSerializer(
                SerializerType.valueOf(
                        configuration.getString("serializer.type", "UNIFIED").toUpperCase()),
                configuration.getBoolean("serializer.cache.enabled", true),
                configuration.getInt("serializer.cache.max-size", 500)
        );
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
                } catch (IllegalArgumentException ignored) {
                }
            }
            return set;
        } else if (getStorageFilterType.equalsIgnoreCase("whitelist")) {
            EnumSet<Material> set = EnumSet.noneOf(Material.class);
            for (String str : getStorageFilterMaterials) {
                try {
                    Material material = Material.valueOf(str.toUpperCase());
                    set.add(material);
                } catch (IllegalArgumentException ignored) {
                }
            }
            return set;
        }
        return EnumSet.noneOf(Material.class);
    }

    public void load() {
        requirements.clear();
        blockedTags = null;
        ranks.clear();
        levels.clear();

        debug = configuration.getBoolean("debug", false);

        inviteCooldown = configuration.getInt("cooldowns.invite-expiration", 60);
        disbandCooldown = configuration.getInt("cooldowns.disband-expiration", 15);
        denyCooldown = configuration.getInt("cooldowns.deny", 120);

        getStorageFilterType = configuration.getString("clan-storage.filter.type", "BLACKLIST");
        getStorageFilterMaterials = configuration.getStringList("clan-storage.filter.materials");

        ConfigurationSection prefix = configuration.getConfigurationSection("prefix");
        if (prefix == null) prefix = configuration.createSection("prefix");

        prefixValidation = new StringValidationRules(
                prefix.getInt("min-clan-prefix-length", 3),
                prefix.getInt("max-clan-prefix-length", 16),
                prefix.getString("regex", "^[A-Za-z0-9]+$"),
                prefix.getString("length-ignored-symbols")
        );

        ConfigurationSection slogan = configuration.getConfigurationSection("slogan");
        if (slogan == null) slogan = configuration.createSection("slogan");
        sloganValidation = new StringValidationRules(
                slogan.getInt("min-clan-slogan-length", 3),
                slogan.getInt("max-clan-slogan-length", 16),
                slogan.getString("regex", "^[A-Za-z0-9]+$"),
                slogan.getString("length-ignored-symbols")
        );

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
                Set<Permission> perms = new HashSet<>();
                if (permission != null) {
                    for (String perm : permission.getKeys(false)) {

                        if (PermissionRegistry.exists(perm.toUpperCase())) {
                            perms.add(PermissionRegistry.get(perm.toUpperCase()));

                        } else {
                            Permission p = () -> perm;
                            perms.add(p);
                        }

                    }
                    perms.add(RankPerm.ALWAYS);

                }
                this.ranks.put(key.toLowerCase(), new Rank(key.toLowerCase(), name, perms));
            }
        }


        ConfigurationSection clanCreate = configuration.getConfigurationSection("clan-create");
        if (clanCreate != null) {

            requirements = ParseUtil.getExpressions(clanCreate.getList("requirements"));


            defaultRank = this.ranks.get(clanCreate.getString("member-rank", "member").toLowerCase());
            leaderRank = this.ranks.get(clanCreate.getString("leader-rank", "leader").toLowerCase());
            createValidation = new StringValidationRules(
                    clanCreate.getInt("min-clan-tag-length", 3),
                    clanCreate.getInt("max-clan-tag-length", 6),
                    clanCreate.getString("regex", "^[A-Za-z0-9]+$"),
                    null
            );
            blockedTags = clanCreate.getStringList("blocked-tags");
        }

        for (String id : level.getKeys(false)) {
            ConfigurationSection lSection = level.getConfigurationSection(id);
            if (lSection == null) continue;
            String name = lSection.getString("name");
            int exp = lSection.getInt("exp", 0);
            int chest = lSection.getInt("chest", 10);
            int maxMembers = lSection.getInt("max-members", 1);
            int maxBalance = lSection.getInt("max-balance", 0);
            List<String> quests = lSection.getStringList("quests");
            List<String> levelUpActions = lSection.getStringList("level-up-actions");
            levels.put(Integer.parseInt(id), new Level(id, name, exp, maxMembers, maxBalance, chest, quests, levelUpActions));
        }

        chatFormat = configuration.getString("chat-format", "<#FFE259>&l[TreexClans]</#FFA751> &e&l{player} &7▶ &f{message}");
    }


    public record StringValidationRules(
            int minLength,
            int maxLength,
            String regex,
            String lengthIgnoredSymbols
    ) {}
}
