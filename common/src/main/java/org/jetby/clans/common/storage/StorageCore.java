package org.jetby.clans.common.storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetby.clans.api.events.ClanCreateEvent;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.level.Level;
import org.jetby.clans.api.service.clan.member.Member;
import org.jetby.clans.api.service.clan.member.rank.Permission;
import org.jetby.clans.api.service.clan.member.rank.Rank;
import org.jetby.clans.api.storage.Storage;
import org.jetby.clans.api.storage.base.BaseSection;
import org.jetby.clans.common.TreexClans;
import org.jetby.clans.common.clan.model.ClanImpl;
import org.jetby.clans.common.clan.model.MemberImpl;
import org.jetby.clans.common.tools.LocationHandler;

import java.util.*;

public abstract class StorageCore implements Storage {

    final TreexClans plugin = TreexClans.getInstance();
    protected final Map<String, Clan> cache = new HashMap<>();

    protected BaseSection section;

    @Override
    public BaseSection getSection() {
        return section;
    }

    @Override
    public boolean createClan(@NotNull String tag, @NotNull Player leaderPlayer) {
        if (clanExists(tag)) return false;

        MemberImpl leader = new MemberImpl(
                leaderPlayer.getUniqueId(),
                plugin.getCfg().getLeaderRank(),
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                false,
                0,
                0,
                0,
                0
        );

        Level baseLevel = plugin.getCfg().getLevels().getOrDefault(
                1,
                new Level("1", "1", 0, 1, 0, 1, new ArrayList<>(), new ArrayList<>())
        );

        Clan clan = new ClanImpl(
                tag,
                null,
                leader,
                new HashSet<>(),
                plugin.getCfg().getRanks(),
                baseLevel,
                0.0,
                null,
                0,
                false,
                new HashMap<>(),
                ""
        );

        var event = new ClanCreateEvent(clan, leaderPlayer);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return false;

        saveClan(clan);
        return true;
    }

    @Override
    public boolean createClan(@NotNull String name, @NotNull Clan clan) {
        if (clanExists(name)) return false;
        saveClan(clan);
        return true;
    }

    @Override
    public boolean deleteClan(@NotNull Clan clan, @Nullable Player initiator) {
        return deleteClan(clan.getId());
    }

    @Override
    public boolean isInClan(@NotNull UUID uuid) {
        return getClanByMember(uuid) != null;
    }

    @Override
    public @Nullable Clan getClanByMember(@NotNull Member member) {
        return getClanByMember(member.getUuid());
    }

    @Override
    public @Nullable Clan getClanByMember(@NotNull UUID uuid) {
        for (Clan clan : cache.values()) {
            if (clan.getLeader().getUuid().equals(uuid)) return clan;
            if (clan.getMember(uuid) != null) return clan;
        }
        return null;
    }

    @Override
    public @Nullable Clan getClan(@NotNull String name) {
        if (cache.containsKey(name)) return cache.get(name);
        return loadClan(name);
    }

    @Override
    public boolean clanExists(@NotNull String name) {
        return cache.containsKey(name);
    }

    @Override
    public boolean deleteClan(@NotNull String name) {
        cache.remove(name);
        return true;
    }

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

        Member leader = loadMember(base, UUID.fromString(leaderUuidStr));

        Set<Member> members = new HashSet<>();
        BaseSection membersSection = base.section("members");
        if (membersSection != null) {
            for (String uuidStr : membersSection.keys().join()) {
                members.add(loadMember(base, UUID.fromString(uuidStr)));
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
        for (String key : base.section("chests").keys().join()) {
            if (key.startsWith("chest") && key.length() > 5) {
                try {
                    int slot = Integer.parseInt(key.substring(5));
                    ItemStack item = (ItemStack) base.section("chests").get(key).join();

                    if (item != null) chest.put(slot, item);
                } catch (NumberFormatException ignored) {
                }
            }
        }


        Clan clan = new ClanImpl(name, null, leader, members, plugin.getCfg().getRanks(), level, balance, baseLocation, exp, pvp, chest, slogan);
        cache.put(name, clan);
        return clan;
    }

    private Member loadMember(BaseSection clan, UUID uuid) {
        BaseSection base = clan.section("members").section(uuid.toString());
        String rankId = base.getString("rank").join();
        Rank rank = plugin.getCfg().getRanks().get(rankId);
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
        cache.put(clan.getId(), clan);

        for (String key : clan.getRanks().keySet()) {
            Rank rank = clan.getRanks().get(key);
            Set<Permission> perms = rank.perms();

            BaseSection ranks = section.of(clan).section("ranks");

            ranks.section(rank.id())
                    .section("permissions")
                    .set("ALWAYS", true);

            for (Permission perm : perms) {
                ranks.section(rank.id())
                        .section("permissions")
                        .set(perm.getId(), true);
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

        for (Map.Entry<Integer, ItemStack> entry : clan.getChest().entrySet()) {
            if (entry.getValue() == null || entry.getValue().getType() == Material.AIR) continue;
            section.of(clan).section("chests").set("chest" + entry.getKey(), entry.getValue());
        }

        Location location = clan.getBase();
        section.of(clan).set("base-location", location != null ? LocationHandler.serialize(location) : null);
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
}