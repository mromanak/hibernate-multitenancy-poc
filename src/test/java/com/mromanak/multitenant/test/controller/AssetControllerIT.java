package com.mromanak.multitenant.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mromanak.multitenant.test.model.dto.AssetDto;
import com.mromanak.multitenant.test.model.entity.Asset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"prod", "test"})
@AutoConfigureMockMvc()
@Sql(
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {
                "/create-test-tenants.sql",
                "/create-test-assets.sql"
        }
)
public class AssetControllerIT extends AbstractControllerIT {

    protected static final String ARQUEBUS_TEST_ASSET_ID = "7471da05-d4ba-4531-ab64-755b94c88635";
    protected static final String BALAM_TEST_ASSET_ID = "b74df32a-99c9-482c-87ca-eccc7013197f";

    @MockBean
    protected JwtDecoder mockJwtDecoder;

    @Autowired
    protected AssetControllerIT(MockMvc mockMvc, ObjectMapper objectMapper) {
        super(mockMvc, objectMapper);
    }

    @Override
    protected JwtDecoder getMockJwtDecoder() {
        return mockJwtDecoder;
    }

    @Nested
    @DisplayName("POST /asset")
    public class PostAssetTests {

        private static final String PATH = "/asset";

        @Test
        @DisplayName("Should return status 200 for a valid request")
        public void shouldReturn200ForValidRequest() throws Exception {
            registerAllTenantJwtsWithRoles(List.of("write:asset"));

            var arquebusRequestBody = new AssetDto();
            arquebusRequestBody.setName("V.I Freud");
            arquebusRequestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            arquebusRequestBody.setServerUrl("https://arquebus.space/locksmith");

            var balamRequestBody = new AssetDto();
            balamRequestBody.setName("G1 Michigan");
            balamRequestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            balamRequestBody.setServerUrl("https://arquebus.space/liger-tail");

            var arquebusMvcResult = sendPostRequestWithToken(PATH, arquebusRequestBody, ARQUEBUS_JWT)
                    .andExpect(status().is2xxSuccessful())
                    .andReturn();
            var arquebusResponseBody = getContentAs(arquebusMvcResult, Asset.class);

            assertThat(arquebusResponseBody.getId(), is(not(nullValue())));
            assertThat(arquebusResponseBody.getName(), is(equalTo(arquebusRequestBody.getName())));
            assertThat(arquebusResponseBody.getCreatedTimestamp(),
                    is(equalTo(parseDateFromString(arquebusRequestBody.getCreatedTimestamp()))));
            assertThat(arquebusResponseBody.getServerUrl().toString(), is(equalTo(arquebusRequestBody.getServerUrl())));

            var balamMvcResult = sendPostRequestWithToken(PATH, balamRequestBody, BALAM_JWT)
                    .andExpect(status().is2xxSuccessful())
                    .andReturn();
            var balamResponseBody = getContentAs(balamMvcResult, Asset.class);

            assertThat(balamResponseBody.getId(), is(not(nullValue())));
            assertThat(balamResponseBody.getName(), is(equalTo(balamRequestBody.getName())));
            assertThat(balamResponseBody.getCreatedTimestamp(),
                    is(equalTo(parseDateFromString(balamRequestBody.getCreatedTimestamp()))));
            assertThat(balamResponseBody.getServerUrl().toString(), is(equalTo(balamRequestBody.getServerUrl())));
        }

