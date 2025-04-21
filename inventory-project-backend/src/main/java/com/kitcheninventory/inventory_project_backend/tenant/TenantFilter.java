package com.kitcheninventory.inventory_project_backend.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;

public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // get schema name from header: X-Tenant-ID
            String tenantId = request.getHeader("X-Tenant-ID");

            if (tenantId == null || tenantId.isBlank()) {
                System.out.println("[TenantFilter] Received bad request with null or blank X-Tenant-ID header");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); //400
                response.setContentType("application/json");
                response.getWriter().write("""
                    {
                      "error": "Missing required header: X-Tenant-ID"
                    }
                """);
                return; //stop filter chain here
            }

            TenantContext.setCurrentTenant(tenantId);
            System.out.println("[TenantFilter] New request with Tenant ID: " + tenantId);

            filterChain.doFilter(request, response);
        } finally {
            //clear threadlocal after request is finished processing
            TenantContext.clear();
        }
    }
}
