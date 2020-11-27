package net.forprogrammers.almosts3.server.service;

import net.forprogrammers.almosts3.domain.FileAccessService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

public class FileDownloadService {
    private final FileAccessService fileAccessService;

    public FileDownloadService(FileAccessService fileAccessService) {
        this.fileAccessService = fileAccessService;
    }

    public ResponseEntity<Resource> getFile(UUID fileID, Authentication principal) {
        String username = Optional.ofNullable(principal)
                .map(Principal::getName)
                .orElse("anonymous");
        return fileAccessService.getFile(username, fileID)
                .fold(
                        left -> ResponseEntity.status(left.getCode()).body(new ByteArrayResource(left.getMsg().getBytes(StandardCharsets.UTF_8))),
                        right -> ResponseEntity.status(200).body(new InputStreamResource(right))
                );
    }
}