        @Test
        @DisplayName("Should return status 400 for an invalid request body")
        public void shouldReturn400ForInvalidRequestBody() throws Exception {
            registerArquebusJwtWithRoles(List.of("write:asset"));

            var requestBody = new AssetDto();
            requestBody.setName("");
            requestBody.setCreatedTimestamp("not-a-timestamp");
            requestBody.setServerUrl("not-a-url");

            sendPostRequestWithToken(PATH, requestBody, ARQUEBUS_JWT)
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "The provided request body was invalid",
                                "validationErrors": [
                                    {
                                        "field": "requestBody.name",
                                        "message": "size must be between 1 and 50"
                                    },
                                    {
                                        "field": "requestBody.name",
                                        "message": "must match \\"^\\\\p{javaLetter}[\\\\w\\\\s-_.]*$\\""
                                    },
                                    {
                                        "field": "requestBody.name",
                                        "message": "must not be empty"
                                    },
                                    {
                                        "field": "requestBody.createdTimestamp",
                                        "message": "must be a valid ISO-8601 date-time"
                                    },
                                    {
                                        "field": "requestBody.serverUrl",
                                        "message": "must be a valid URL"
                                    }
                                ]
                            }"""));
        }

        @Test
        @DisplayName("Should return status 401 for invalid authentication")
        // TODO: Implement documented error response body
        public void shouldReturn401ForInvalidAuthentication() throws Exception {
            var requestBody = new AssetDto();
            requestBody.setName("V.I Freud");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/locksmith");

            sendPostRequestWithToken(PATH, requestBody, UNKNOWN_TENANT_JWT)
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("WWW-Authenticate",
                            containsString("Bearer error=\"invalid_token\"")))
                    .andExpect(header().string("WWW-Authenticate",
                            containsString("Unknown tenant: https://idp.example.org/unknown-tenant")))
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should return status 403 for missing authentication")
        // TODO: Implement documented error response body
        public void shouldReturn403ForMissingAuthentication() throws Exception {
            registerAllTenantJwtsWithRoles(List.of("write:asset"));

            var requestBody = new AssetDto();
            requestBody.setName("V.I Freud");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/locksmith");

            mockMvc.perform(
                            post(PATH)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestBody))
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should return status 403 for inadequate permissions")
        // TODO: Implement documented error response body
        public void shouldReturn403ForInadequatePermissions() throws Exception {
            registerArquebusJwtWithRoles(List.of());

            var requestBody = new AssetDto();
            requestBody.setName("V.I Freud");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/locksmith");

            sendPostRequestWithToken(PATH, requestBody, ARQUEBUS_JWT)
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(""));
        }
    }

    @Nested
    @DisplayName("GET /asset")
    public class GetAssetTests {

        private static final String PATH = "/asset";

        @Test
        @DisplayName("Should return status 200 for a valid request")
        public void shouldReturn200ForValidRequest() throws Exception {
            registerAllTenantJwtsWithRoles(List.of("read:asset"));

            sendGetRequestWithToken(PATH, ARQUEBUS_JWT)
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().json("""
                            {
                                "content": [
                                    {
                                        "id": "7471da05-d4ba-4531-ab64-755b94c88635",
                                        "name": "V.IV Rusty",
                                        "createdTimestamp": "1970-01-01T00:00:00.000Z",
                                        "serverUrl": "https://arquebus.space/steel-haze"
                                    }
                                ],
                                "pageable": {
                                    "sort": {
                                        "empty": true,
                                        "sorted": false,
                                        "unsorted": true
                                    },
                                    "offset": 0,
                                    "pageNumber": 0,
                                    "pageSize": 25,
                                    "paged": true,
                                    "unpaged": false
                                },
                                "last": true,
                                "totalPages": 1,
                                "totalElements": 1,
                                "size": 25,
                                "numberOfElements": 1,
                                "first": true,
                                "number": 0,
                                "sort": {
                                    "empty": true,
                                    "sorted": false,
                                    "unsorted": true
                                },
                                "empty": false
                            }"""));

            sendGetRequestWithToken(PATH, BALAM_JWT)
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().json("""
                            {
                                "content": [
                                    {
                                        "id": "b74df32a-99c9-482c-87ca-eccc7013197f",
                                        "name": "G5 Iguazu",
                                        "createdTimestamp": "1970-01-01T00:00:00.000Z",
                                        "serverUrl": "https://balam.space/head-bringer"
                                    }
                                ],
                                "pageable": {
                                    "sort": {
                                        "empty": true,
                                        "sorted": false,
                                        "unsorted": true
                                    },
                                    "offset": 0,
                                    "pageNumber": 0,
                                    "pageSize": 25,
                                    "paged": true,
                                    "unpaged": false
                                },
                                "last": true,
                                "totalPages": 1,
                                "totalElements": 1,
                                "size": 25,
                                "numberOfElements": 1,
                                "first": true,
                                "number": 0,
                                "sort": {
                                    "empty": true,
                                    "sorted": false,
                                    "unsorted": true
                                },
                                "empty": false
                            }"""));
        }

        @Test
        @DisplayName("Should return status 400 for invalid URL parameters")
        public void shouldReturn400ForInvalidUrlParameters() throws Exception {
            registerArquebusJwtWithRoles(List.of("read:asset"));

            mockMvc.perform(
                            get(PATH).queryParam("pageNumber", "-1")
                                    .queryParam("pageSize", "1000000")
                                    .queryParam("sortField", "serverUrl")
                                    .queryParam("sortDirection", "sideways")
                                    .with(bearerToken(ARQUEBUS_JWT))
                    )
                    .andExpect(status().is(400))
                    .andExpect(content().json("""
                            {
                                "errorMessage": "The provided request parameters were invalid",
                                "validationErrors": [
                                    {
                                        "field": "getAssetPage.sortField",
                                        "message": "must be one of the enumerated sort fields for an Asset"
                                    },
                                    {
                                        "field": "getAssetPage.pageNumber",
                                        "message": "must be greater than or equal to 0"
                                    },
                                    {
                                        "field": "getAssetPage.sortDirection",
                                        "message": "must be one of the enumerated sort directions"
                                    },
                                    {
                                        "field": "getAssetPage.pageSize",
                                        "message": "must be less than or equal to 100"
                                    }
                                ]
                            }"""));

        }

        @Test
        @DisplayName("Should return status 401 for invalid authentication")
        // TODO: Implement documented error response body
        public void shouldReturn401ForInvalidAuthentication() throws Exception {
            sendGetRequestWithToken(PATH, UNKNOWN_TENANT_JWT)
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should return status 403 for missing authentication")
        // TODO: Implement documented error response body
        public void shouldReturn401ForMissingAuthentication() throws Exception {
            mockMvc.perform(get(PATH))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should return status 403 for inadequate permissions")
        // TODO: Implement documented error response body
        public void shouldReturn403ForInadequatePermissions() throws Exception {
            registerArquebusJwtWithRoles(Collections.emptyList());

            sendGetRequestWithToken(PATH, ARQUEBUS_JWT)
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(""));
        }
    }

    @Nested
    @DisplayName("GET /asset/{id}")
    public class GetAssetByIdTests {

        private static final String PATH_TEMPLATE = "/asset/%s";

        @Test
        @DisplayName("Should return status 200 for a valid request")
        public void shouldReturn200ForValidRequest() throws Exception {
            registerAllTenantJwtsWithRoles(List.of("read:asset"));

            sendGetRequestWithToken(PATH_TEMPLATE.formatted(ARQUEBUS_TEST_ASSET_ID), ARQUEBUS_JWT)
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().json("""
                            {
                                "id": "%s",
                                "name": "V.IV Rusty",
                                "createdTimestamp": "1970-01-01T00:00:00.000Z",
                                "serverUrl": "https://arquebus.space/steel-haze"
                            }""".formatted(ARQUEBUS_TEST_ASSET_ID)
                    ));

            sendGetRequestWithToken(PATH_TEMPLATE.formatted(BALAM_TEST_ASSET_ID), BALAM_JWT)
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().json("""
                            {
                                "id": "%s",
                                "name": "G5 Iguazu",
                                "createdTimestamp": "1970-01-01T00:00:00.000Z",
                                "serverUrl": "https://balam.space/head-bringer"
                            }""".formatted(BALAM_TEST_ASSET_ID)
                    ));
        }

        @Test
        @DisplayName("Should return status 400 for invalid URL parameters")
        public void shouldReturn400ForInvalidUrlParameters() throws Exception {
            registerAllTenantJwtsWithRoles(List.of("read:asset"));

            sendGetRequestWithToken(PATH_TEMPLATE.formatted("notAUuid"), ARQUEBUS_JWT)
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "The provided request parameters were invalid",
                                "validationErrors": [
                                    {
                                        "field": "getAsset.id",
                                        "message": "must be a valid UUID"
                                    }
                                ]
                            }"""
                    ));
        }

        @Test
        @DisplayName("Should return status 401 for invalid authentication")
        // TODO: Implement documented error response body
        public void shouldReturn401ForInvalidAuthentication() throws Exception {
            sendGetRequestWithToken(PATH_TEMPLATE.formatted(ARQUEBUS_TEST_ASSET_ID), UNKNOWN_TENANT_JWT)
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("WWW-Authenticate",
                            containsString("Bearer error=\"invalid_token\"")))
                    .andExpect(header().string("WWW-Authenticate",
                            containsString("Unknown tenant: https://idp.example.org/unknown-tenant")))
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should return status 401 for missing authentication")
        // TODO: Implement documented error response body
        public void shouldReturn401ForMissingAuthentication() throws Exception {
            mockMvc.perform(get(PATH_TEMPLATE.formatted(ARQUEBUS_TEST_ASSET_ID)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should return status 403 for inadequate permissions")
        // TODO: Implement documented error response body
        public void shouldReturn403ForInadequatePermissions() throws Exception {
            registerArquebusJwtWithRoles(Collections.emptyList());

            var path = "/asset/%s".formatted(ARQUEBUS_TEST_ASSET_ID);

            sendGetRequestWithToken(path, ARQUEBUS_JWT)
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should return status 404 if entity with ID does not exist")
        // TODO: Implement documented error response body
        public void shouldReturn404ForNonexistentId() throws Exception {
            registerArquebusJwtWithRoles(List.of("read:asset"));

            var nonexistentId = "4776b07b-3018-41b7-898b-3a97c07db8d8";

            sendGetRequestWithToken(PATH_TEMPLATE.formatted(nonexistentId), ARQUEBUS_JWT)
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "No Asset entity found with ID %s"
                            }""".formatted(nonexistentId)
                    ));
        }

        @Test
        @DisplayName("Should return status 404 if entity with ID exists in different tenant")
        public void shouldReturn404ForCrossTenantId() throws Exception {
            registerAllTenantJwtsWithRoles(List.of("read:asset"));

            sendGetRequestWithToken(PATH_TEMPLATE.formatted(ARQUEBUS_TEST_ASSET_ID), BALAM_JWT)
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "No Asset entity found with ID %s"
                            }""".formatted(ARQUEBUS_TEST_ASSET_ID)
                    ));

            sendGetRequestWithToken(PATH_TEMPLATE.formatted(BALAM_TEST_ASSET_ID), ARQUEBUS_JWT)
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "No Asset entity found with ID %s"
                            }""".formatted(BALAM_TEST_ASSET_ID)
                    ));
        }
    }

    @Nested
    @DisplayName("PUT /asset/{id}")
    public class PutAssetTests {

        private static final String PATH_TEMPLATE = "/asset/%s";

        @Test
        @DisplayName("Should return status 200 for a valid request")
        public void shouldReturn200ForValidRequest() throws Exception {
            registerAllTenantJwtsWithRoles(List.of("write:asset"));

            var arquebusRequestBody = new AssetDto();
            arquebusRequestBody.setName("V.IV Rusty");
            arquebusRequestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            arquebusRequestBody.setServerUrl("https://arquebus.space/steel-haze-ortus");

            var balamRequestBody = new AssetDto();
            balamRequestBody.setName("G5 Iguazu");
            balamRequestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            balamRequestBody.setServerUrl("https://balam.space/mind-gamma");

            sendPutRequestWithToken(PATH_TEMPLATE.formatted(ARQUEBUS_TEST_ASSET_ID), arquebusRequestBody, ARQUEBUS_JWT)
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().json("""
                            {
                                "id": "%s",
                                "name": "%s",
                                "createdTimestamp": "%s",
                                "serverUrl": "%s"
                            }"""
                            .formatted(
                                    ARQUEBUS_TEST_ASSET_ID,
                                    arquebusRequestBody.getName(),
                                    arquebusRequestBody.getCreatedTimestamp(),
                                    arquebusRequestBody.getServerUrl()
                            )));

            sendPutRequestWithToken(PATH_TEMPLATE.formatted(BALAM_TEST_ASSET_ID), balamRequestBody, BALAM_JWT)
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().json("""
                            {
                                "id": "%s",
                                "name": "%s",
                                "createdTimestamp": "%s",
                                "serverUrl": "%s"
                            }"""
                            .formatted(
                                    BALAM_TEST_ASSET_ID,
                                    balamRequestBody.getName(),
                                    balamRequestBody.getCreatedTimestamp(),
                                    balamRequestBody.getServerUrl()
                            )));
        }

        @Test
        @DisplayName("Should return status 400 for invalid URL parameters")
        public void shouldReturn400ForInvalidUrlParameters() throws Exception {
            registerArquebusJwtWithRoles(List.of("write:asset"));

            var requestBody = new AssetDto();
            requestBody.setName("V.IV Rusty");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/steel-haze-ortus");

            sendPutRequestWithToken(PATH_TEMPLATE.formatted("notAUuid"), requestBody, ARQUEBUS_JWT)
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "The provided request parameters were invalid",
                                "validationErrors": [
                                    {
                                        "field": "updateAsset.id",
                                        "message": "must be a valid UUID"
                                    }
                                ]
                            }"""));
        }

        @Test
        @DisplayName("Should return status 400 for an invalid request body")
        public void shouldReturn400ForInvalidRequestBody() throws Exception {
            registerArquebusJwtWithRoles(List.of("write:asset"));

            var requestBody = new AssetDto();
            requestBody.setName("");
            requestBody.setCreatedTimestamp("notATimestamp");
            requestBody.setServerUrl("notAUrl");

            sendPutRequestWithToken(PATH_TEMPLATE.formatted(ARQUEBUS_TEST_ASSET_ID), requestBody, ARQUEBUS_JWT)
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "The provided request body was invalid",
                                "validationErrors": [
                                    {
                                        "field": "requestBody.name",
                                        "message": "size must be between 1 and 50"
                                    },
                                    {
                                        "field": "requestBody.name",
                                        "message": "must match \\"^\\\\p{javaLetter}[\\\\w\\\\s-_.]*$\\""
                                    },
                                    {
                                        "field": "requestBody.name",
                                        "message": "must not be empty"
                                    },
                                    {
                                        "field": "requestBody.createdTimestamp",
                                        "message": "must be a valid ISO-8601 date-time"
                                    },
                                    {
                                        "field": "requestBody.serverUrl",
                                        "message": "must be a valid URL"
                                    }
                                ]
                            }"""));
        }

        @Test
        @DisplayName("Should return status 401 for invalid authentication")
        // TODO: Implement documented error response body
        public void shouldReturn401ForInvalidAuthentication() throws Exception {
            var requestBody = new AssetDto();
            requestBody.setName("V.IV Rusty");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/steel-haze-ortus");

            sendPutRequestWithToken(PATH_TEMPLATE.formatted(ARQUEBUS_TEST_ASSET_ID), requestBody, UNKNOWN_TENANT_JWT)
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("WWW-Authenticate",
                            containsString("Bearer error=\"invalid_token\"")))
                    .andExpect(header().string("WWW-Authenticate",
                            containsString("Unknown tenant: https://idp.example.org/unknown-tenant")))
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should return status 403 for missing authentication")
        // TODO: Implement documented error response body
        public void shouldReturn403ForMissingAuthentication() throws Exception {
            var requestBody = new AssetDto();
            requestBody.setName("V.IV Rusty");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/steel-haze-ortus");

            mockMvc.perform(put(PATH_TEMPLATE.formatted(ARQUEBUS_TEST_ASSET_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should return status 403 for inadequate permissions")
        // TODO: Implement documented error response body
        public void shouldReturn403ForInadequatePermissions() throws Exception {
            registerArquebusJwtWithRoles(Collections.emptyList());

            var requestBody = new AssetDto();
            requestBody.setName("V.IV Rusty");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/steel-haze-ortus");

            sendPutRequestWithToken(PATH_TEMPLATE.formatted(ARQUEBUS_TEST_ASSET_ID), requestBody, ARQUEBUS_JWT)
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should return status 404 if entity with ID does not exist")
        public void shouldReturn404ForNonexistentId() throws Exception {
            registerArquebusJwtWithRoles(List.of("write:asset"));

            var nonexistentId = "bc6a0ff7-793b-4d4d-986a-9dcc895bbe12";

            var requestBody = new AssetDto();
            requestBody.setName("V.I Freud");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/locksmit");

            sendPutRequestWithToken(PATH_TEMPLATE.formatted(nonexistentId), requestBody, ARQUEBUS_JWT)
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "No Asset entity found with ID %s"
                            }""".formatted(nonexistentId)
                    ));
        }

        @Test
        @DisplayName("Should return status 404 if entity with ID exists in different tenant")
        public void shouldReturn404ForCrossTenantId() throws Exception {
            registerAllTenantJwtsWithRoles(List.of("write:asset"));

            var arquebusRequestBody = new AssetDto();
            arquebusRequestBody.setName("ARQUEBUS RULES");
            arquebusRequestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            arquebusRequestBody.setServerUrl("https://arquebus.space/balam-drools");

            var balamRequestBody = new AssetDto();
            balamRequestBody.setName("NO U");
            balamRequestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            balamRequestBody.setServerUrl("https://balam.space/you-are-the-silly-man");

            sendPutRequestWithToken(PATH_TEMPLATE.formatted(BALAM_TEST_ASSET_ID), arquebusRequestBody, ARQUEBUS_JWT)
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "No Asset entity found with ID %s"
                            }""".formatted(BALAM_TEST_ASSET_ID)
                    ));

            sendPutRequestWithToken(PATH_TEMPLATE.formatted(ARQUEBUS_TEST_ASSET_ID), balamRequestBody, BALAM_JWT)
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "No Asset entity found with ID %s"
                            }""".formatted(ARQUEBUS_TEST_ASSET_ID)
                    ));
        }
    }

    @Nested
    @DisplayName("DELETE /asset/{id}")
    public class DeleteAssetTests {

        private static final String PATH_TEMPLATE = "/asset/%s";

        @Test
        @DisplayName("Should return status 204 for a valid request")
        public void shouldReturn204ForValidRequest() throws Exception {
            registerAllTenantJwtsWithRoles(List.of("write:asset"));

            sendDeleteRequest(PATH_TEMPLATE.formatted(ARQUEBUS_TEST_ASSET_ID), ARQUEBUS_JWT)
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().string(""));

            sendDeleteRequest(PATH_TEMPLATE.formatted(BALAM_TEST_ASSET_ID), BALAM_JWT)
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should return status 400 for invalid URL parameters")
        public void shouldReturn400ForInvalidUrlParameters() throws Exception {
            registerArquebusJwtWithRoles(List.of("write:asset"));

            sendDeleteRequest(PATH_TEMPLATE.formatted("notAUuid"), ARQUEBUS_JWT)
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "The provided request parameters were invalid",
                                "validationErrors": [
                                    {
                                        "field": "deleteAsset.id",
                                        "message": "must be a valid UUID"
                                    }
                                ]
                            }"""
                    ));
        }

        @Test
        @DisplayName("Should return status 401 for invalid authentication")
        // TODO: Implement documented error response body
        public void shouldReturn401ForInvalidAuthentication() throws Exception {
            sendDeleteRequest(PATH_TEMPLATE.formatted(ARQUEBUS_TEST_ASSET_ID), UNKNOWN_TENANT_JWT)
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("WWW-Authenticate",
                            containsString("Bearer error=\"invalid_token\"")))
                    .andExpect(header().string("WWW-Authenticate",
                            containsString("Unknown tenant: https://idp.example.org/unknown-tenant")))
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should return status 403 for missing authentication")
        // TODO: Implement documented error response body
        public void shouldReturn403ForMissingAuthentication() throws Exception {
            mockMvc.perform(delete(PATH_TEMPLATE.formatted(ARQUEBUS_TEST_ASSET_ID)))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should return status 403 for inadequate permissions")
        // TODO: Implement documented error response body
        public void shouldReturn403ForInadequatePermissions() throws Exception {
            registerArquebusJwtWithRoles(Collections.emptyList());

            sendDeleteRequest(PATH_TEMPLATE.formatted(ARQUEBUS_TEST_ASSET_ID), ARQUEBUS_JWT)
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should return status 404 if entity with ID does not exist")
        public void shouldReturn404ForNonexistentId() throws Exception {
            registerArquebusJwtWithRoles(List.of("write:asset"));

            var nonexistentId = "bb4a7634-3563-42d6-b946-c16808d1ec4c";

            sendDeleteRequest(PATH_TEMPLATE.formatted(nonexistentId), ARQUEBUS_JWT)
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "No Asset entity found with ID %s"
                            }""".formatted(nonexistentId)
                    ));
        }

        @Test
        @DisplayName("Should return status 404 if entity with ID exists in different tenant")
        public void shouldReturn404ForCrossTenantId() throws Exception {
            registerAllTenantJwtsWithRoles(List.of("write:asset"));

            sendDeleteRequest(PATH_TEMPLATE.formatted(ARQUEBUS_TEST_ASSET_ID), BALAM_JWT)
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "No Asset entity found with ID %s"
                            }""".formatted(ARQUEBUS_TEST_ASSET_ID)
                    ));

            sendDeleteRequest(PATH_TEMPLATE.formatted(BALAM_TEST_ASSET_ID), ARQUEBUS_JWT)
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "No Asset entity found with ID %s"
                            }""".formatted(BALAM_TEST_ASSET_ID)
                    ));
        }
    }
}