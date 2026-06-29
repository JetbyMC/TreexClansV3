package org.jetby.clans.common.storage.trash.simple;

import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.level.Level;
import org.jetby.clans.api.service.clan.member.Member;
import org.jetby.clans.api.service.clan.member.rank.Rank;
import org.jetby.clans.common.clan.model.ClanImpl;
import org.jetby.clans.common.clan.model.MemberImpl;
import org.jetby.clans.common.storage.trash.manager.DatabaseManager;

import java.sql.*;
import java.util.*;

public class ClanTable {

    private final DatabaseManager db;
    private final boolean mysql;

    public ClanTable(DatabaseManager db, boolean mysql) {
        this.db = db;
        this.mysql = mysql;
    }

    public void createTables() {
        String clans = mysql ? """
                CREATE TABLE IF NOT EXISTS clans (
                    id          VARCHAR(64)  PRIMARY KEY,
                    prefix      VARCHAR(64),
                    leader_uuid VARCHAR(36)  NOT NULL,
                    level_id    VARCHAR(16)  NOT NULL,
                    balance     DOUBLE       NOT NULL DEFAULT 0,
                    exp         INT          NOT NULL DEFAULT 0,
                    pvp         TINYINT      NOT NULL DEFAULT 0,
                    slogan      VARCHAR(255)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """ : """
                CREATE TABLE IF NOT EXISTS clans (
                    id          TEXT PRIMARY KEY,
                    prefix      TEXT,
                    leader_uuid TEXT NOT NULL,
                    level_id    TEXT NOT NULL,
                    balance     REAL NOT NULL DEFAULT 0,
                    exp         INTEGER NOT NULL DEFAULT 0,
                    pvp         INTEGER NOT NULL DEFAULT 0,
                    slogan      TEXT
                );
                """;

        String members = mysql ? """
                CREATE TABLE IF NOT EXISTS clan_members (
                    uuid        VARCHAR(36)  PRIMARY KEY,
                    clan_id     VARCHAR(64)  NOT NULL,
                    rank_id     VARCHAR(64)  NOT NULL,
                    joined_at   BIGINT       NOT NULL,
                    last_online BIGINT       NOT NULL,
                    coin        INT          NOT NULL DEFAULT 0,
                    exp         INT          NOT NULL DEFAULT 0,
                    kills       INT          NOT NULL DEFAULT 0,
                    deaths      INT          NOT NULL DEFAULT 0
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """ : """
                CREATE TABLE IF NOT EXISTS clan_members (
                    uuid        TEXT PRIMARY KEY,
                    clan_id     TEXT NOT NULL,
                    rank_id     TEXT NOT NULL,
                    joined_at   INTEGER NOT NULL,
                    last_online INTEGER NOT NULL,
                    coin        INTEGER NOT NULL DEFAULT 0,
                    exp         INTEGER NOT NULL DEFAULT 0,
                    kills       INTEGER NOT NULL DEFAULT 0,
                    deaths      INTEGER NOT NULL DEFAULT 0
                );
                """;

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(clans);
            stmt.execute(members);
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось создать таблицы: " + e.getMessage());
        }
    }

    public void saveClan(Clan clan) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(clanUpsert())) {

            ps.setString(1, clan.getId());
            ps.setString(2, clan.getPrefix());
            ps.setString(3, clan.getLeader().getUuid().toString());
            ps.setString(4, clan.getLevel().id());
            ps.setDouble(5, clan.getBalance());
            ps.setInt(6, clan.getExp());
            ps.setInt(7, clan.isPvp() ? 1 : 0);
            ps.setString(8, clan.getSlogan());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось сохранить клан: " + e.getMessage());
        }
    }

    public void saveMember(String clanId, Member member) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(memberUpsert())) {

            ps.setString(1, member.getUuid().toString());
            ps.setString(2, clanId);
            ps.setString(3, member.getRank().id());
            ps.setLong(4, member.getJoinedAt());
            ps.setLong(5, member.getLastOnline());
            ps.setInt(6, member.getCoin());
            ps.setInt(7, member.getExp());
            ps.setInt(8, member.getKills());
            ps.setInt(9, member.getDeaths());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось сохранить участника: " + e.getMessage());
        }
    }

    public void deleteClan(String clanId) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM clans WHERE id = ?")) {
            ps.setString(1, clanId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось удалить клан: " + e.getMessage());
        }
    }

    public void deleteMember(UUID uuid) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM clan_members WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось удалить участника: " + e.getMessage());
        }
    }

    public void loadAll(Map<String, Clan> storage, Map<String, Rank> ranks, Map<Integer, Level> levels) {
        try (Connection conn = db.getConnection();
             PreparedStatement clanPs = conn.prepareStatement("SELECT * FROM clans");
             ResultSet clanRs = clanPs.executeQuery()) {

            while (clanRs.next()) {
                String clanId     = clanRs.getString("id");
                String leaderUuid = clanRs.getString("leader_uuid");
                String levelId    = clanRs.getString("level_id");

                List<MemberImpl> memberList = loadMembers(conn, clanId, ranks);

                MemberImpl leader = memberList.stream()
                        .filter(m -> m.getUuid().toString().equals(leaderUuid))
                        .findFirst()
                        .orElse(null);

                if (leader == null) continue;

                memberList.remove(leader);

                Level level = levels.getOrDefault(Integer.parseInt(levelId),
                        new Level("1", "1", 0, 1, 0, 1, new ArrayList<>(), new ArrayList<>()));

                ClanImpl clan = new ClanImpl(
                        clanId,
                        clanRs.getString("prefix"),
                        leader,
                        new HashSet<>(memberList),
                        ranks,
                        level,
                        clanRs.getDouble("balance"),
                        null,
                        clanRs.getInt("exp"),
                        clanRs.getInt("pvp") == 1,
                        new HashMap<>(),
                        clanRs.getString("slogan")
                );

                storage.put(clanId, clan);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось загрузить кланы: " + e.getMessage());
        }
    }

    private List<MemberImpl> loadMembers(Connection conn, String clanId, Map<String, Rank> ranks) throws SQLException {
        List<MemberImpl> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM clan_members WHERE clan_id = ?")) {
            ps.setString(1, clanId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Rank rank = ranks.get(rs.getString("rank_id"));
                    if (rank == null) continue;

                    list.add(new MemberImpl(
                            UUID.fromString(rs.getString("uuid")),
                            rank,
                            rs.getLong("joined_at"),
                            rs.getLong("last_online"),
                            false,
                            rs.getInt("coin"),
                            rs.getInt("exp"),
                            rs.getInt("kills"),
                            rs.getInt("deaths")
                    ));
                }
            }
        }

        return list;
    }

    private String clanUpsert() {
        if (mysql) {
            return """
                    INSERT INTO clans (id, prefix, leader_uuid, level_id, balance, exp, pvp, slogan)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        prefix      = VALUES(prefix),
                        leader_uuid = VALUES(leader_uuid),
                        level_id    = VALUES(level_id),
                        balance     = VALUES(balance),
                        exp         = VALUES(exp),
                        pvp         = VALUES(pvp),
                        slogan      = VALUES(slogan);
                    """;
        }
        return """
                INSERT INTO clans (id, prefix, leader_uuid, level_id, balance, exp, pvp, slogan)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    prefix      = excluded.prefix,
                    leader_uuid = excluded.leader_uuid,
                    level_id    = excluded.level_id,
                    balance     = excluded.balance,
                    exp         = excluded.exp,
                    pvp         = excluded.pvp,
                    slogan      = excluded.slogan;
                """;
    }

    private String memberUpsert() {
        if (mysql) {
            return """
                    INSERT INTO clan_members (uuid, clan_id, rank_id, joined_at, last_online, coin, exp, kills, deaths)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        clan_id     = VALUES(clan_id),
                        rank_id     = VALUES(rank_id),
                        last_online = VALUES(last_online),
                        coin        = VALUES(coin),
                        exp         = VALUES(exp),
                        kills       = VALUES(kills),
                        deaths      = VALUES(deaths);
                    """;
        }
        return """
                INSERT INTO clan_members (uuid, clan_id, rank_id, joined_at, last_online, coin, exp, kills, deaths)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                    clan_id     = excluded.clan_id,
                    rank_id     = excluded.rank_id,
                    last_online = excluded.last_online,
                    coin        = excluded.coin,
                    exp         = excluded.exp,
                    kills       = excluded.kills,
                    deaths      = excluded.deaths;
                """;
    }
}