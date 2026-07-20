package org.example.library.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
@AutoConfigureTestDatabase(replace = NONE)
@Transactional(propagation = NOT_SUPPORTED)
@Import(TestDbClient.class)
public abstract class AbstractRepositoryTest<T> {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected TestDbClient testDbClient;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    @Autowired
    protected T repository;

    @BeforeEach
    void cleanDb() {
        testDbClient.cleanDatabase();
    }

    @AfterEach
    void cleanDbAfter() {
        testDbClient.cleanDatabase();
    }

}
