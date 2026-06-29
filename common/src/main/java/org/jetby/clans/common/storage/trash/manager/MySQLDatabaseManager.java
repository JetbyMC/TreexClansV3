package org.jetby.clans.common.storage.trash.manager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

public class MySQLDatabaseManager extends DatabaseManager {

    private final FileConfiguration cfg;

    public MySQLDatabaseManager(FileConfiguration cfg) {
        this.cfg = cfg;
    }

    public void connect() {

        String host = cfg.getString("storage.host", "localhost");
        String port = cfg.getString("storage.port", "3306");
        String database = cfg.getString("storage.database", "treexclans");
        String username = cfg.getString("storage.username", "root");
        String password = cfg.getString("storage.password", "");

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&autoReconnect=true&characterEncoding=utf8");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        dataSource = new HikariDataSource(config);
    }
}