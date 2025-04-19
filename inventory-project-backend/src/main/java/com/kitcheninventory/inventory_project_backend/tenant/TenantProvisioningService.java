package com.kitcheninventory.inventory_project_backend.tenant;

import com.kitcheninventory.inventory_project_backend.schema.SchemaGenerator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class TenantProvisioningService {
    // @Autowired
    // // @Qualifier("multiTenantConnectionProvider")
    // // private final MultiTenantConnectionProvider<String> connectionProvider;
    // private final DataSource dataSource;

    // private SchemaGenerator schemaGenerator;

    // @Autowired
    // public TenantProvisioningService(SchemaGenerator schemaGenerator) {
    //     this.schemaGenerator = schemaGenerator;
    // }
    private final DataSource dataSource;
    private final SchemaGenerator schemaGenerator;

    @PostConstruct
    public void init() {
        // Optionally create a default tenant at startup
        createSchemaIfNotExists("public");
    }

    public synchronized void createSchemaIfNotExists(String tenantId) {
        if(!schemaExists(tenantId)){
            try{
                schemaGenerator.generateSchema(
                        tenantId
                );
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to create schema for tenant: " + tenantId, e);
            }
        } else {
            System.out.println("Schema " + tenantId + " already exists.");
        }
    }

    private boolean schemaExists(String schemaName) {
        try (Connection connection = dataSource.getConnection();
             ResultSet resultSet = connection.getMetaData().getSchemas()) {

            while (resultSet.next()) {
                if (schemaName.equals(resultSet.getString(1))) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check if schema exists: " + schemaName, e);
        }
        return false;
    }

    private String getEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Missing environment variable: " + key);
        }
        return value;
    }
}
