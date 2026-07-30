package org.example.library.recommendation.service;

import org.example.library.config.AbstractServiceIntegrationTest;
import org.example.library.recommendation.event.UserProfileUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.example.library.book.domain.BookStatus.SYNCED;
import static org.example.library.library_book.domain.LibraryBookStatus.FAVORITE;
import static org.example.library.library_book.domain.LibraryBookStatus.READING;

@TestPropertySource(properties = "library.recommendation.profile-rebuild-debounce=PT1S")
class UserProfileServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void shouldAsynchronouslyRebuildVectorOnEventAfterCommit() {
        var testUser = saveUser();
        var defaultCategory = saveCategory(c -> c.name("IT").description("IT Category"));
        var embedding = new float[384];
        embedding[0] = 1.0f;
        var book = saveBook(b -> b.title("Java Book").bookLanguage("English").description("Description").embedding(embedding).category(defaultCategory).status(SYNCED));

        transactionTemplate.executeWithoutResult(status -> {
            saveLibraryBook(lb -> lb.user(testUser).book(book).status(FAVORITE).title("Java Book"));
            eventPublisher.publishEvent(new UserProfileUpdatedEvent(testUser.getId()));
        });

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var savedVector = userProfileService.getUserProfileEmbedding(testUser.getId());
            assertThat(savedVector).isPresent();
            assertThat(savedVector.get()[0]).isGreaterThan(0.0f);
        });
    }

    @Test
    void shouldDeleteVectorOnEventWhenLibraryIsEmpty() {
        var testUser = saveUser();
        var defaultCategory = saveCategory(c -> c.name("IT").description("IT Category"));
        var embedding = new float[384];
        embedding[0] = 1.0f;
        var book = saveBook(b -> b.title("Java Book").bookLanguage("English").description("Description").embedding(embedding).category(defaultCategory).status(SYNCED));

        transactionTemplate.executeWithoutResult(status -> {
            saveLibraryBook(lb -> lb.user(testUser).book(book).status(READING).title("Java Book"));
            eventPublisher.publishEvent(new UserProfileUpdatedEvent(testUser.getId()));
        });

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(userProfileService.getUserProfileEmbedding(testUser.getId())).isPresent();
        });

        transactionTemplate.executeWithoutResult(status -> {
            testDbClient.deleteAllLibraryBooks();
            eventPublisher.publishEvent(new UserProfileUpdatedEvent(testUser.getId()));
        });

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var savedVector = testDbClient.findUserProfileVectorById(testUser.getId());
            assertThat(savedVector).isNull();
        });
    }

}
