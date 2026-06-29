package org.jetby.clans.common.storage.trash.manager;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class DatabaseManager {

    protected HikariDataSource dataSource;

    public abstract void connect();

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}