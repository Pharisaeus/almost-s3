package net.forprogrammers.almosts3.test;

import io.vavr.control.Either;
import net.forprogrammers.almosts3.client.FancyFile;
import net.forprogrammers.almosts3.interfaces.error.RestRequestFailure;
import net.forprogrammers.almosts3.test.dsl.TestConfiguration;
import net.forprogrammers.almosts3.test.dsl.TestFile;
import net.forprogrammers.almosts3.test.dsl.TestUser;
import org.junit.jupiter.api.Test;

public class DownloadTest extends BaseIT {

    @Test
    public void shouldReturnFileIfFileIsAccessibleAnonymously() {
        // given
        TestConfiguration testConfiguration = testHelper.createNewConfiguration()
                .withFile(TestFile.builder()
                        .withAccessibleAnonymously()
                        .withAvailableForDownload()
                        .build())
                .setup();
        TestFile storedFile = testConfiguration.getFile();

        // when
        Either<RestRequestFailure, FancyFile> result = client.getFileAnonymously(storedFile.getFileId());

        // then
        testHelper.assertOnResponse(result)
                .isOk()
                .matchesContent(storedFile.getContent());
    }

    @Test
    public void shouldReturnFileForCurrentUserIfFileIsAccessibleAnonymously() {
        // given
        TestConfiguration testConfiguration = testHelper.createNewConfiguration()
                .withFile(TestFile.builder()
                        .withAccessibleAnonymously()
                        .withAvailableForDownload()
                        .build())
                .withUser(TestUser.builder().build())
                .setup();
        TestFile storedFile = testConfiguration.getFile();

        // when
        Either<RestRequestFailure, FancyFile> result = client.getFileAuthenticated(
                storedFile.getFileId(),
                testHelper.getValidToken(testConfiguration.getUser())
        );

        // then
        testHelper.assertOnResponse(result)
                .isOk()
                .matchesContent(storedFile.getContent());
    }

    @Test
    public void shouldReturnFileIfFileIsAccessibleForCurrentUser() {
        // given
        TestUser user = TestUser.builder().build();
        TestConfiguration testConfiguration = testHelper.createNewConfiguration()
                .withFile(TestFile.builder()
                        .withAccessibleFor(user)
                        .withAvailableForDownload()
                        .build())
                .setup();
        TestFile storedFile = testConfiguration.getFile();

        // when
        Either<RestRequestFailure, FancyFile> result = client.getFileAuthenticated(
                storedFile.getFileId(),
                testHelper.getValidToken(user)
        );

        // then
        testHelper.assertOnResponse(result)
                .isOk()
                .matchesContent(storedFile.getContent());
    }

    @Test
    public void shouldReturnUnauthorizedIfUserHasInvalidToken() {
        // given
        TestUser user = TestUser.builder().build();
        TestConfiguration testConfiguration = testHelper.createNewConfiguration()
                .withFile(TestFile.builder()
                        .withAccessibleAnonymously()
                        .withAvailableForDownload()
                        .build())
                .setup();

        // when
        Either<RestRequestFailure, FancyFile> result = client.getFileAuthenticated(
                testConfiguration.getFile().getFileId(),
                testHelper.getInvalidToken(user)
        );

        // then
        testHelper.assertOnResponse(result)
                .isFailed()
                .isUnauthorized();
    }

    @Test
    public void shouldReturnUnauthorizedIfUserIsNotAuthenticatedAndFileNotAnonymous() {
        // given
        TestUser user = TestUser.builder().build();
        TestConfiguration testConfiguration = testHelper.createNewConfiguration()
                .withFile(TestFile.builder()
                        .withAccessibleFor(user)
                        .withAvailableForDownload()
                        .build())
                .withUser(user)
                .setup();

        // when
        Either<RestRequestFailure, FancyFile> result = client.getFileAnonymously(testConfiguration.getFile().getFileId());

        // then
        testHelper.assertOnResponse(result)
                .isFailed()
                .isUnauthorized();
    }

    @Test
    public void shouldReturnForbiddenIfUserIsAuthenticatedButHasNoAccessToFile() {
        // given
        TestConfiguration testConfiguration = testHelper.createNewConfiguration()
                .withFile(TestFile.builder()
                        .withAvailableForDownload()
                        .build())
                .withUser(TestUser.builder().build())
                .setup();

        // when
        Either<RestRequestFailure, FancyFile> result = client.getFileAuthenticated(
                testConfiguration.getFile().getFileId(),
                testHelper.getValidToken(testConfiguration.getUser())
        );

        // then
        testHelper.assertOnResponse(result)
                .isFailed()
                .isForbidden();
    }

    @Test
    public void shouldReturnInternalErrorIfCannotFetchFileContent() {
        // given
        TestConfiguration testConfiguration = testHelper.createNewConfiguration()
                .withFile(TestFile.builder()
                        .withAccessibleAnonymously()
                        .withUnavailableForDownload()
                        .build())
                .setup();

        // when
        Either<RestRequestFailure, FancyFile> result = client.getFileAnonymously(testConfiguration.getFile().getFileId());

        // then
        testHelper.assertOnResponse(result)
                .isFailed()
                .isInternalError();
    }

    @Test
    public void shouldReturnTemporaryUnavailableIfDbIsDown() {
        // given
        TestConfiguration testConfiguration = testHelper.createNewConfiguration()
                .withFile(TestFile.builder()
                        .withAccessibleAnonymously()
                        .withAvailableForDownload()
                        .build())
                .setup();

        // when
        testHelper.withDbFailure(() -> {
            Either<RestRequestFailure, FancyFile> result = client.getFileAnonymously(testConfiguration.getFile().getFileId());
            // then
            testHelper.assertOnResponse(result)
                    .isFailed()
                    .isTemporaryUnavailable();
        });
    }
}
