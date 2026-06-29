package org.jetby.clans.common.storage.trash.manager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;

public class SQLiteDatabaseManager extends DatabaseManager {

    private final File dbFile;

    public SQLiteDatabaseManager(File dbFile) {
        this.dbFile = dbFile;
    }

    @Override
    public void connect() {

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setMaximumPoolSize(1);
        config.setConnectionTimeout(30000);

        dataSource = new HikariDataSource(config);
    }
}
