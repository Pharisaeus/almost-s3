package net.forprogrammers.almosts3.test;

import io.vavr.control.Either;
import net.forprogrammers.almosts3.client.FancyFile;
import net.forprogrammers.almosts3.interfaces.error.RestRequestFailure;
import net.forprogrammers.almosts3.test.dsl.TestConfiguration;
import net.forprogrammers.almosts3.test.dsl.TestFile;
import net.forprogrammers.almosts3.test.dsl.TestUser;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DownloadTest extends BaseIT {

    @Test
    public void shouldReturnFileIfFileIsAccessibleAnonymously() throws IOException {
        // given
        TestConfiguration testConfiguration = testHelper.createNewConfiguration()
                .withFile(TestFile.builder()
                        .withAccessibleAnonymously()
                        .withAvailableForDownload()
                        .build())
                .setup();

        // when
        Either<RestRequestFailure, FancyFile> result = client.getFileAnonymously(testConfiguration.getFile().getFileId());

        // then
        assertTrue(result.isRight());
        assertEquals(new String(testConfiguration.getFile().getContent().getBytes()), new String(result.get().getContentStream().readAllBytes()));
    }

    @Test
    public void shouldReturnFileForCurrentUserIfFileIsAccessibleAnonymously() throws IOException {
        // given
        TestConfiguration testConfiguration = testHelper.createNewConfiguration()
                .withFile(TestFile.builder()
                        .withAccessibleAnonymously()
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
        assertTrue(result.isRight());
        assertEquals(new String(testConfiguration.getFile().getContent().getBytes()), new String(result.get().getContentStream().readAllBytes()));
    }

    @Test
    public void shouldReturnFileIfFileIsAccessibleForCurrentUser() throws IOException {
        // given
        TestUser user = TestUser.builder().build();
        TestConfiguration testConfiguration = testHelper.createNewConfiguration()
                .withFile(TestFile.builder()
                        .withAccessibleFor(user)
                        .withAvailableForDownload()
                        .build())
                .setup();

        // when
        Either<RestRequestFailure, FancyFile> result = client.getFileAuthenticated(
                testConfiguration.getFile().getFileId(),
                testHelper.getValidToken(user)
        );

        // then
        assertTrue(result.isRight());
        assertEquals(new String(testConfiguration.getFile().getContent().getBytes()), new String(result.get().getContentStream().readAllBytes()));
    }

    @Test
    public void shouldReturn401IfUserHasInvalidToken() {
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
        assertTrue(result.isLeft());
        assertEquals(401, result.getLeft().getCode());
    }

    @Test
    public void shouldReturn401IfUserIsNotAuthenticatedAndFileNotAnonymous() {
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
        assertTrue(result.isLeft());
        assertEquals(401, result.getLeft().getCode());
    }

    @Test
    public void shouldReturn403IfUserIsAuthenticatedButHasNoAccessToFile() {
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
        assertTrue(result.isLeft());
        assertEquals(403, result.getLeft().getCode());
    }

    @Test
    public void shouldReturn500IfCannotFetchFileContent() {
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
        assertTrue(result.isLeft());
        assertEquals(500, result.getLeft().getCode());
    }

    @Test
    public void shouldReturn503IfCannotCheckFileAccess() {
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
            assertTrue(result.isLeft());
            assertEquals(503, result.getLeft().getCode());
        });
    }
}
