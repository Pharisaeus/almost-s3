package net.forprogrammers.almosts3.external;

import io.vavr.control.Either;
import net.forprogrammers.almosts3.interfaces.FileContentFetcher;
import net.forprogrammers.almosts3.interfaces.FileLocation;
import net.forprogrammers.almosts3.interfaces.error.RestRequestFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FileContentClient implements FileContentFetcher {
    private static final Logger LOG = LoggerFactory.getLogger(FileContentClient.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public Either<RestRequestFailure, InputStream> getContentStream(FileLocation path) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(path.getUrl()))
                .build();
        try {
            HttpResponse<InputStream> result = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (result.statusCode() / 100 == 2) {
                return Either.right(result.body());
            } else {
                LOG.warn("Failed to get file from {}, status code {}", path, result.statusCode());
                return Either.left(new RestRequestFailure(500, new String(result.body().readAllBytes())));
            }
        } catch (IOException | InterruptedException e) {
            LOG.warn("Failed to get file from {}", path, e);
            return Either.left(new RestRequestFailure(500, e.toString()));
        }
    }
}
