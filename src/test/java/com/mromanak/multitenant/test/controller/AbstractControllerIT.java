package com.mromanak.multitenant.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mromanak.multitenant.test.validation.ValidationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Implements utility methods for integration testing controllers
 */
public abstract class AbstractControllerIT {

    protected static final String ARQUEBUS_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2lkcC5leGFtcGxlLm9yZy9hcnF1ZWJ1cyIsImlhdCI6MTY2NDU4MjQwMCwiZXhwIjoyNTI0NjA4MDAwLCJzdWIiOiI0YWViYzRkMS0yNDFjLTQ5MzQtYjBiNi1lNjFmMGI1NmRjNzciLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsidXNlciJdfX0.MEbWdWKa3IA3d-B2K-VasgsCii9aGrWfKzIgWXbMVhU";
    protected static final String ARQUEBUS_ISSUER = "https://idp.example.org/arquebus";
    protected static final String BALAM_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2lkcC5leGFtcGxlLm9yZy9iYWxhbSIsImlhdCI6MTY2NDU4MjQwMCwiZXhwIjoyNTI0NjA4MDAwLCJzdWIiOiI0YWViYzRkMS0yNDFjLTQ5MzQtYjBiNi1lNjFmMGI1NmRjNzciLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsidXNlciJdfX0.Z4yF9uIcybJhdjPtWbfivAESe7m-RbncvnKPVW1ER2s";
    protected static final String BALAM_ISSUER = "https://idp.example.org/balam";
    protected static final String UNKNOWN_TENANT_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2lkcC5leGFtcGxlLm9yZy91bmtub3duLXRlbmFudCIsImlhdCI6MTY2NDU4MjQwMCwiZXhwIjoyNTI0NjA4MDAwLCJzdWIiOiI0YWViYzRkMS0yNDFjLTQ5MzQtYjBiNi1lNjFmMGI1NmRjNzciLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsidXNlciJdfX0.cg-zpABf4gaqyep1qyNh601RLe1zX6r2PaMJGxeCsFk";

    protected static final DateFormat ISO_8601_DATE_FORMATTER = new SimpleDateFormat(ValidationUtils.ISO_8601_DATE_FORMAT);

    protected final MockMvc mockMvc;
    protected final ObjectMapper objectMapper;

    protected AbstractControllerIT(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    // Can't apply @MockBean to constructor parameters
    protected abstract JwtDecoder getMockJwtDecoder();

    protected ResultActions sendGetRequest(String path, String token) throws Exception {
        return mockMvc.perform(get(path).with(bearerToken(token)));
    }

    protected ResultActions sendPostRequest(String path, Object requestBody, String token) throws Exception {
        return mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .with(bearerToken(token)));
    }

    protected ResultActions sendDeleteRequest(String path, String token) throws Exception {
        return mockMvc.perform(delete(path)
                .contentType(MediaType.APPLICATION_JSON)
                .with(bearerToken(token)));
    }

    protected <T> T getContentAs(MvcResult result, Class<T> targetClass) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), targetClass);
    }

    protected Date parseDateFromString(String dateString) throws Exception {
        return ISO_8601_DATE_FORMATTER.parse(dateString);
    }

    public void registerJwtWithRoles(String jwt, String issuerUrl, List<String> roles) {
        when(getMockJwtDecoder().decode(jwt))
                .thenReturn(new Jwt(jwt, Instant.now(), Instant.now().plusSeconds(60),
                        Map.of("alg", "HS256", "typ", "JWT"),
                        Map.of("realm_access", Map.of("roles", roles),
                                "iss", issuerUrl,
                                "sub", "e489ffe7-22d2-40ef-bde1-fd069ac8af0b")));
    }

    protected void registerArquebusJwt(List<String> roles) {
        registerJwtWithRoles(ARQUEBUS_JWT, ARQUEBUS_ISSUER, roles);
    }

    protected void registerBalamJwt(List<String> roles) {
        registerJwtWithRoles(BALAM_JWT, BALAM_ISSUER, roles);
    }

    protected void registerTestTenantJwts(List<String> roles) {
        registerArquebusJwt(roles);
        registerBalamJwt(roles);
    }

    protected ResultActions sendPutRequest(String path, Object requestBody, String token) throws Exception {
        return mockMvc.perform(put(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .with(bearerToken(token)));
    }

    protected static BearerTokenRequestPostProcessor bearerToken(String token) {
        return new BearerTokenRequestPostProcessor(token);
    }

    protected record BearerTokenRequestPostProcessor(String token) implements RequestPostProcessor {

        @Override
        @NonNull
        public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            return request;
        }

    }
}
