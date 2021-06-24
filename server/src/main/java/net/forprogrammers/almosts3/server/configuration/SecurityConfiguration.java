package net.forprogrammers.almosts3.server.configuration;


import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;

import static net.forprogrammers.almosts3.client.AlmostS3Client.DIRECT_DOWNLOAD_PATH;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Configuration
    @Order(1)
    public static class TokenSecurityConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                    .authorizeRequests(authorizeRequests -> authorizeRequests
                            .antMatchers(DIRECT_DOWNLOAD_PATH + "/**")
                            .permitAll()
                    );
        }
    }
}
