package net.forprogrammers.almosts3.domain;

import io.vavr.control.Either;
import net.forprogrammers.almosts3.interfaces.FileAccessRepository;
import net.forprogrammers.almosts3.interfaces.FileContentFetcher;
import net.forprogrammers.almosts3.interfaces.FileLocator;
import net.forprogrammers.almosts3.interfaces.error.RestRequestFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.UUID;

public class FileAccessService {
    private static final Logger LOG = LoggerFactory.getLogger(FileAccessService.class);
    private final FileLocator fileLocator;
    private final FileContentFetcher fileContentFetcher;
    private final FileAccessRepository fileAccessRepository;

    public FileAccessService(FileLocator fileLocator,
                             FileContentFetcher fileContentFetcher,
                             FileAccessRepository fileAccessRepository) {
        this.fileLocator = fileLocator;
        this.fileContentFetcher = fileContentFetcher;
        this.fileAccessRepository = fileAccessRepository;
    }

    public Either<RestRequestFailure, InputStream> getFile(String username, UUID fileID) {
        LOG.debug("Get file {} as {}", fileID, username);
        return fileAccessRepository.canAccessFile(username, fileID)
                .mapLeft(error -> new RestRequestFailure(503, error.getMsg()))
                .flatMap(access -> getFileIfAccessible(access, fileID, username.equals("anonymous")));
    }

    private Either<RestRequestFailure, InputStream> getFileIfAccessible(Boolean hasAccess, UUID fileID, boolean anonymous) {
        if (hasAccess) {
            return fileLocator.getFilePath(fileID)
                    .flatMap(fileContentFetcher::getContentStream);
        } else if (!anonymous) {
            return Either.left(new RestRequestFailure(403, "Za cienki jestes w uszach."));
        } else {
            return Either.left(new RestRequestFailure(401, "Nie dla psa kielbasa."));
        }
    }
}
