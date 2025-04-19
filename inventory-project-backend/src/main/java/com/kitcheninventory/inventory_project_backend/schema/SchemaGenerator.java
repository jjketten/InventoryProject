package com.kitcheninventory.inventory_project_backend.schema;

import com.kitcheninventory.inventory_project_backend.model.*;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;

@Component
public class SchemaGenerator {

    @Autowired
    private DataSource dataSource;

    @Value("${spring.datasource.password}")
    private String pw;

        public void generateSchema(String schemaName) {
        try (Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()) {

            statement.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            System.out.println("Ensured schema exists: " + schemaName);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to ensure schema exists before export", e);
        }
        try (Connection conn = dataSource.getConnection()) {
            String url = conn.getMetaData().getURL();
            String user = conn.getMetaData().getUserName();

            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                .applySetting("hibernate.connection.url", url)
                .applySetting("hibernate.connection.username", user)
                .applySetting("hibernate.connection.password", pw) 
                .applySetting("hibernate.default_schema", schemaName)
                .applySetting("hibernate.multiTenancy", "SCHEMA")
                .build();

            Metadata metadata = new MetadataSources(serviceRegistry)
                .addAnnotatedClass(Item.class)
                .addAnnotatedClass(Category.class)
                .addAnnotatedClass(ItemCategory.class)
                .addAnnotatedClass(Recipe.class)
                .addAnnotatedClass(RecipeItem.class)
                .addAnnotatedClass(RecipeStep.class)
                .addAnnotatedClass(Reminder.class)
                .addAnnotatedClass(Purchase.class)
                .addAnnotatedClass(PurchaseItem.class)
                .buildMetadata();

            SchemaExport schemaExport = new SchemaExport();
            schemaExport.setDelimiter(";");
            schemaExport.setFormat(true);
            schemaExport.setHaltOnError(true); 
            schemaExport.setOutputFile("schema.sql"); 
            schemaExport.execute(EnumSet.of(TargetType.DATABASE), SchemaExport.Action.CREATE, metadata);
                
            // schemaExport.createOnly(EnumSet.of(TargetType.STDOUT), metadata); // just prints
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate schema for: " + schemaName, e);
        }
    }
}
