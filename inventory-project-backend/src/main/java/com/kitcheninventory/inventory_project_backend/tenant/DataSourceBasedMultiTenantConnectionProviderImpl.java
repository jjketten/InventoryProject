package com.kitcheninventory.inventory_project_backend.tenant;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.tool.schema.TargetType;

import com.kitcheninventory.inventory_project_backend.model.ItemCategory;
import com.kitcheninventory.inventory_project_backend.model.Purchase;
import com.kitcheninventory.inventory_project_backend.model.PurchaseItem;
import com.kitcheninventory.inventory_project_backend.model.Recipe;
import com.kitcheninventory.inventory_project_backend.model.RecipeItem;
import com.kitcheninventory.inventory_project_backend.model.Reminder;
import com.kitcheninventory.inventory_project_backend.model.Item;
import com.kitcheninventory.inventory_project_backend.model.Category;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumSet;

import org.hibernate.tool.schema.TargetType;
import org.hibernate.tool.schema.spi.SchemaManagementTool;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
// import org.hibernate.tool.schema.internal.SchemaExport;

import java.util.EnumSet;
import java.util.Map;

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

    // private void createSchema(String tenantIdentifier) {
    //     ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
    //         .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
    //         .build();

    //     Metadata metadata = new MetadataSources(serviceRegistry)
    //         .addAnnotatedClass(Item.class)
    //         .addAnnotatedClass(Category.class)
    //         .addAnnotatedClass(ItemCategory.class)
    //         .addAnnotatedClass(Recipe.class)
    //         .addAnnotatedClass(RecipeItem.class)
    //         .addAnnotatedClass(Reminder.class)
    //         .addAnnotatedClass(Purchase.class)
    //         .addAnnotatedClass(PurchaseItem.class)
    //         // 👆 Include all your entities
    //         .buildMetadata();

    //     metadata.getDatabase().getDefaultNamespace().setName(tenantIdentifier);

    //     new SchemaExport()
    //         .setDelimiter(";")
    //         .setFormat(true)
    //         .create(EnumSet.of(TargetType.DATABASE), metadata);
    // }


    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        // if (!schemaExists(tenantIdentifier)) {
        //     createSchema(tenantIdentifier);
        // }

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
