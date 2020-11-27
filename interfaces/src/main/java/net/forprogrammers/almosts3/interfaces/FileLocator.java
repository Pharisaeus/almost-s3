package net.forprogrammers.almosts3.interfaces;

import io.vavr.control.Either;
import net.forprogrammers.almosts3.interfaces.error.RestRequestFailure;

import java.util.UUID;

public interface FileLocator {
    Either<RestRequestFailure, FileLocation> getFilePath(UUID fileID);
}
