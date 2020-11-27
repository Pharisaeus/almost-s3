package net.forprogrammers.almosts3.server.configuration;

import net.forprogrammers.almosts3.domain.FileAccessService;
import net.forprogrammers.almosts3.external.FileContentClient;
import net.forprogrammers.almosts3.external.FileLocatorClient;
import net.forprogrammers.almosts3.interfaces.FileAccessRepository;
import net.forprogrammers.almosts3.server.service.FileDownloadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    FileAccessService fileAccessService(FileAccessRepository fileAccessRepository,
                                        @Value("${locator.host}") String host,
                                        @Value("${locator.port}") int port) {
        return new FileAccessService(new FileLocatorClient(host, port), new FileContentClient(), fileAccessRepository);
    }

    @Bean
    FileDownloadService fileDownloadService(FileAccessService fileAccessService) {
        return new FileDownloadService(fileAccessService);
    }
}
