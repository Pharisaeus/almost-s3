package net.forprogrammers.almosts3.test.dsl;

import net.forprogrammers.almosts3.interfaces.FileAccessRepository;
import net.forprogrammers.almosts3.interfaces.FileLocation;
import org.powermock.reflect.Whitebox;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import pl.codewise.canaveral.mock.http.HttpNoDepsMockProvider;
import pl.codewise.canaveral.mock.http.Method;
import pl.codewise.canaveral.mock.http.Mime;
import pl.codewise.canaveral.mock.http.MockRuleProvider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.forprogrammers.almosts3.external.FileLocatorClient.FILE_LOCATION;
import static org.mockito.Mockito.*;

public class TestHelper {
    private final TokenHelper tokenHelper = new TokenHelper();
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final HttpNoDepsMockProvider authTokenService;
    private final HttpNoDepsMockProvider locatorService;
    private final HttpNoDepsMockProvider contentProviderService;
    private final FileAccessRepository fileAccessRepository;

    public TestHelper(NamedParameterJdbcTemplate jdbcTemplate,
                      HttpNoDepsMockProvider authTokenService,
                      HttpNoDepsMockProvider locatorService,
                      HttpNoDepsMockProvider contentProviderService,
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
            locatorService.createRule()
                    .whenCalledWith(Method.GET, FILE_LOCATION + "/" + fileId)
                    .thenRespondWith(MockRuleProvider.Body.from(new FileLocation(contentProviderService.getEndpoint() + "/" + fileId), Mime.JSON));
            if (file.isDownloadable()) {
                contentProviderService.createRule()
                        .whenCalledWith(Method.GET, "/" + fileId)
                        .thenRespondWith(MockRuleProvider.Body.asTextFrom(file.getContent()));
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
        private HttpNoDepsMockProvider authTokenService;
        private HttpNoDepsMockProvider locatorService;
        private HttpNoDepsMockProvider contentProviderService;
        private FileAccessRepository fileAccessRepository;

        public Builder withJdbc(NamedParameterJdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
            return this;
        }

        public Builder withTokenService(HttpNoDepsMockProvider authTokenServer) {
            this.authTokenService = authTokenServer;
            return this;
        }

        public Builder withLocatorService(HttpNoDepsMockProvider locatorServer) {
            this.locatorService = locatorServer;
            return this;
        }

        public Builder withContentProviderService(HttpNoDepsMockProvider contentProviderService) {
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
