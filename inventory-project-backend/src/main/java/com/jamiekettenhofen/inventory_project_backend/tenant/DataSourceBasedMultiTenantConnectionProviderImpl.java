package com.jamiekettenhofen.inventory_project_backend.tenant;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Custom implementation of Hibernate's MultiTenantConnectionProvider interface
 * for schema-based multi-tenancy.
 * 
 * This class provides database connections and sets the appropriate schema
 * (based on the current tenant identifier) before queries are executed.
 * 
 * It also resets the schema to the default ("public") after releasing connections.
 */


public class DataSourceBasedMultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    public DataSourceBasedMultiTenantConnectionProviderImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = getAnyConnection();
        connection.setSchema(tenantIdentifier);
        return connection;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        connection.setSchema("public");
        connection.close();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.setSchema("public");
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
} 
