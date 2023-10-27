package com.mromanak.multitenant.test.repository;

import com.mromanak.multitenant.test.model.entity.Tenant;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * A basic {@link CrudRepository} for {@link Tenant Tenants}
 */
@Repository
public interface TenantRepository extends CrudRepository<Tenant, String> {

    Optional<Tenant> findByIssuer(String issuer);

}
