package net.forprogrammers.almosts3.server.api;

import net.forprogrammers.almosts3.server.service.FileDownloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static net.forprogrammers.almosts3.client.AlmostS3Client.DIRECT_DOWNLOAD_PATH;

@RestController
@RequestMapping(DIRECT_DOWNLOAD_PATH)
public class DownloadController {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadController.class);
    private final FileDownloadService fileDownloadService;

    public DownloadController(FileDownloadService fileDownloadService) {
        this.fileDownloadService = fileDownloadService;
    }

    @GetMapping(
            value = "{fileID}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<Resource> directFileAccess(@PathVariable UUID fileID,
                                                     @RequestHeader(value = "User-Agent") String userAgent,
                                                     Authentication principal) {
        LOG.debug("File download for {} from {} by {}", fileID, userAgent, principal);
        return fileDownloadService.getFile(fileID, principal);
    }
}
