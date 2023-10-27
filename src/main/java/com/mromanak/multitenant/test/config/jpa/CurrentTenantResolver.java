package com.mromanak.multitenant.test.config.jpa;

import io.quantics.multitenant.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Allows the automatic multitenancy configuration to determine which tenant schema we should be querying against.
 * <p/>
 * The automatic configuration handles calling {@link TenantContext#setTenantId(String)} by itself, but I don't quite
 * follow why there isn't also something built-in to call {@link TenantContext#getTenantId()}} as well.
 */
@Component
public class CurrentTenantResolver implements CurrentTenantIdentifierResolver {

    public static final String DEFAULT_SCHEMA = "public";

    @Override
    @NonNull
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.getTenantId() != null
                ? TenantContext.getTenantId()
                : DEFAULT_SCHEMA;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

}
