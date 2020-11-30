package net.forprogrammers.almosts3.test.dsl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import net.forprogrammers.almosts3.interfaces.FileAccessRepository;
import net.forprogrammers.almosts3.interfaces.FileLocation;
import org.powermock.reflect.Whitebox;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static net.forprogrammers.almosts3.external.FileLocatorClient.FILE_LOCATION;
import static org.mockito.Mockito.*;

public class TestHelper {
    private final TokenHelper tokenHelper = new TokenHelper();
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final WireMockServer authTokenService;
    private final WireMockServer locatorService;
    private final WireMockServer contentProviderService;
    private final FileAccessRepository fileAccessRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TestHelper(NamedParameterJdbcTemplate jdbcTemplate,
                      WireMockServer authTokenService,
                      WireMockServer locatorService,
                      WireMockServer contentProviderService,
                      FileAccessRepository fileAccessRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.authTokenService = authTokenService;
        this.locatorService = locatorService;
        this.contentProviderService = contentProviderService;
        this.fileAccessRepository = fileAccessRepository;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getValidToken(TestUser user) {
        return tokenHelper.generateUserToken(user.getUsername(), authTokenService);
    }

    public String getInvalidToken(TestUser user) {
        return tokenHelper.generateInvalidToken(user.getUsername(), authTokenService);
    }

    public TestConfiguration.Builder createNewConfiguration() {
        return TestConfiguration.builder(this);
    }

    public void createUsers(Set<TestUser> users) {
        for (TestUser user : users) {
            jdbcTemplate.update(
                    "insert into users(user_name, name, surname) values (:username, :name, :surname)",
                    Map.of(
                            "username", user.getUsername(),
                            "name", user.getName(),
                            "surname", user.getSurname()
                    )
            );
        }
    }

    public void createFiles(List<TestFile> files) {
        for (TestFile file : files) {
            jdbcTemplate.update(
                    "insert into files(file_id, category) values (:file_id, :category)",
                    Map.of(
                            "file_id", file.getFileId(),
                            "category", file.getCategory()
                    )
            );
            for (TestUser user : file.getAccessibleFor()) {
                jdbcTemplate.update(
                        "insert into file_access(user_name, file_id) values (:user_name, :file_id)",
                        Map.of(
                                "user_name", user.getUsername(),
                                "file_id", file.getFileId()
                        )
                );
            }
            String fileId = file.getFileId().toString();
            String contentBody;
            try {
                contentBody = objectMapper.writeValueAsString(new FileLocation(contentProviderService.baseUrl() + "/" + fileId));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            locatorService.stubFor(WireMock.get(WireMock.urlEqualTo(FILE_LOCATION + "/" + fileId))
                    .willReturn(WireMock.aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(contentBody)));
            if (file.isDownloadable()) {
                contentProviderService.stubFor(get(urlEqualTo("/" + fileId))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withBody(file.getContent())));
            }
        }
    }

    public void withDbFailure(Runnable testCode) {
        NamedParameterJdbcTemplate fakeJdbc = mock(NamedParameterJdbcTemplate.class, withSettings()
                .defaultAnswer(invocation -> {
                    throw new RuntimeException();
                })
        );
        Whitebox.setInternalState(fileAccessRepository, NamedParameterJdbcTemplate.class, fakeJdbc);
        testCode.run();
        Whitebox.setInternalState(fileAccessRepository, NamedParameterJdbcTemplate.class, jdbcTemplate);
    }


    public static class Builder {
        private NamedParameterJdbcTemplate jdbcTemplate;
        private WireMockServer authTokenService;
        private WireMockServer locatorService;
        private WireMockServer contentProviderService;
        private FileAccessRepository fileAccessRepository;

        public Builder withJdbc(NamedParameterJdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
            return this;
        }

        public Builder withTokenService(WireMockServer authTokenServer) {
            this.authTokenService = authTokenServer;
            return this;
        }

        public Builder withLocatorService(WireMockServer locatorServer) {
            this.locatorService = locatorServer;
            return this;
        }

        public Builder withContentProviderService(WireMockServer contentProviderService) {
            this.contentProviderService = contentProviderService;
            return this;
        }

        public Builder withFileAccessRepository(FileAccessRepository fileAccessRepository) {
            this.fileAccessRepository = fileAccessRepository;
            return this;
        }

        public TestHelper build() {
            return new TestHelper(jdbcTemplate, authTokenService, locatorService, contentProviderService, fileAccessRepository);
        }
    }
}
