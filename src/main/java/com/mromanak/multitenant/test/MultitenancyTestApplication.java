package com.mromanak.multitenant.test;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Declares:
 * <ol>
 *     <li>The main method used to start the application</li>
 *     <li>Any globally-defined OpenAPI components for the application</li>
 * </ol>
 */
@SpringBootApplication
@SecuritySchemes({
        @SecurityScheme(
                name = "OIDC",
                type = SecuritySchemeType.OPENIDCONNECT,
                openIdConnectUrl = "http://localhost:9090/realms/{tenant}/.well-known/openid-configuration"
        )
})
public class MultitenancyTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultitenancyTestApplication.class, args);
    }

}
