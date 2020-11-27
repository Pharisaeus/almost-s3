package net.forprogrammers.almosts3.repository;

import io.vavr.control.Either;
import net.forprogrammers.almosts3.interfaces.FileAccessRepository;
import net.forprogrammers.almosts3.interfaces.error.DBError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SQLFileAccessRepository implements FileAccessRepository {
    private static final Logger LOG = LoggerFactory.getLogger(SQLFileAccessRepository.class);
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String CAN_USER_ACCESS_FILE = "select count(*) from file_access access where (access.user_name = :username or access.user_name = 'anonymous') and access.file_id = :file_id";

    public SQLFileAccessRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Either<DBError, Boolean> canAccessFile(String username, UUID fileId) {
        try {
            Integer entries = namedParameterJdbcTemplate.queryForObject(
                    CAN_USER_ACCESS_FILE,
                    Map.of(
                            "username", username,
                            "file_id", fileId.toString()
                    ),
                    Integer.class
            );
            return Either.right(Optional.ofNullable(entries).map(x -> x > 0).orElse(false));
        } catch (Exception exception) {
            LOG.warn("Failed to check file access for {} and {}", username, fileId, exception);
            return Either.left(new DBError(exception.toString()));
        }
    }
}

