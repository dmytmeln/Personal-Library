package org.example.library.config;

import org.jspecify.annotations.NonNull;
import java.util.TimeZone;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;

public final class PostgresTestContainer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER;

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("pgvector/pgvector:pg17");
        POSTGRESQL_CONTAINER.start();
    }

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                applicationContext,
                "spring.datasource.url=" + POSTGRESQL_CONTAINER.getJdbcUrl(),
                "spring.datasource.username=" + POSTGRESQL_CONTAINER.getUsername(),
                "spring.datasource.password=" + POSTGRESQL_CONTAINER.getPassword()
        );
    }

}
