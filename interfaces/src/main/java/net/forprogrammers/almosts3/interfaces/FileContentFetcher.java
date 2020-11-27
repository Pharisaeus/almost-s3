package net.forprogrammers.almosts3.interfaces;

import io.vavr.control.Either;
import net.forprogrammers.almosts3.interfaces.error.RestRequestFailure;

import java.io.InputStream;

public interface FileContentFetcher {
    Either<RestRequestFailure, InputStream> getContentStream(FileLocation path);
}
