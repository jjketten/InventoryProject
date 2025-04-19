package com.kitcheninventory.inventory_project_backend.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;


/**
 * Resolves the current tenant identifier (i.e., schema name) for Hibernate at runtime.
 * 
 * It reads the schema identifier from a thread-local context (TenantContext).
 * If no tenant is set, it defaults to the "public" schema.
 *
 * Used in Hibernate's schema-based multi-tenant strategy to route queries.
 */


public class SchemaTenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    private static final String DEFAULT_TENANT = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.getCurrentTenant();
        return (tenant != null && !tenant.isBlank()) ? tenant : DEFAULT_TENANT;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}