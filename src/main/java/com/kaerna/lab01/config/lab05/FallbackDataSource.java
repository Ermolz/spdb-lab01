package com.kaerna.lab01.config.lab05;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class FallbackDataSource implements DataSource {

    private final DataSource primary;
    private final DataSource replica;

    public FallbackDataSource(DataSource replica, DataSource primary) {
        this.replica = replica;
        this.primary = primary;
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return replica.getConnection();
        } catch (SQLException e) {
            return primary.getConnection();
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        try {
            return replica.getConnection(username, password);
        } catch (SQLException e) {
            return primary.getConnection(username, password);
        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        throw new SQLException("DataSource of type " + getClass().getName() + " cannot be unwrapped as " + iface.getName());
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isInstance(this);
    }

    @Override
    public PrintWriter getLogWriter() {
        try {
            return primary.getLogWriter();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        try {
            primary.setLogWriter(out);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setLoginTimeout(int seconds) {
        try {
            primary.setLoginTimeout(seconds);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getLoginTimeout() {
        try {
            return primary.getLoginTimeout();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return primary.getParentLogger();
    }
}
