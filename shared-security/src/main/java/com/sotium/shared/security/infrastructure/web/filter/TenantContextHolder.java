package com.sotium.shared.security.infrastructure.web.filter;

import com.sotium.shared.security.domain.model.TenantContext;

import java.util.Optional;

public class TenantContextHolder {

    private final ThreadLocal<TenantContext> tenantContext = new ThreadLocal<>();

    public void set(final TenantContext context) {
        tenantContext.set(context);
    }

    public Optional<TenantContext> get() {
        return Optional.ofNullable(tenantContext.get());
    }

    public void clear() {
        tenantContext.remove();
    }
}
