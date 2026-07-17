package org.jetby.clans.common.storage;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.level.Level;
import org.jetby.clans.api.service.clan.member.Member;
import org.jetby.clans.api.service.clan.member.rank.Permission;
import org.jetby.clans.api.service.clan.member.rank.PermissionRegistry;
import org.jetby.clans.api.service.clan.member.rank.Rank;
import org.jetby.clans.api.storage.Storage;
import org.jetby.clans.api.storage.base.BaseSection;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.clan.model.ClanImpl;
import org.jetby.clans.common.clan.model.MemberImpl;
import org.jetby.clans.common.tools.LocationHandler;

import java.util.*;

public abstract class StorageCore implements Storage {

    public final TreexClans plugin = TreexClans.getInstance();

    @Getter
    protected final Map<String, Clan> cache = new HashMap<>();

    @Getter
    protected BaseSection section;

    @Override
    public List<Clan> getClanList(int limit) {
        return cache.values().stream().limit(limit).toList();
    }

    public Clan loadClan(String name) {
        if (cache.containsKey(name)) return cache.get(name);

        BaseSection base = section.section(name);
        if (base == null) return null;

        String leaderUuidStr = base.getString("leader_uuid").join();
        if (leaderUuidStr == null) return null;

        Map<String, Rank> ranks = new HashMap<>();
        for (String rankId : base.section("ranks").keys().join()) {
            BaseSection permsSection = base.section("ranks").section(rankId).section("permissions");
            Set<Permission> permissions = new HashSet<>();
            for (String perm : permsSection.keys().join()) {
                if (permsSection.getBoolean(perm).join()) {
                    Permission permission = PermissionRegistry.get(perm);
                    if (permission != null) {
                        permissions.add(permission);
                    } else {
                        TreexClans.LOGGER.warn("Unknown permission '" + perm + "' for clan " + name);
                    }
                }
            }

            String displayName = null;
            Rank cfgRank = plugin.getCfg().getRanks().get(rankId);
            if (cfgRank != null) {
                displayName = cfgRank.name();
            }

            ranks.put(rankId, new Rank(rankId, displayName, permissions));
        }

        Member leader = loadMember(base, UUID.fromString(leaderUuidStr), ranks);

        Set<Member> members = new HashSet<>();
        BaseSection membersSection = base.section("members");
        if (membersSection != null) {
            for (String uuidStr : membersSection.keys().join()) {
                if (leaderUuidStr.equals(uuidStr)) continue;
                members.add(loadMember(base, UUID.fromString(uuidStr), ranks));
            }
        }

        String levelId = base.getString("level").join();
        Level level = plugin.getCfg().getLevels().getOrDefault(Integer.parseInt(levelId), new Level("1", "1", 0, 1, 0, 1, new ArrayList<>(), new ArrayList<>()));

        double balance = base.getDouble("balance").join();
        int exp = base.getInt("exp").join();
        boolean pvp = base.getBoolean("pvp").join();
        String slogan = base.getString("slogan").join();

        String locStr = base.getString("base-location").join();
        Location baseLocation = locStr != null ? LocationHandler.deserialize(locStr) : null;

        Map<Integer, ItemStack> chest = new HashMap<>();
        BaseSection chests = base.section("chests");
        for (String key : chests.keys().join()) {
            if (key.startsWith("chest") && key.length() > 5) {
                try {
                    int slot = Integer.parseInt(key.substring(5));
                    ItemStack item = (ItemStack) chests.get(key).join();

                    if (item != null) chest.put(slot, item);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return cache.put(name, new ClanImpl(
                name,
                null,
                leader,
                members,
                ranks,
                level,
                balance,
                baseLocation,
                exp,
                pvp,
                chest,
                slogan
        ));
    }

    private Member loadMember(BaseSection clan, UUID uuid, Map<String, Rank> ranks) {
        BaseSection base = clan.section("members").section(uuid.toString());
        String rankId = base.getString("rank").join();
        Rank rank = ranks.get(rankId);
        if (rank == null) {
            rank = plugin.getCfg().getDefaultRank();
        }
        return new MemberImpl(uuid, rank,
                base.getLong("joined-at").join(),
                base.getLong("last-online").join(),
                false,
                base.getInt("coin").join(),
                base.getInt("exp").join(),
                base.getInt("kills").join(),
                base.getInt("deaths").join());
    }

    public void saveClan(Clan clan) {

        for (String key : clan.getRanks().keySet()) {
            Rank rank = clan.getRanks().get(key);
            BaseSection ranks = section.of(clan).section("ranks");

            ranks.section(rank.id())
                    .section("permissions")
                    .set("ALWAYS", true);

            for (Permission permission : PermissionRegistry.getAll()) {
                boolean status = rank.hasPermission(permission);

                if (status) {
                    ranks.section(rank.id())
                            .section("permissions")
                            .set(permission.getId(), true);
                } else {
                    ranks.section(rank.id())
                            .section("permissions")
                            .remove(permission.getId());
                }
            }
        }

        section.of(clan).set("prefix", clan.getPrefix());
        section.of(clan).set("slogan", clan.getSlogan());
        section.of(clan).set("balance", clan.getBalance());
        section.of(clan).set("level", clan.getLevel().id());
        section.of(clan).set("exp", clan.getExp());
        section.of(clan).set("pvp", clan.isPvp());

        section.of(clan).set("leader_uuid", clan.getLeader().getUuid().toString());

        for (Member member : clan.getMembersWithLeader()) {
            saveMember(clan, member);
        }

        BaseSection chests = section.of(clan).section("chests");
        for (Map.Entry<Integer, ItemStack> entry : clan.getChest().entrySet()) {
            if (entry.getValue() == null || entry.getValue().getType() == Material.AIR) continue;
            chests.set("chest" + entry.getKey(), entry.getValue());
        }

        Location location = clan.getBase();
        section.of(clan).set("base-location", location != null ? LocationHandler.serialize(location) : null);

        cache.put(clan.getId(), clan);

    }

    public void saveMember(Clan clan, Member member) {
        section.of(clan, member).set("rank", member.getRank().id());
        section.of(clan, member).set("joined-at", member.getJoinedAt());
        section.of(clan, member).set("last-online", member.getLastOnline());
        section.of(clan, member).set("coin", member.getCoin());
        section.of(clan, member).set("exp", member.getExp());
        section.of(clan, member).set("kills", member.getKills());
        section.of(clan, member).set("deaths", member.getDeaths());
    }

    public boolean deleteClan(@NotNull Clan clan) {
        boolean removed = cache.remove(clan.getId()) != null;
        section.set(clan.getId(), null);
        return removed;
    }

    public boolean deleteClan(@NotNull String name) {
        boolean removed = cache.remove(name) != null;
        section.set(name, null);
        return removed;
    }
}