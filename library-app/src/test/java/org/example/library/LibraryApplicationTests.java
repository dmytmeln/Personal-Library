package org.example.library;

import org.example.library.config.PostgresTestContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;

@SpringBootTest
@ActiveProfiles("test")
class LibraryApplicationTests {

    @DynamicPropertySource
    static void setPostgresProperties(DynamicPropertyRegistry registry) {
        PostgresTestContainer.setProperties(registry);
    }

    @Test
    void contextLoads() {
    }

}
