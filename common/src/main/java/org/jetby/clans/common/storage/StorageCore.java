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
import org.jetby.clans.common.tools.ItemSerializer;
import org.jetby.clans.common.tools.LocationHandler;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

    public void saveClan(Clan clan) {
        cache.put(clan.getId(), clan);

        for (String key : clan.getRanks().keySet()) {
            Rank rank = clan.getRanks().get(key);
            Set<Permission> perms = rank.perms();
            section.set(clan, "ranks_" + rank.id() + "_permissions_ALWAYS", true);
            for (Permission perm : perms) {
                section.set(clan, "ranks_" + rank.id() + "_permissions_" + perm.getId(), true);
            }
        }

        section.set(clan, "prefix", clan.getPrefix());
        section.set(clan, "slogan", clan.getSlogan());
        section.set(clan, "balance", clan.getBalance());
        section.set(clan, "level", clan.getLevel().id());
        section.set(clan, "exp", clan.getExp());
        section.set(clan, "pvp", clan.isPvp());

        Member leader = clan.getLeader();
        section.set(clan, "leader_uuid", leader.getUuid().toString());
        saveMember(clan, leader);

        for (Member member : clan.getMembers()) {
            saveMember(clan, member);
        }

        for (Map.Entry<Integer, ItemStack> entry : clan.getChest().entrySet()) {
            if (entry.getValue() == null || entry.getValue().getType() == Material.AIR) continue;
            section.set(clan, "chest" + entry.getKey(), entry.getValue());
        }

        Location location = clan.getBase();
        section.set(clan, "base-location", location != null ? LocationHandler.serialize(location) : null);
    }

    public void saveMember(Clan clan, Member member) {
        section.set(clan, member, "rank", member.getRank().id());
        section.set(clan, member, "joined-at", member.getJoinedAt());
        section.set(clan, member, "last-online", member.getLastOnline());
        section.set(clan, member, "coin", member.getCoin());
        section.set(clan, member, "exp", member.getExp());
        section.set(clan, member, "kills", member.getKills());
        section.set(clan, member, "deaths", member.getDeaths());
    }
}