package net.forprogrammers.almosts3.test;

import com.github.tomakehurst.wiremock.WireMockServer;
import net.forprogrammers.almosts3.Main;
import net.forprogrammers.almosts3.client.AlmostS3Client;
import net.forprogrammers.almosts3.interfaces.FileAccessRepository;
import net.forprogrammers.almosts3.test.dsl.TestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Main.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class BaseIT {
    @LocalServerPort
    protected int port;
    @Value("${locator.port}")
    private int locatorPort;
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String publicKeyEndpoint;
    @Inject
    private NamedParameterJdbcTemplate as3jdbcTemplate;
    @Inject
    private FileAccessRepository fileAccessRepository;

    private WireMockServer contentProviderService;
    private WireMockServer locatorService;
    private WireMockServer authTokenService;

    protected AlmostS3Client client;
    protected TestHelper testHelper;

    @BeforeEach
    public void setup() throws MalformedURLException {
        contentProviderService = new WireMockServer(wireMockConfig().dynamicPort());
        contentProviderService.start();
        authTokenService = new WireMockServer(wireMockConfig().port(new URL(publicKeyEndpoint).getPort()));
        authTokenService.start();
        locatorService = new WireMockServer(wireMockConfig().port(locatorPort));
        locatorService.start();
        client = new AlmostS3Client("localhost", this.port);
        testHelper = TestHelper.builder()
                .withJdbc(as3jdbcTemplate)
                .withTokenService(authTokenService)
                .withLocatorService(locatorService)
                .withContentProviderService(contentProviderService)
                .withFileAccessRepository(fileAccessRepository)
                .build();
    }

    @AfterEach
    public void cleanup() {
        authTokenService.stop();
        locatorService.stop();
        contentProviderService.stop();
        cleanDB();
    }

    public void cleanDB() {
        for (String tableName : Arrays.asList(
                "file_access",
                "users",
                "files")) {
            as3jdbcTemplate.execute("delete from " + tableName, Collections.emptyMap(), x -> null);
        }
    }
}
