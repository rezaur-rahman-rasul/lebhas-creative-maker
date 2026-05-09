package com.lebhas.creativesaas.creative;

import com.lebhas.creativesaas.common.security.Role;
import com.lebhas.creativesaas.common.security.jwt.JwtAccessTokenService;
import com.lebhas.creativesaas.identity.domain.UserEntity;
import com.lebhas.creativesaas.identity.domain.UserStatus;
import com.lebhas.creativesaas.identity.domain.WorkspaceMembershipEntity;
import com.lebhas.creativesaas.identity.domain.WorkspaceMembershipStatus;
import com.lebhas.creativesaas.identity.infrastructure.persistence.UserRepository;
import com.lebhas.creativesaas.identity.infrastructure.persistence.WorkspaceMembershipRepository;
import com.lebhas.creativesaas.workspace.domain.WorkspaceEntity;
import com.lebhas.creativesaas.workspace.domain.WorkspaceLanguage;
import com.lebhas.creativesaas.workspace.infrastructure.persistence.WorkspaceRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreativeAssetApiIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES::getUsername);
        registry.add("spring.flyway.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
    }

    @LocalServerPort
    int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Autowired
    private JwtAccessTokenService jwtAccessTokenService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private WorkspaceEntity workspace;
    private UserEntity adminUser;

    @BeforeEach
    void setUp() {
        workspaceMembershipRepository.deleteAll();
        workspaceRepository.deleteAll();
        userRepository.deleteAll();
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        adminUser = userRepository.save(UserEntity.register(
                "Admin",
                "User",
                "admin@example.com",
                null,
                "{noop}unused",
                Role.ADMIN,
                UserStatus.ACTIVE,
                true));
        workspace = workspaceRepository.save(WorkspaceEntity.create(
                "Creative Workspace",
                "creative-workspace",
                null,
                null,
                null,
                "Asia/Dhaka",
                WorkspaceLanguage.ENGLISH,
                "BDT",
                "BD",
                adminUser.getId()));
        workspaceMembershipRepository.save(WorkspaceMembershipEntity.create(
                workspace.getId(),
                adminUser.getId(),
                Role.ADMIN,
                WorkspaceMembershipStatus.ACTIVE,
                java.util.Set.of(),
                Instant.now(),
                adminUser.getId()));
    }

    @Test
    void shouldRejectOversizedPaginationRequest() {
        String accessToken = jwtAccessTokenService.generate(adminUser, workspace.getId(), Role.ADMIN).token();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/workspaces/{workspaceId}/assets?size=101", workspace.getId())
                .then()
                .statusCode(400)
                .body("errors[0].code", equalTo("COMMON-400"));
    }

    @Test
    void shouldRejectMaliciousSvgUpload() {
        String accessToken = jwtAccessTokenService.generate(adminUser, workspace.getId(), Role.ADMIN).token();

        given()
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.MULTIPART)
                .multiPart(
                        "file",
                        "brand-logo.svg",
                        """
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 10 10">
                          <script>alert('xss')</script>
                        </svg>
                        """.getBytes(StandardCharsets.UTF_8),
                        "image/svg+xml")
                .multiPart("assetCategory", "BRAND_LOGO")
                .when()
                .post("/api/v1/workspaces/{workspaceId}/assets/upload", workspace.getId())
                .then()
                .statusCode(400)
                .body("errors[0].code", equalTo("ASSET-400-05"));
    }
}
