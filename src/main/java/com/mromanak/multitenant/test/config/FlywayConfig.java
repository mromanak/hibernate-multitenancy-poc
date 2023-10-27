package com.mromanak.multitenant.test.config;

import com.mromanak.multitenant.test.config.jpa.CurrentTenantResolver;
import io.quantics.multitenant.tenantdetails.TenantSchemaDetailsService;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;

/**
 * Configures Flyway to maintain two sets of database migration scripts, which are applied in this order:
 * <ul>
 *     <li>Flyway migration files in {@code src/main/resources/db/migration/default} are applied to the default
 *     schema</li>
 *     <li>Flyway migration files in {@code src/main/resources/db/migration/tenants} are applied to each tenant
 *     schema</li>
 * </ul>
 */
@Configuration
@ConditionalOnProperty(prefix = "spring", name = "flyway.enabled", matchIfMissing = true)
public class FlywayConfig {

    private final boolean outOfOrder;
    private final boolean baselineOnMigrate;

    public FlywayConfig(@Value("${spring.flyway.out-of-order:false}") boolean outOfOrder,
                        @Value("${spring.flyway.baseline-on-migrate:false}") boolean baselineOnMigrate) {
        this.outOfOrder = outOfOrder;
        this.baselineOnMigrate = baselineOnMigrate;
    }

    @Bean
    @Order(1)
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .outOfOrder(outOfOrder)
                .baselineOnMigrate(baselineOnMigrate)
                .locations("db/migration/default")
                .dataSource(dataSource)
                .schemas(CurrentTenantResolver.DEFAULT_SCHEMA)
                .load();
        flyway.migrate();
        return flyway;
    }

    @Bean
    @Order(2)
    @ConditionalOnProperty(prefix = "spring", name = "flyway.repair-on-migrate", havingValue = "false",
            matchIfMissing = true)
    public Boolean tenantsFlyway(TenantSchemaDetailsService tenantService, DataSource dataSource) {
        tenantService.getAll().forEach(tenant -> {
            Flyway flyway = Flyway.configure()
                    .outOfOrder(outOfOrder)
                    .baselineOnMigrate(baselineOnMigrate)
                    .locations("db/migration/tenants")
                    .dataSource(dataSource)
                    .schemas(tenant.getSchema())
                    .load();
            flyway.migrate();
        });
        return true;
    }

}
