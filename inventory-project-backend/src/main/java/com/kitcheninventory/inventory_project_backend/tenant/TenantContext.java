package com.kitcheninventory.inventory_project_backend.tenant;

/**
 * Thread-local context holder for the current tenant (schema) identifier.
 * 
 * This allows each request to dynamically define which schema should be used
 * during that request's lifecycle. Typically set at the beginning of a request,
 * e.g., in a filter or controller interceptor.
 */


public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setCurrentTenant(String tenant) {
        currentTenant.set(tenant);
    }

    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}
