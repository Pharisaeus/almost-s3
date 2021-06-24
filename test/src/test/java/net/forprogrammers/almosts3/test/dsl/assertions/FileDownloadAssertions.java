package net.forprogrammers.almosts3.test.dsl.assertions;

import io.vavr.control.Either;
import net.forprogrammers.almosts3.client.FancyFile;
import net.forprogrammers.almosts3.interfaces.error.RestRequestFailure;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FileDownloadAssertions {
    private final Either<RestRequestFailure, FancyFile> result;

    public FileDownloadAssertions(Either<RestRequestFailure, FancyFile> result) {
        this.result = result;
    }

    public FileDownloadAssertions isOk() {
        assertAll(
                () -> assertTrue(result.isRight(), "Request failed when it should have succeeded")
        );
        return this;
    }

    public FileDownloadAssertions isFailed() {
        assertAll(
                () -> assertTrue(result.isLeft(), "Request succeeded when it should have failed")
        );
        return this;
    }

    public FileDownloadAssertions matchesContent(String content) {
        assertAll(
                () -> assertEquals(content, extractContent(result), String.format("Retrieved file content does not match expected '%s'", content))
        );
        return this;
    }

    public FileDownloadAssertions isUnauthorized() {
        return hasErrorCode(401);
    }

    public FileDownloadAssertions isForbidden() {
        return hasErrorCode(403);
    }

    public FileDownloadAssertions isInternalError() {
        return hasErrorCode(500);
    }

    public FileDownloadAssertions isTemporaryUnavailable() {
        return hasErrorCode(503);
    }

    private FileDownloadAssertions hasErrorCode(int code) {
        assertAll(
                () -> assertEquals(code, result.getLeft().getCode(), String.format("Expected error code %s but got %s", code, result.getLeft().getCode()))
        );
        return this;
    }

    private String extractContent(Either<RestRequestFailure, FancyFile> result) throws IOException {
        return new String(result.get().getContentStream().readAllBytes());
    }
}
