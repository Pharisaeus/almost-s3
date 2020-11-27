package net.forprogrammers.almosts3.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import net.forprogrammers.almosts3.interfaces.FileLocation;
import net.forprogrammers.almosts3.interfaces.FileLocator;
import net.forprogrammers.almosts3.interfaces.error.RestRequestFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class FileLocatorClient implements FileLocator {
    private static final Logger LOG = LoggerFactory.getLogger(FileLocatorClient.class);
    public static final String FILE_LOCATION = "/locate";
    private final HttpClient client = HttpClient.newBuilder().build();
    private final String endpoint;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FileLocatorClient(String host, int port) {
        endpoint = String.format("http://%s:%d", host, port);
    }

    public Either<RestRequestFailure, FileLocation> getFilePath(UUID fileID) {
        LOG.debug("Checking location of {}", fileID);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(endpoint + FILE_LOCATION + "/" + fileID.toString()))
                .build();
        try {
            byte[] response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
                    .body();
            return Either.right(objectMapper.readValue(response, FileLocation.class));
        } catch (IOException | InterruptedException e) {
            LOG.warn("Failed to check location of file {}", fileID, e);
            return Either.left(new RestRequestFailure(404, e.toString()));
        }
    }
}
