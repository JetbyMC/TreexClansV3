package org.jetby.clans.common.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetby.clans.api.service.clan.Clan;
import org.jetby.clans.api.service.clan.member.Member;
import org.jetby.clans.api.storage.base.BaseSection;
import org.jetby.clans.common.configurations.Config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class MySQLStorageImpl extends StorageCore {

    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[A-Za-z0-9_\\-]+");

    private final ExecutorService executor = Executors.newSingleThreadExecutor(
            r -> new Thread(r, "treexclans-storage-mysql")
    );

    @Override
    ExecutorService executor() {
        return executor;
    }

    // column-existence cache: table name -> known column names (lazily loaded via information_schema)
    private final Map<String, Set<String>> columnCache = new ConcurrentHashMap<>();
    // group EAV tables we've already confirmed exist ("<group>_data")
    private final Set<String> knownEavTables = ConcurrentHashMap.newKeySet();

    private String databaseName;
    private HikariDataSource dataSource;

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final boolean useSSL;

    public MySQLStorageImpl(Config cfg) {
        this.host = cfg.getMysqlHost();
        this.port = cfg.getMysqlPort();
        this.database = cfg.getMysqlDatabase();
        this.username = cfg.getMysqlUsername();
        this.password = cfg.getMysqlPassword();
        this.useSSL = cfg.isMysqlUseSSL();
    }

    @Override
    public void initialize() {
        HikariConfig config = getHikariConfig();
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource = new HikariDataSource(config);

        this.databaseName = database;

        createBaseTables();
        this.section = new SQLSection("");
        loadExistingClans();
    }

    private @NotNull HikariConfig getHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database +
                "?useSSL=" + useSSL +
                "&useUnicode=true&characterEncoding=utf8" +
                "&autoReconnect=true&allowPublicKeyRetrieval=true");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(10000L);
        return config;
    }

    @Override
    public void shutdown() {
        for (Clan clan : cache.values()) {
            saveClan(clan);
        }
        CompletableFuture.runAsync(() -> {
        }, executor).join();

        executor.shutdown();
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private void createBaseTables() {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                execute(connection,
                        "CREATE TABLE IF NOT EXISTS `clans` (`id` VARCHAR(191) NOT NULL PRIMARY KEY) " +
                                "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                execute(connection,
                        "CREATE TABLE IF NOT EXISTS `clan_members` (" +
                                "`uuid` VARCHAR(191) NOT NULL PRIMARY KEY, " +
                                "`clan_id` VARCHAR(191) NOT NULL, " +
                                "CONSTRAINT `clan_members_clan_id_fk` FOREIGN KEY (`clan_id`) REFERENCES `clans` (`id`)" +
                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor).join();
    }

    private void loadExistingClans() {
        Set<String> ids = CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                return querySet(connection, "SELECT `id` FROM `clans`");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor).join();

        for (String id : ids) {
            loadClan(id);
        }
    }

    // ==================================================================
    //  BaseSection dispatcher
    //  path shapes:
    //    ""                                  -> root (list of clan ids)
    //    "<clanId>"                          -> row in `clans`, dynamic columns
    //    "<clanId>.members"                  -> list of member uuids
    //    "<clanId>.members.<uuid>"           -> row in `clan_members`, dynamic columns
    //    "<clanId>.<group>[.<sub.path...>]"  -> generic EAV table "<group>_data",
    //                                            auto-created, arbitrary depth/keys
    // ==================================================================

    private final class SQLSection implements BaseSection {

        private final String path;

        private SQLSection(String path) {
            this.path = path;
        }

        private String child(String name) {
            return path.isEmpty() ? name : path + "." + name;
        }

        @Override
        public BaseSection of(Clan clan) {
            return new SQLSection(clan.getId());
        }

        @Override
        public BaseSection of(Clan clan, Member member) {
            return new SQLSection(clan.getId() + ".members." + member.getUuid());
        }

        @Override
        public BaseSection section(String name) {
            return new SQLSection(child(name));
        }

        @Override
        public CompletableFuture<Set<String>> keys() {
            return CompletableFuture.supplyAsync(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    Target t = Target.parse(path);
                    return switch (t.kind) {
                        case ROOT -> querySet(connection, "SELECT `id` FROM `clans`");
                        case MEMBERS_ROOT -> querySet(connection,
                                "SELECT `uuid` FROM `clan_members` WHERE `clan_id` = ?", t.clanId);
                        case CLAN -> columns(connection, "clans");
                        case MEMBER -> columns(connection, "clan_members");
                        case GROUP -> eavKeys(connection, t);
                        default -> throw new UnsupportedOperationException("keys() not supported at " + path);
                    };
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, executor);
        }

        @Override
        public CompletableFuture<Void> remove(String key) {
            return CompletableFuture.runAsync(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    Target t = Target.parse(path);
                    switch (t.kind) {
                        case ROOT:
                            deleteClanEverywhere(connection, key);
                            return;
                        case MEMBERS_ROOT:
                            execute(connection, "DELETE FROM `clan_members` WHERE `clan_id` = ? AND `uuid` = ?",
                                    t.clanId, key);
                            return;
                        case CLAN:
                            removeColumnValue(connection, "clans", "id", t.clanId, key);
                            return;
                        case MEMBER:
                            removeColumnValue(connection, "clan_members", "uuid", t.uuid, key);
                            return;
                        case GROUP:
                            eavRemove(connection, t, key);
                            return;
                        default:
                            throw new UnsupportedOperationException("remove() not supported at " + path);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, executor);
        }

        @Override
        public CompletableFuture<Void> set(String key, Object value) {
            return CompletableFuture.runAsync(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    validate(key);
                    Target t = Target.parse(path);
                    switch (t.kind) {
                        case CLAN:
                            upsertColumn(connection, "clans", "id", t.clanId, key, value);
                            return;
                        case MEMBER:
                            upsertColumn(connection, "clan_members", "uuid", t.uuid, key, value,
                                    "clan_id", t.clanId);
                            return;
                        case GROUP:
                            eavSet(connection, t, key, value);
                            return;
                        default:
                            throw new UnsupportedOperationException("set() not supported at " + path);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, executor);
        }

        @Override
        public CompletableFuture<Object> get(String key) {
            return CompletableFuture.supplyAsync(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    Target t = Target.parse(path);
                    return switch (t.kind) {
                        case CLAN -> readColumn(connection, "clans", "id", t.clanId, key);
                        case MEMBER -> readColumn(connection, "clan_members", "uuid", t.uuid, key);
                        case GROUP -> eavGet(connection, t, key);
                        default -> throw new UnsupportedOperationException("get() not supported at " + path);
                    };
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, executor);
        }

        @Override
        public CompletableFuture<String> getString(String key) {
            return get(key).thenApply(v -> v == null ? null : v.toString());
        }

        @Override
        public CompletableFuture<Integer> getInt(String key) {
            return get(key).thenApply(v -> v == null ? 0 : Integer.parseInt(v.toString()));
        }

        @Override
        public CompletableFuture<Double> getDouble(String key) {
            return get(key).thenApply(v -> v == null ? 0.0 : Double.parseDouble(v.toString()));
        }

        @Override
        public CompletableFuture<Long> getLong(String key) {
            return get(key).thenApply(v -> v == null ? 0L : Long.parseLong(v.toString()));
        }

        @Override
        public CompletableFuture<Boolean> getBoolean(String key) {
            return get(key).thenApply(v -> v != null && Boolean.parseBoolean(v.toString()));
        }

        @Override
        public CompletableFuture<List<?>> getList(String key) {
            return getStringList(key).thenApply(list -> (List<?>) list);
        }

        @Override
        public CompletableFuture<List<String>> getStringList(String key) {
            return getString(key).thenApply(raw ->
                    raw == null || raw.isEmpty() ? Collections.emptyList() : List.of(raw.split(",")));
        }
    }

    // ==================================================================
    //  path -> target resolution
    // ==================================================================

    private enum Kind {ROOT, CLAN, MEMBERS_ROOT, MEMBER, GROUP}

    private static final class Target {
        final Kind kind;
        final String clanId;
        final String uuid;
        final String group;     // e.g. "quests", "ranks", any non-"members" section name
        final String subPath;   // remaining dotted path inside the group's EAV table ("" = group root)

        private Target(Kind kind, String clanId, String uuid, String group, String subPath) {
            this.kind = kind;
            this.clanId = clanId;
            this.uuid = uuid;
            this.group = group;
            this.subPath = subPath;
        }

        static Target parse(String path) {
            if (path.isEmpty()) return new Target(Kind.ROOT, null, null, null, null);
            String[] seg = path.split("\\.", -1);
            if (seg.length == 1) return new Target(Kind.CLAN, seg[0], null, null, null);

            String clanId = seg[0];
            String second = seg[1];

            if ("members".equals(second)) {
                if (seg.length == 2) return new Target(Kind.MEMBERS_ROOT, clanId, null, null, null);
                if (seg.length == 3) return new Target(Kind.MEMBER, clanId, seg[2], null, null);
                throw new UnsupportedOperationException("members do not support nesting: " + path);
            }

            String rest = String.join(".", java.util.Arrays.copyOfRange(seg, 2, seg.length));
            return new Target(Kind.GROUP, clanId, null, second, rest);
        }
    }

    // ==================================================================
    //  clans / clan_members: dynamic-column handling
    // ==================================================================

    private Set<String> columns(Connection connection, String table) throws SQLException {
        return columnCache.computeIfAbsent(table, t -> {
            try {
                Set<String> cols = new LinkedHashSet<>();
                try (PreparedStatement stmt = connection.prepareStatement(
                        "SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS` " +
                                "WHERE `TABLE_SCHEMA` = ? AND `TABLE_NAME` = ?")) {
                    stmt.setString(1, databaseName);
                    stmt.setString(2, t);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) cols.add(rs.getString("COLUMN_NAME"));
                    }
                }
                return cols;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void ensureColumn(Connection connection, String table, String column) throws SQLException {
        validate(column);
        Set<String> cols = columns(connection, table);
        if (cols.contains(column)) return;
        execute(connection, "ALTER TABLE `" + table + "` ADD COLUMN `" + column + "` TEXT");
        cols.add(column);
    }

    private void upsertColumn(Connection connection, String table, String idColumn, String idValue,
                              String key, Object value, String... extraCols) throws SQLException {
        if (value == null) {
            removeColumnValue(connection, table, idColumn, idValue, key);
            return;
        }
        ensureColumn(connection, table, key);

        // ensure the row exists first (INSERT id [+ extra fixed cols] if missing)
        StringBuilder insertCols = new StringBuilder("`" + idColumn + "`");
        StringBuilder insertVals = new StringBuilder("?");
        Object[] insertParams = new Object[1 + extraCols.length / 2];
        insertParams[0] = idValue;
        for (int i = 0; i < extraCols.length; i += 2) {
            insertCols.append(", `").append(extraCols[i]).append("`");
            insertVals.append(", ?");
            insertParams[1 + i / 2] = extraCols[i + 1];
        }
        execute(connection, "INSERT IGNORE INTO `" + table + "` (" + insertCols + ") VALUES (" + insertVals + ")",
                insertParams);

        execute(connection, "UPDATE `" + table + "` SET `" + key + "` = ? WHERE `" + idColumn + "` = ?",
                serialize(value), idValue);
    }

    private Object readColumn(Connection connection, String table, String idColumn, String idValue, String key)
            throws SQLException {
        if (!columns(connection, table).contains(key)) return null; // never set -> not an error
        return queryValue(connection, "SELECT `" + key + "` FROM `" + table + "` WHERE `" + idColumn + "` = ?",
                idValue);
    }

    private void removeColumnValue(Connection connection, String table, String idColumn, String idValue, String key)
            throws SQLException {
        if (!columns(connection, table).contains(key)) return; // nothing to clear
        execute(connection, "UPDATE `" + table + "` SET `" + key + "` = NULL WHERE `" + idColumn + "` = ?", idValue);
    }

    // ==================================================================
    //  generic per-group EAV tables ("<group>_data"): arbitrary nesting/keys
    // ==================================================================

    private String eavTable(String group) {
        validate(group);
        String name = group.toLowerCase(Locale.ROOT).replace('-', '_');
        return name + "_data";
    }

    private void ensureEavTable(Connection connection, String table) throws SQLException {
        if (knownEavTables.contains(table)) return;
        execute(connection,
                "CREATE TABLE IF NOT EXISTS `" + table + "` (" +
                        "`clan_id` VARCHAR(191) NOT NULL, " +
                        "`path` VARCHAR(191) NOT NULL, " +
                        "`key_name` VARCHAR(191) NOT NULL, " +
                        "`value` MEDIUMTEXT, " +
                        "PRIMARY KEY (`clan_id`, `path`, `key_name`)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        knownEavTables.add(table);
    }

    private Set<String> eavKeys(Connection connection, Target t) throws SQLException {
        String table = eavTable(t.group);
        ensureEavTable(connection, table);

        Set<String> result = querySet(connection,
                "SELECT `key_name` FROM `" + table + "` WHERE `clan_id` = ? AND `path` = ?", t.clanId, t.subPath);

        String likePrefix = (t.subPath.isEmpty() ? "" : t.subPath + ".") + "%";
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT DISTINCT `path` FROM `" + table + "` WHERE `clan_id` = ? AND `path` LIKE ?")) {
            stmt.setString(1, t.clanId);
            stmt.setString(2, likePrefix);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String p = rs.getString(1);
                    String sub = t.subPath.isEmpty() ? p : p.substring(t.subPath.length() + 1);
                    if (sub.isEmpty()) continue;
                    int dot = sub.indexOf('.');
                    result.add(dot == -1 ? sub : sub.substring(0, dot));
                }
            }
        }
        return result;
    }

    private Object eavGet(Connection connection, Target t, String key) throws SQLException {
        String table = eavTable(t.group);
        ensureEavTable(connection, table);
        Object raw = queryValue(connection,
                "SELECT `value` FROM `" + table + "` WHERE `clan_id` = ? AND `path` = ? AND `key_name` = ?",
                t.clanId, t.subPath, key);
        return raw == null ? null : deserialize(raw);
    }

    private void eavSet(Connection connection, Target t, String key, Object value) throws SQLException {
        String table = eavTable(t.group);
        ensureEavTable(connection, table);

        if (value == null) {
            eavRemove(connection, t, key);
            return;
        }

        int updated = execute(connection,
                "UPDATE `" + table + "` SET `value` = ? WHERE `clan_id` = ? AND `path` = ? AND `key_name` = ?",
                serialize(value), t.clanId, t.subPath, key);
        if (updated == 0) {
            execute(connection,
                    "INSERT INTO `" + table + "` (`clan_id`, `path`, `key_name`, `value`) VALUES (?, ?, ?, ?)",
                    t.clanId, t.subPath, key, serialize(value));
        }
    }

    private void eavRemove(Connection connection, Target t, String key) throws SQLException {
        String table = eavTable(t.group);
        ensureEavTable(connection, table);

        execute(connection, "DELETE FROM `" + table + "` WHERE `clan_id` = ? AND `path` = ? AND `key_name` = ?",
                t.clanId, t.subPath, key);
        // if `key` was itself a whole nested sub-section, drop everything under it too
        String childPath = t.subPath.isEmpty() ? key : t.subPath + "." + key;
        execute(connection, "DELETE FROM `" + table + "` WHERE `clan_id` = ? AND (`path` = ? OR `path` LIKE ?)",
                t.clanId, childPath, childPath + ".%");
    }

    // ==================================================================
    //  clan-wide deletion (root.remove(clanId)) across all known + on-disk group tables
    // ==================================================================

    private void deleteClanEverywhere(Connection connection, String clanId) throws SQLException {
        execute(connection, "DELETE FROM `clan_members` WHERE `clan_id` = ?", clanId);
        execute(connection, "DELETE FROM `clans` WHERE `id` = ?", clanId);

        // sweep every auto-created group table, not just ones seen this session
        Set<String> tables;
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT `TABLE_NAME` FROM `INFORMATION_SCHEMA`.`TABLES` " +
                        "WHERE `TABLE_SCHEMA` = ? AND `TABLE_NAME` LIKE '%\\_data' ESCAPE '\\\\'")) {
            stmt.setString(1, databaseName);
            tables = new LinkedHashSet<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) tables.add(rs.getString(1));
            }
        }
        for (String table : tables) {
            execute(connection, "DELETE FROM `" + table + "` WHERE `clan_id` = ?", clanId);
        }
    }

    // ==================================================================
    //  identifier validation + serialization + low-level JDBC helpers
    // ==================================================================

    private static void validate(String identifier) {
        if (identifier == null || identifier.isEmpty() || !SAFE_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Unsafe storage key/section name: '" + identifier + "'");
        }
    }

    private static String serialize(Object value) {
        if (value instanceof ItemStack) {
            return "itemstack:" + itemStackToBase64((ItemStack) value);
        }
        return value.toString();
    }

    private static Object deserialize(Object raw) {
        String s = raw.toString();
        if (s.startsWith("itemstack:")) {
            return itemStackFromBase64(s.substring("itemstack:".length()));
        }
        return s;
    }

    private static String itemStackToBase64(ItemStack item) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(item);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ItemStack itemStackFromBase64(String raw) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(raw));
             ObjectInputStream ois = new ObjectInputStream(in)) {
            return (ItemStack) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int execute(Connection connection, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            return stmt.executeUpdate();
        }
    }

    private static Set<String> querySet(Connection connection, String sql, Object... params) throws SQLException {
        Set<String> result = new LinkedHashSet<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.add(rs.getString(1));
            }
        }
        return result;
    }

    private static Object queryValue(Connection connection, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getObject(1) : null;
            }
        }
    }
}