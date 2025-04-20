package com.kitcheninventory.inventory_project_backend.schema;

// import com.kitcheninventory.inventory_project_backend.dto.ReminderDTOMapperConfig;
import com.kitcheninventory.inventory_project_backend.model.*;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.EnumSet;

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
            System.out.println("[SchemaGenerator] Ensured schema created or exists: " + schemaName);

        } catch (Exception e) {
            throw new RuntimeException("[SchemaGenerator] Failed to ensure schema exists", e);
        }

        try (Connection conn = dataSource.getConnection()) {
            String url = conn.getMetaData().getURL();
            String user = conn.getMetaData().getUserName();

            System.out.println("[SchemaGenerator] Using DB URL: " + url);
            System.out.println("[SchemaGenerator] Using user: " + user);

            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                    .applySetting("hibernate.connection.driver_class", "org.postgresql.Driver")
                    .applySetting("hibernate.connection.url", url)
                    .applySetting("hibernate.connection.username", user)
                    .applySetting("hibernate.connection.password", pw)
                    .applySetting("hibernate.connection.init_sql", "SET search_path TO " + schemaName)
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
                    // .addAnnotatedClass(ReminderDTOMapperConfig.class)
                    .buildMetadata();

            SchemaExport schemaExport = new SchemaExport();
            schemaExport.setDelimiter(";");
            schemaExport.setFormat(true);
            schemaExport.setHaltOnError(true);
            schemaExport.setOutputFile("schema.sql");

            System.out.println("[SchemaGenerator] Starting schema export for schema: " + schemaName);
            schemaExport.execute(EnumSet.of(TargetType.DATABASE), SchemaExport.Action.CREATE, metadata);
            System.out.println("[SchemaGenerator] Schema export complete.");

        } catch (Exception e) {
            throw new RuntimeException("[SchemaGenerator] Failed to generate schema for: " + schemaName, e);
        }
    }
}
