package com.kitcheninventory.inventory_project_backend.tenant;

import com.kitcheninventory.inventory_project_backend.tenant.TenantProvisioningService;
import com.kitcheninventory.inventory_project_backend.tenant.TenantContext;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class DataSourceBasedMultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;
    private final TenantProvisioningService tenantProvisioningService;

    @Autowired
    public DataSourceBasedMultiTenantConnectionProviderImpl(
            DataSource dataSource,
            TenantProvisioningService tenantProvisioningService) {
        this.dataSource = dataSource;
        this.tenantProvisioningService = tenantProvisioningService;
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {

        tenantProvisioningService.createSchemaIfNotExists(tenantIdentifier); //why are we checking the schema existence twice


        Connection connection = dataSource.getConnection();
        connection.setSchema(tenantIdentifier); // set schema for current tenant
        return connection;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        connection.setSchema("public"); // reset schema
        connection.close();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.setSchema("public"); // reset schema
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
