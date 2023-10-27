package com.mromanak.multitenant.test.service;

import com.mromanak.multitenant.test.model.entity.Tenant;
import com.mromanak.multitenant.test.repository.TenantRepository;
import io.quantics.multitenant.tenantdetails.TenantSchemaDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * A service that performs basic CRUD operations for {@link Tenant Tenants}. Used by
 * {@code multitenant-oauth2-spring-boot-starter} to access information about the tenants that the application supports
 */
@Service
public class TenantService implements TenantSchemaDetailsService {

    private final TenantRepository repository;

    @Autowired
    public TenantService(TenantRepository repository) {
        this.repository = repository;
    }

    public Iterable<Tenant> getAll() {
        return repository.findAll();
    }

    public Optional<Tenant> getById(String id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Tenant> getByIssuer(String issuer) {
        return repository.findByIssuer(issuer);
    }

}
