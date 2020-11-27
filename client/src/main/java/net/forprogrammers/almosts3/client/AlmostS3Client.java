package net.forprogrammers.almosts3.client;

import io.vavr.control.Either;
import net.forprogrammers.almosts3.interfaces.error.RestRequestFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class AlmostS3Client {
    private static final Logger LOG = LoggerFactory.getLogger(AlmostS3Client.class);
    public static final String DIRECT_DOWNLOAD_PATH = "/file";
    private final HttpClient client = HttpClient.newBuilder().build();
    private final String endpoint;

    public AlmostS3Client(String host, int port) {
        endpoint = String.format("http://%s:%d", host, port);
    }

    public Either<RestRequestFailure, FancyFile> getFileAuthenticated(UUID fileID, String token) {
        LOG.debug("Requesting file {} with token {}.", fileID, token);
        return getFile(HttpRequest.newBuilder().header("Authorization", "Bearer " + token), fileID);
    }

    public Either<RestRequestFailure, FancyFile> getFileAnonymously(UUID fileID) {
        LOG.debug("Requesting file {}", fileID);
        return getFile(HttpRequest.newBuilder(), fileID);
    }

    private Either<RestRequestFailure, FancyFile> getFile(HttpRequest.Builder requestBuilder, UUID fileID) {
        HttpRequest request = requestBuilder
                .GET()
                .uri(URI.create(endpoint + DIRECT_DOWNLOAD_PATH + "/" + fileID.toString()))
                .build();
        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() / 100 == 2) {
                return Either.right(new FancyFile(response.body()));
            } else {
                return Either.left(new RestRequestFailure(response.statusCode(), new String(response.body().readAllBytes())));
            }
        } catch (IOException | InterruptedException e) {
            LOG.warn("Failed to check location of file {}", fileID, e);
            return Either.left(new RestRequestFailure(404, e.toString()));
        }
    }
}
