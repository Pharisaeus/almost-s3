package net.forprogrammers.almosts3.interfaces;

import io.vavr.control.Either;
import net.forprogrammers.almosts3.interfaces.error.DBError;

import java.util.UUID;

public interface FileAccessRepository {
    Either<DBError, Boolean> canAccessFile(String username, UUID fileId);
}