package org.jetby.clans.common.storage;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.level.Level;
import org.jetby.clans.api.service.clan.member.Member;
import org.jetby.clans.api.service.clan.member.rank.Rank;
import org.jetby.clans.api.storage.base.BaseSection;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.clan.model.ClanImpl;
import org.jetby.clans.common.clan.model.MemberImpl;
import org.jetby.clans.common.tools.FileLoader;
import org.jetby.clans.common.tools.LocationHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class YamlStorageCore extends StorageCore {

    private final TreexClans plugin;
    private final File file;
    private final FileConfiguration configuration;

    public YamlStorageCore(TreexClans plugin) {
        this.plugin = plugin;
        this.configuration = FileLoader.getFileConfiguration("storage.yml");
        this.file = FileLoader.getFile("storage.yml");
    }

    @Override
    public List<Clan> getClanList(int limit) {
        return cache.values().stream().limit(limit).toList();
    }

    @Override
    public void initialize() {
        initBaseSection();
        for (String key : configuration.getKeys(false)) {
            getClan(key);
        }
    }

    @Override
    public void shutdown() {
        for (Clan clan : cache.values()) {
            saveClan(clan);
        }
        try {
            configuration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean clanExists(@NotNull String name) {
        return cache.containsKey(name) || configuration.contains(name);
    }

    @Override
    public boolean deleteClan(@NotNull String name) {
        cache.remove(name);
        configuration.set(name, null);
        return true;
    }

    @Override
    public @Nullable Clan getClan(@NotNull String name) {
        if (cache.containsKey(name)) return cache.get(name);
        if (!configuration.contains(name)) return null;

        String leaderUuidStr = configuration.getString(name + ".leader_uuid");
        if (leaderUuidStr == null) return null;

        Member leader = loadMember(name, UUID.fromString(leaderUuidStr), true);

        Set<Member> members = new HashSet<>();
        var membersSection = configuration.getConfigurationSection(name + ".members");
        if (membersSection != null) {
            for (String uuidStr : membersSection.getKeys(false)) {
                members.add(loadMember(name, UUID.fromString(uuidStr), false));
            }
        }

        String levelId = configuration.getString(name + ".level", "1");
        Level level = plugin.getCfg().getLevels().getOrDefault(
                Integer.parseInt(levelId),
                new Level("1", "1", 0, 1, 0, 1, new ArrayList<>(), new ArrayList<>())
        );

        double balance = configuration.getDouble(name + ".balance");
        int exp = configuration.getInt(name + ".exp");
        boolean pvp = configuration.getBoolean(name + ".pvp");
        String slogan = configuration.getString(name + ".slogan", "");

        String locStr = configuration.getString(name + ".base-location");
        Location base = locStr != null ? LocationHandler.deserialize(locStr) : null;

        Map<Integer, ItemStack> chest = new HashMap<>();
        var clanSection = configuration.getConfigurationSection(name);
        if (clanSection != null) {
            for (String key : clanSection.getKeys(false)) {
                if (key.startsWith("chest") && key.length() > 5) {
                    try {
                        int slot = Integer.parseInt(key.substring(5));
                        ItemStack item = configuration.getItemStack(name + "." + key);
                        if (item != null) chest.put(slot, item);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        Clan clan = new ClanImpl(
                name, null, leader, members,
                plugin.getCfg().getRanks(),
                level, balance, base, exp, pvp, chest, slogan
        );
        cache.put(name, clan);
        return clan;
    }

    private Member loadMember(String clanId, UUID uuid, boolean isLeader) {
        String base = clanId + (isLeader ? ".leader." : ".members.") + uuid + ".";
        String rankId = configuration.getString(base + "rank");
        Rank rank = plugin.getCfg().getRanks().get(rankId);
        if (rank == null) {
            rank = plugin.getCfg().getDefaultRank();
        }
        return new MemberImpl(
                uuid,
                rank,
                configuration.getLong(base + "joined-at"),
                configuration.getLong(base + "last-online"),
                false,
                configuration.getInt(base + "coin"),
                configuration.getInt(base + "exp"),
                configuration.getInt(base + "kills"),
                configuration.getInt(base + "deaths")
        );
    }

    private void initBaseSection() {
        this.section = new BaseSection() {
            @Override
            public CompletableFuture<Void> set(String key, Object value) {
                return CompletableFuture.runAsync(() -> configuration.set(key, value));
            }

            @Override
            public CompletableFuture<Void> set(Clan clan, String key, Object value) {
                String path = clan.getId() + "." + key;
                return CompletableFuture.runAsync(() -> configuration.set(path, value));
            }

            @Override
            public CompletableFuture<Void> set(Clan clan, Member member, String key, Object value) {
                String path = buildMemberPath(clan, member, key);
                return CompletableFuture.runAsync(() -> configuration.set(path, value));
            }

            @Override
            public CompletableFuture<Object> get(String key) {
                return CompletableFuture.supplyAsync(() -> configuration.get(key));
            }

            @Override
            public CompletableFuture<Object> get(Clan clan, String key) {
                String path = clan.getId() + "." + key;
                return CompletableFuture.supplyAsync(() -> configuration.get(path));
            }

            @Override
            public CompletableFuture<Object> get(Clan clan, Member member, String key) {
                String path = buildMemberPath(clan, member, key);
                return CompletableFuture.supplyAsync(() -> configuration.get(path));
            }

            @Override
            public CompletableFuture<String> getString(String key) {
                return CompletableFuture.supplyAsync(() -> configuration.getString(key));
            }

            @Override
            public CompletableFuture<String> getString(Clan clan, String key) {
                String path = clan.getId() + "." + key;
                return CompletableFuture.supplyAsync(() -> configuration.getString(path));
            }

            @Override
            public CompletableFuture<String> getString(Clan clan, Member member, String key) {
                String path = buildMemberPath(clan, member, key);
                return CompletableFuture.supplyAsync(() -> configuration.getString(path));
            }

            @Override
            public CompletableFuture<List<?>> getList(String key) {
                return CompletableFuture.supplyAsync(() -> configuration.getList(key));
            }

            @Override
            public CompletableFuture<List<?>> getList(Clan clan, String key) {
                String path = clan.getId() + "." + key;
                return CompletableFuture.supplyAsync(() -> configuration.getList(path));
            }

            @Override
            public CompletableFuture<List<?>> getList(Clan clan, Member member, String key) {
                String path = buildMemberPath(clan, member, key);
                return CompletableFuture.supplyAsync(() -> configuration.getList(path));
            }

            @Override
            public CompletableFuture<List<String>> getStringList(String key) {
                return CompletableFuture.supplyAsync(() -> configuration.getStringList(key));
            }

            @Override
            public CompletableFuture<List<String>> getStringList(Clan clan, String key) {
                String path = clan.getId() + "." + key;
                return CompletableFuture.supplyAsync(() -> configuration.getStringList(path));
            }

            @Override
            public CompletableFuture<List<String>> getStringList(Clan clan, Member member, String key) {
                String path = buildMemberPath(clan, member, key);
                return CompletableFuture.supplyAsync(() -> configuration.getStringList(path));
            }

            @Override
            public CompletableFuture<Integer> getInt(String key) {
                return CompletableFuture.supplyAsync(() -> configuration.getInt(key));
            }

            @Override
            public CompletableFuture<Integer> getInt(Clan clan, String key) {
                String path = clan.getId() + "." + key;
                return CompletableFuture.supplyAsync(() -> configuration.getInt(path));
            }

            @Override
            public CompletableFuture<Integer> getInt(Clan clan, Member member, String key) {
                String path = buildMemberPath(clan, member, key);
                return CompletableFuture.supplyAsync(() -> configuration.getInt(path));
            }

            @Override
            public CompletableFuture<Double> getDouble(String key) {
                return CompletableFuture.supplyAsync(() -> configuration.getDouble(key));
            }

            @Override
            public CompletableFuture<Double> getDouble(Clan clan, String key) {
                String path = clan.getId() + "." + key;
                return CompletableFuture.supplyAsync(() -> configuration.getDouble(path));
            }

            @Override
            public CompletableFuture<Double> getDouble(Clan clan, Member member, String key) {
                String path = buildMemberPath(clan, member, key);
                return CompletableFuture.supplyAsync(() -> configuration.getDouble(path));
            }

            @Override
            public CompletableFuture<Long> getLong(String key) {
                return CompletableFuture.supplyAsync(() -> configuration.getLong(key));
            }

            @Override
            public CompletableFuture<Long> getLong(Clan clan, String key) {
                String path = clan.getId() + "." + key;
                return CompletableFuture.supplyAsync(() -> configuration.getLong(path));
            }

            @Override
            public CompletableFuture<Long> getLong(Clan clan, Member member, String key) {
                String path = buildMemberPath(clan, member, key);
                return CompletableFuture.supplyAsync(() -> configuration.getLong(path));
            }

            @Override
            public CompletableFuture<Boolean> getBoolean(String key) {
                return CompletableFuture.supplyAsync(() -> configuration.getBoolean(key));
            }

            @Override
            public CompletableFuture<Boolean> getBoolean(Clan clan, String key) {
                String path = clan.getId() + "." + key;
                return CompletableFuture.supplyAsync(() -> configuration.getBoolean(path));
            }

            @Override
            public CompletableFuture<Boolean> getBoolean(Clan clan, Member member, String key) {
                String path = buildMemberPath(clan, member, key);
                return CompletableFuture.supplyAsync(() -> configuration.getBoolean(path));
            }

            private String buildMemberPath(Clan clan, Member member, String key) {
                boolean isLeader = clan.getLeader() == member;
                String section = isLeader ? "leader" : "members";
                return clan.getId() + "." + section + "." + member.getUuid() + "." + key;
            }
        };
    }
}