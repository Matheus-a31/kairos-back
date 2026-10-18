package com.kairos.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Value("${spring.datasource.url:#{null}}")
    private String springDatasourceUrl;

    @Value("${spring.datasource.username:#{null}}")
    private String username;

    @Value("${spring.datasource.password:#{null}}")
    private String password;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverClassName);

        String rawUrl = (databaseUrl != null && !databaseUrl.isBlank()) ? databaseUrl.trim() : springDatasourceUrl;

        // Strip single or double quotes if present (e.g. from .env file)
        if (rawUrl != null) {
            if ((rawUrl.startsWith("'") && rawUrl.endsWith("'")) || (rawUrl.startsWith("\"") && rawUrl.endsWith("\""))) {
                rawUrl = rawUrl.substring(1, rawUrl.length() - 1).trim();
            }
        }

        if (rawUrl != null && (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://"))) {
            log.info("Configuring DataSource from DATABASE_URL URI format");
            try {
                URI uri = new URI(rawUrl);
                String userInfo = uri.getUserInfo();
                if (userInfo != null) {
                    String[] parts = userInfo.split(":", 2);
                    config.setUsername(parts[0]);
                    if (parts.length > 1) {
                        config.setPassword(parts[1]);
                    }
                }
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String query = uri.getQuery();
                // Clean and ensure sslmode parameter
                StringBuilder cleanQuery = new StringBuilder();
                if (query != null && !query.isBlank()) {
                    String[] params = query.split("&");
                    for (String param : params) {
                        if (!param.startsWith("channel_binding=")) {
                            if (cleanQuery.length() > 0) {
                                cleanQuery.append("&");
                            }
                            cleanQuery.append(param);
                        }
                    }
                }
                if (cleanQuery.indexOf("sslmode=") == -1) {
                    if (cleanQuery.length() > 0) {
                        cleanQuery.append("&");
                    }
                    cleanQuery.append("sslmode=require");
                }

                String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath()
                        + (cleanQuery.length() > 0 ? "?" + cleanQuery : "");

                log.info("Constructed JDBC URL: {}", jdbcUrl.replaceAll(":[^/@]+@", ":****@"));
                config.setJdbcUrl(jdbcUrl);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid DATABASE_URL format: " + rawUrl, e);
            }
        } else {
            log.info("Configuring DataSource from standard JDBC configuration");
            config.setJdbcUrl(rawUrl);
            if (username != null) config.setUsername(username);
            if (password != null) config.setPassword(password);
        }

        return new HikariDataSource(config);
    }
}
