package org.example.library.recommendation.service;

import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookTranslation;
import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryTranslation;
import org.example.library.config.PostgresTestContainer;
import org.example.library.config.TestDbClient;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.recommendation.event.UserProfileUpdatedEvent;
import org.example.library.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.example.library.book.domain.BookStatus.SYNCED;
import static org.example.library.library_book.domain.LibraryBookStatus.FAVORITE;
import static org.example.library.library_book.domain.LibraryBookStatus.READING;
import static org.example.library.user.domain.Role.USER;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "library.recommendation.profile-rebuild-debounce=PT1S")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class UserProfileServiceIntegrationTest {

    @Autowired
    private TestDbClient testDbClient;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private User testUser;
    private Category defaultCategory;

    @BeforeAll
    static void setUpAll() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @BeforeEach
    void setUp() {
        testDbClient.cleanDatabase();

        testUser = User.builder()
                .email("test@example.com")
                .fullName("Test User")
                .password("pass")
                .role(USER)
                .build();
        testDbClient.saveUser(testUser);

        var translation = CategoryTranslation.builder()
                .languageCode("en")
                .name("IT")
                .description("IT Category")
                .build();
        defaultCategory = Category.builder()
                .popularityCount(0)
                .translations(Map.of("en", translation))
                .build();
        translation.setCategory(defaultCategory);

        testDbClient.saveCategory(defaultCategory);
    }

    @AfterEach
    void tearDownEach() {
        testDbClient.cleanDatabase();
    }

    @Test
    void shouldAsynchronouslyRebuildVectorOnEventAfterCommit() {
        var embedding = new float[384];
        embedding[0] = 1.0f;
        var book = saveBook("Java Book", embedding);

        transactionTemplate.executeWithoutResult(status -> {
            var lb = LibraryBook.builder()
                    .user(testUser)
                    .book(book)
                    .status(FAVORITE)
                    .title("Java Book")
                    .addedAt(LocalDateTime.now())
                    .build();
            testDbClient.saveLibraryBook(lb);

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
        var embedding = new float[384];
        embedding[0] = 1.0f;
        var book = saveBook("Java Book", embedding);

        transactionTemplate.executeWithoutResult(status -> {
            var lb = LibraryBook.builder()
                    .user(testUser)
                    .book(book)
                    .status(READING)
                    .title("Java Book")
                    .addedAt(LocalDateTime.now())
                    .build();
            testDbClient.saveLibraryBook(lb);
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

    private Book saveBook(String title, float[] embedding) {
        var book = Book.builder()
                .category(defaultCategory)
                .publishYear((short) 2020)
                .pages((short) 100)
                .coverImageUrl("url")
                .popularityCount(0)
                .status(SYNCED)
                .embedding(embedding)
                .build();

        var translation = BookTranslation.builder()
                .languageCode("en")
                .title(title)
                .bookLanguage("English")
                .description("Description")
                .book(book)
                .build();
        book.setTranslations(Map.of("en", translation));

        testDbClient.saveBook(book);
        return book;
    }

}
