package net.forprogrammers.almosts3.repository;

import net.forprogrammers.almosts3.interfaces.FileAccessRepository;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class RepositoryConfiguration {
    @Bean
    FileAccessRepository fileAccessRepository(NamedParameterJdbcTemplate as3jdbcTemplate) {
        return new SQLFileAccessRepository(as3jdbcTemplate);
    }

    @Bean
    public NamedParameterJdbcTemplate as3jdbcTemplate(DataSource as3DataSource) {
        return new NamedParameterJdbcTemplate(as3DataSource);
    }

    @Bean
    @ConfigurationProperties(prefix = "almosts3.datasource")
    public DataSource as3DataSource() {
        return DataSourceBuilder.create().build();
    }
}
