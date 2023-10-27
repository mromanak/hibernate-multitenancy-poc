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
    @DisplayName("POST operation")
    public class PostOperationTests {

        @Test
        @DisplayName("should validate request body")
        public void postShouldValidateRequestBody() throws Exception {
            registerTestTenantJwts(List.of("write:asset"));

            var path = "/asset";

            var requestBody = new AssetDto();
            requestBody.setName("");
            requestBody.setCreatedTimestamp("not-a-timestamp");
            requestBody.setServerUrl("not-a-url");

            sendPostRequest(path, requestBody, ARQUEBUS_JWT)
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
        @DisplayName("should reject a request from an unknown tenant")
        public void postShouldUpdateRejectUpdateForUnknownTenant() throws Exception {
            registerTestTenantJwts(List.of("write:asset"));

            var path = "/asset/%s".formatted(ARQUEBUS_TEST_ASSET_ID);

            var requestBody = new AssetDto();
            requestBody.setName("V.IV Rusty");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/steel-haze-ortus");

            sendPostRequest(path, requestBody, UNKNOWN_TENANT_JWT)
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("WWW-Authenticate", containsString("Bearer error=\"invalid_token\"")))
                    .andExpect(header().string("WWW-Authenticate", containsString("Unknown tenant: https://idp.example.org/unknown-tenant")))
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should reject an update with no authorization")
        public void postShouldUpdateRejectUpdateWithNoAuthorization() throws Exception {
            registerTestTenantJwts(List.of("write:asset"));

            var path = "/asset";

            var requestBody = new AssetDto();
            requestBody.setName("V.IV Rusty");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/steel-haze-ortus");

            mockMvc.perform(post(path)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should reject a request from a user without the write:asset role")
        public void postShouldUpdateRejectRequestFromUserWithoutWriteAccess() throws Exception {
            registerTestTenantJwts(Collections.emptyList());

            var path = "/asset";

            var requestBody = new AssetDto();
            requestBody.setName("V.IV Rusty");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/steel-haze-ortus");

            mockMvc.perform(post(path)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should create new Asset for a properly authorized request")
        public void postShouldUpdateAnExistingAsset() throws Exception {
            registerTestTenantJwts(List.of("write:asset"));

            var path = "/asset";

            var requestBody = new AssetDto();
            requestBody.setName("V.IV Rusty");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/steel-haze-ortus");

            var mvcResult = sendPostRequest(path, requestBody, ARQUEBUS_JWT)
                    .andExpect(status().is2xxSuccessful())
                    .andReturn();
            var responseBody = getContentAs(mvcResult, Asset.class);

            assertThat(responseBody.getId(), is(not(nullValue())));
            assertThat(responseBody.getName(), is(equalTo(requestBody.getName())));
            assertThat(responseBody.getCreatedTimestamp(), is(equalTo(parseDateFromString(requestBody.getCreatedTimestamp()))));
            assertThat(responseBody.getServerUrl().toString(), is(equalTo(requestBody.getServerUrl())));
        }
    }

    @Nested
    @DisplayName("GET operation")
    public class GetOperationTests {

        @Test
        @DisplayName("should validate request parameters")
        public void getShouldValidateRequestBody() throws Exception {
             registerTestTenantJwts(List.of("read:asset"));

            var path = "/asset/not-a-uuid";

            sendGetRequest(path, ARQUEBUS_JWT)
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
        @DisplayName("should reject a request from an unknown tenant")
        public void getShouldUpdateRejectUpdateForUnknownTenant() throws Exception {
             registerTestTenantJwts(List.of("read:asset"));

            var path = "/asset/%s".formatted(ARQUEBUS_TEST_ASSET_ID);

            sendGetRequest(path, UNKNOWN_TENANT_JWT)
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("WWW-Authenticate", containsString("Bearer error=\"invalid_token\"")))
                    .andExpect(header().string("WWW-Authenticate", containsString("Unknown tenant: https://idp.example.org/unknown-tenant")))
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should reject a request with no authorization")
        public void getShouldUpdateRejectUpdateWithNoAuthorization() throws Exception {
             registerTestTenantJwts(List.of("read:asset"));

            var path = "/asset/%s".formatted(ARQUEBUS_TEST_ASSET_ID);

            mockMvc.perform(get(path))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should reject an request from a user without the read:asset role")
        public void getShouldUpdateRejectRequestFromUserWithoutReadAccess() throws Exception {
             registerTestTenantJwts(Collections.emptyList());

            var path = "/asset/%s".formatted(ARQUEBUS_TEST_ASSET_ID);

            sendGetRequest(path, ARQUEBUS_JWT)
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should not return Assets from other tenants")
        public void getShouldNotReturnAssetsFromOtherTenants() throws Exception {
             registerTestTenantJwts(List.of("read:asset"));

            var arquebusPath = "/asset/%s".formatted(ARQUEBUS_TEST_ASSET_ID);
            var balamPath = "/asset/%s".formatted(BALAM_TEST_ASSET_ID);

            sendGetRequest(arquebusPath, BALAM_JWT)
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "No Asset entity found with ID %s"
                            }""".formatted(ARQUEBUS_TEST_ASSET_ID)
                    ));

            sendGetRequest(balamPath, ARQUEBUS_JWT)
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "No Asset entity found with ID %s"
                            }""".formatted(BALAM_TEST_ASSET_ID)
                    ));
        }

        @Test
        @DisplayName("should return the requested Asset for a properly authorized request")
        public void getShouldReturnAssetsFromAuthorizedTenant() throws Exception {
             registerTestTenantJwts(List.of("read:asset"));

            var arquebusPath = "/asset/%s".formatted(ARQUEBUS_TEST_ASSET_ID);
            var balamPath = "/asset/%s".formatted(BALAM_TEST_ASSET_ID);

            sendGetRequest(arquebusPath, ARQUEBUS_JWT)
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().json("""
                            {
                                "id": "%s",
                                "name": "V.IV Rusty",
                                "createdTimestamp": "1970-01-01T00:00:00.000Z",
                                "serverUrl": "https://arquebus.space/steel-haze"
                            }""".formatted(ARQUEBUS_TEST_ASSET_ID)
                    ));

            sendGetRequest(balamPath, BALAM_JWT)
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().json("""
                            {
                                "id": "%s",
                                "name": "G5 Iguazu",
                                "createdTimestamp": "1970-01-01T00:00:00.000Z",
                                "serverUrl": "https://balam.space/mind-gamma"
                            }
                            """.formatted(BALAM_TEST_ASSET_ID)
                    ));
        }
    }

    @Nested
    @DisplayName("PUT operation")
    public class PutOperationTests {

        @Test
        @DisplayName("should validate request body")
        public void putShouldValidateRequestBody() throws Exception {
             registerTestTenantJwts(List.of("write:asset"));

            var path = "/asset/%s".formatted(ARQUEBUS_TEST_ASSET_ID);

            var requestBody = new AssetDto();
            requestBody.setName("");
            requestBody.setCreatedTimestamp("not-a-timestamp");
            requestBody.setServerUrl("not-a-url");

            sendPutRequest(path, requestBody, ARQUEBUS_JWT)
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
        @DisplayName("should reject a request to a non-existent Asset")
        public void putShouldUpdateRejectUpdateForNonExistentAsset() throws Exception {
            registerTestTenantJwts(List.of("write:asset"));

            var id = "bc6a0ff7-793b-4d4d-986a-9dcc895bbe12";
            var path = "/asset/%s".formatted(id);

            var requestBody = new AssetDto();
            requestBody.setName("V.I Freud");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/locksmit");

            sendPutRequest(path, requestBody, ARQUEBUS_JWT)
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "No Asset entity found with ID %s"
                            }
                            """.formatted(id)
                    ));
        }

        @Test
        @DisplayName("should reject a request from an unknown tenant")
        public void putShouldUpdateRejectUpdateForUnknownTenant() throws Exception {
             registerTestTenantJwts(List.of("write:asset"));

            var path = "/asset/%s".formatted(ARQUEBUS_TEST_ASSET_ID);

            AssetDto requestBody = new AssetDto();
            requestBody.setName("V.IV Rusty");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/steel-haze-ortus");

            sendPutRequest(path, requestBody, UNKNOWN_TENANT_JWT)
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("WWW-Authenticate", containsString("Bearer error=\"invalid_token\"")))
                    .andExpect(header().string("WWW-Authenticate", containsString("Unknown tenant: https://idp.example.org/unknown-tenant")))
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should reject a request with no authorization")
        public void putShouldUpdateRejectUpdateWithNoAuthorization() throws Exception {
             registerTestTenantJwts(List.of("write:asset"));

            var path = "/asset/%s".formatted(ARQUEBUS_TEST_ASSET_ID);

            var requestBody = new AssetDto();
            requestBody.setName("V.IV Rusty");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/steel-haze-ortus");

            mockMvc.perform(put(path)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should reject cross-tenant requests")
        public void putShouldUpdateRejectCrossTenantUpdates() throws Exception {
             registerTestTenantJwts(List.of("write:asset"));

            var path = "/asset/%s".formatted(BALAM_TEST_ASSET_ID);

            var requestBody = new AssetDto();
            requestBody.setName("ARQUEBUS RULES");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/balam-drools");

            sendPutRequest(path, requestBody, ARQUEBUS_JWT)
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "No Asset entity found with ID %s"
                            }""".formatted(BALAM_TEST_ASSET_ID)
                    ));
        }

        @Test
        @DisplayName("should reject an request from a user without the write:asset role")
        public void putShouldUpdateRejectRequestFromUserWithoutWriteAccess() throws Exception {
             registerTestTenantJwts(Collections.emptyList());

            var path = "/asset/%s".formatted(ARQUEBUS_TEST_ASSET_ID);

            var requestBody = new AssetDto();
            requestBody.setName("V.IV Rusty");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/steel-haze-ortus");

            sendPutRequest(path, requestBody, ARQUEBUS_JWT)
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should update an existing Asset for a properly authorized request")
        public void putShouldUpdateAnExistingAsset() throws Exception {
             registerTestTenantJwts(List.of("write:asset"));

            var id = ARQUEBUS_TEST_ASSET_ID;
            var path = "/asset/%s".formatted(id);

            var requestBody = new AssetDto();
            requestBody.setName("V.IV Rusty");
            requestBody.setCreatedTimestamp("1970-01-01T00:00:00.000Z");
            requestBody.setServerUrl("https://arquebus.space/steel-haze-ortus");

            sendPutRequest(path, requestBody, ARQUEBUS_JWT)
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().json("""
                            {
                                "id": "%s",
                                "name": "%s",
                                "createdTimestamp": "%s",
                                "serverUrl": "%s"
                            }""".formatted(
                            id,
                            requestBody.getName(),
                            requestBody.getCreatedTimestamp(),
                            requestBody.getServerUrl()
                    )));
        }
    }

    @Nested
    @DisplayName("DELETE operation")
    public class DeleteOperationTests {

        @Test
        @DisplayName("should validate request parameters")
        public void putShouldValidateRequestBody() throws Exception {
             registerTestTenantJwts(List.of("write:asset"));

            var path = "/asset/not-a-uuid";

            sendDeleteRequest(path, ARQUEBUS_JWT)
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
        @DisplayName("should reject a request from an unknown tenant")
        public void putShouldUpdateRejectUpdateForUnknownTenant() throws Exception {
             registerTestTenantJwts(List.of("write:asset"));

            var path = "/asset/%s".formatted(ARQUEBUS_TEST_ASSET_ID);

            sendDeleteRequest(path, UNKNOWN_TENANT_JWT)
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("WWW-Authenticate", containsString("Bearer error=\"invalid_token\"")))
                    .andExpect(header().string("WWW-Authenticate", containsString("Unknown tenant: https://idp.example.org/unknown-tenant")))
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should reject a request with no authorization")
        public void putShouldUpdateRejectUpdateWithNoAuthorization() throws Exception {
             registerTestTenantJwts(List.of("write:asset"));

            var path = "/asset/%s".formatted(ARQUEBUS_TEST_ASSET_ID);

            mockMvc.perform(delete(path))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should reject a request for a non-existent Asset")
        public void deleteShouldUpdateRejectUpdateForNonExistentAsset() throws Exception {
            registerTestTenantJwts(List.of("write:asset"));

            var id = "bc6a0ff7-793b-4d4d-986a-9dcc895bbe12";
            var path = "/asset/%s".formatted(id);

            sendDeleteRequest(path, ARQUEBUS_JWT)
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "No Asset entity found with ID %s"
                            }
                            """.formatted(id)
                    ));
        }

        @Test
        @DisplayName("should not delete Assets from other tenants")
        public void putShouldNotReturnAssetsFromOtherTenants() throws Exception {
             registerTestTenantJwts(List.of("write:asset"));

            var arquebusPath = "/asset/%s".formatted(ARQUEBUS_TEST_ASSET_ID);
            var balamPath = "/asset/%s".formatted(BALAM_TEST_ASSET_ID);

            sendDeleteRequest(arquebusPath, BALAM_JWT)
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "No Asset entity found with ID %s"
                            }""".formatted(ARQUEBUS_TEST_ASSET_ID)
                    ));

            sendDeleteRequest(balamPath, ARQUEBUS_JWT)
                    .andExpect(status().isNotFound())
                    .andExpect(content().json("""
                            {
                                "errorMessage": "No Asset entity found with ID %s"
                            }""".formatted(BALAM_TEST_ASSET_ID)
                    ));
        }

        @Test
        @DisplayName("should reject a request from a user without the write:asset role")
        public void deleteShouldUpdateRejectRequestFromUserWithoutWriteAccess() throws Exception {
            registerTestTenantJwts(Collections.emptyList());

            var arquebusPath = "/asset/%s".formatted(ARQUEBUS_TEST_ASSET_ID);
            var balamPath = "/asset/%s".formatted(BALAM_TEST_ASSET_ID);

            sendDeleteRequest(arquebusPath, ARQUEBUS_JWT)
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(""));

            sendDeleteRequest(balamPath, BALAM_JWT)
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("should delete requested Asset for a properly authorized request")
        public void putShouldReturnAssetsFromAuthorizedTenant() throws Exception {
             registerTestTenantJwts(List.of("write:asset"));

            var arquebusPath = "/asset/%s".formatted(ARQUEBUS_TEST_ASSET_ID);
            var balamPath = "/asset/%s".formatted(BALAM_TEST_ASSET_ID);

            sendDeleteRequest(arquebusPath, ARQUEBUS_JWT)
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().string(""));

            sendDeleteRequest(balamPath, BALAM_JWT)
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().string(""));
        }
    }
}