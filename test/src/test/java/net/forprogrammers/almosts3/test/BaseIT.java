package net.forprogrammers.almosts3.test;

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
import pl.codewise.canaveral.core.runtime.DummyRunnerContext;
import pl.codewise.canaveral.mock.http.HttpNoDepsMockProvider;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Main.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class BaseIT {
    @LocalServerPort
    protected int port;
    @Value("${locator.port}")
    private int locatorPort;
    @Inject
    private NamedParameterJdbcTemplate as3jdbcTemplate;
    @Inject
    private FileAccessRepository fileAccessRepository;

    private HttpNoDepsMockProvider authTokenService = HttpNoDepsMockProvider.newConfig()
            .build("authTokenService");
    private HttpNoDepsMockProvider locatorService = HttpNoDepsMockProvider.newConfig()
            .build("locatorService");
    private HttpNoDepsMockProvider contentProviderService = HttpNoDepsMockProvider.newConfig()
            .build("contentProviderService");


    protected AlmostS3Client client;
    protected TestHelper testHelper;

    @BeforeEach
    public void setup() throws Exception {
        authTokenService.start(new DummyRunnerContext() {
            @Override
            public int getFreePort() {
                return 34567;
            }
        });
        locatorService.start(new DummyRunnerContext() {
            @Override
            public int getFreePort() {
                return locatorPort;
            }
        });
        contentProviderService.start(new DummyRunnerContext());
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
