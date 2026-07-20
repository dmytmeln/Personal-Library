package org.example.library.config;

import lombok.RequiredArgsConstructor;
import org.example.library.auth.domain.RefreshToken;
import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorTranslation;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookStatus;
import org.example.library.book.domain.BookTranslation;
import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryTranslation;
import org.example.library.collection.domain.Collection;
import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.collection_book.domain.CollectionBookId;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.note.domain.Note;
import org.example.library.quote.domain.Quote;
import org.example.library.reading_goal.domain.ReadingGoal;
import org.example.library.recommendation.domain.UserProfileVector;
import org.example.library.user.domain.Role;
import org.example.library.user.domain.User;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDbClient {

    private final JdbcClient jdbcClient;

    public void cleanDatabase() {
        List<String> tableNames = jdbcClient.sql("""
                        SELECT table_name
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_type = 'BASE TABLE'
                          AND table_name != 'databasechangelog'
                          AND table_name != 'databasechangeloglock'
                        """)
                .query(String.class)
                .list();

        if (tableNames.isEmpty()) {
            return;
        }

        String tablesString = String.join(", ", tableNames);
        jdbcClient.sql("TRUNCATE TABLE " + tablesString + " RESTART IDENTITY CASCADE")
                .update();
    }

    public void saveUser(User user) {
        if (user.getId() == null) {
            Integer nextId = jdbcClient.sql("SELECT nextval('users_seq')").query(Integer.class).single();
            user.setId(nextId);
        }

        jdbcClient.sql("""
                        INSERT INTO users (user_id, email, full_name, password, role)
                        VALUES (:id, :email, :fullName, :password, :role)
                        """)
                .param("id", user.getId())
                .param("email", user.getEmail())
                .param("fullName", user.getFullName())
                .param("password", user.getPassword())
                .param("role", user.getRole().name())
                .update();
    }

    public User findUserById(Integer id) {
        return jdbcClient.sql("SELECT user_id, email, full_name, password, role FROM users WHERE user_id = :id")
                .param("id", id)
                .query((rs, rowNum) -> User.builder()
                        .id(rs.getInt("user_id"))
                        .email(rs.getString("email"))
                        .fullName(rs.getString("full_name"))
                        .password(rs.getString("password"))
                        .role(Role.valueOf(rs.getString("role")))
                        .build())
                .optional()
                .orElse(null);
    }

    public void saveRefreshToken(RefreshToken token) {
        if (token.getId() == null) {
            Integer nextId = jdbcClient.sql("SELECT nextval('refresh_tokens_seq')").query(Integer.class).single();
            token.setId(nextId);
        }

        if (token.getCreatedAt() == null) {
            token.setCreatedAt(Instant.now());
        }

        jdbcClient.sql("""
                        INSERT INTO refresh_tokens (refresh_token_id, user_id, revoked, refresh_token_hash, expiry_date, created_at)
                        VALUES (:id, :userId, :revoked, :hash, :expiryDate, :createdAt)
                        """)
                .param("id", token.getId())
                .param("userId", token.getUser().getId())
                .param("revoked", token.isRevoked())
                .param("hash", token.getRefreshTokenHash())
                .param("expiryDate", Timestamp.from(token.getExpiryDate()))
                .param("createdAt", Timestamp.from(token.getCreatedAt()))
                .update();
    }

    public RefreshToken findRefreshTokenById(Integer id) {
        return jdbcClient.sql("""
                        SELECT refresh_token_id, user_id, revoked, refresh_token_hash, expiry_date, created_at
                        FROM refresh_tokens
                        WHERE refresh_token_id = :id
                        """)
                .param("id", id)
                .query((rs, rowNum) -> {
                    RefreshToken token = new RefreshToken();
                    token.setId(rs.getInt("refresh_token_id"));
                    User stubUser = new User();
                    stubUser.setId(rs.getInt("user_id"));
                    token.setUser(stubUser);
                    token.setRevoked(rs.getBoolean("revoked"));
                    token.setRefreshTokenHash(rs.getString("refresh_token_hash"));
                    token.setExpiryDate(rs.getTimestamp("expiry_date").toInstant());
                    token.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                    return token;
                })
                .optional()
                .orElse(null);
    }

    public long countUsers() {
        return jdbcClient.sql("SELECT COUNT(*) FROM users").query(Long.class).single();
    }

    public void saveAuthor(Author author) {
        if (author.getId() == null) {
            Integer nextId = jdbcClient.sql("SELECT nextval('authors_seq')").query(Integer.class).single();
            author.setId(nextId);
        }

        jdbcClient.sql("""
                        INSERT INTO authors (author_id, birth_year, death_year, popularity_count)
                        VALUES (:id, :birthYear, :deathYear, :popularityCount)
                        """)
                .param("id", author.getId())
                .param("birthYear", author.getBirthYear())
                .param("deathYear", author.getDeathYear())
                .param("popularityCount", author.getPopularityCount())
                .update();

        if (author.getTranslations() == null) {
            return;
        }

        for (AuthorTranslation translation : author.getTranslations().values()) {
            jdbcClient.sql("""
                            INSERT INTO author_translations (author_id, language_code, full_name, country, biography)
                            VALUES (:authorId, :languageCode, :fullName, :country, :biography)
                            """)
                    .param("authorId", author.getId())
                    .param("languageCode", translation.getLanguageCode())
                    .param("fullName", translation.getFullName())
                    .param("country", translation.getCountry())
                    .param("biography", translation.getBiography())
                    .update();
        }
    }

    public Author findAuthorById(Integer id) {
        Author author = jdbcClient.sql("SELECT author_id, birth_year, death_year, popularity_count FROM authors WHERE author_id = :id")
                .param("id", id)
                .query((rs, rowNum) -> Author.builder()
                        .id(rs.getInt("author_id"))
                        .birthYear(rs.getShort("birth_year"))
                        .deathYear(rs.getObject("death_year") != null ? rs.getShort("death_year") : null)
                        .popularityCount(rs.getInt("popularity_count"))
                        .build())
                .optional()
                .orElse(null);

        if (author == null) {
            return null;
        }

        List<AuthorTranslation> translations = jdbcClient.sql("""
                        SELECT author_id, language_code, full_name, country, biography
                        FROM author_translations
                        WHERE author_id = :authorId
                        """)
                .param("authorId", id)
                .query((rs, rowNum) -> AuthorTranslation.builder()
                        .authorId(rs.getInt("author_id"))
                        .languageCode(rs.getString("language_code"))
                        .fullName(rs.getString("full_name"))
                        .country(rs.getString("country"))
                        .biography(rs.getString("biography"))
                        .build())
                .list();
        for (AuthorTranslation translation : translations) {
            author.getTranslations().put(translation.getLanguageCode(), translation);
        }

        return author;
    }

    public long countAuthors() {
        return jdbcClient.sql("SELECT COUNT(*) FROM authors").query(Long.class).single();
    }

    public long countAuthorTranslations() {
        return jdbcClient.sql("SELECT COUNT(*) FROM author_translations").query(Long.class).single();
    }

    public void saveBook(Book book) {
        if (book.getId() == null) {
            Integer nextId = jdbcClient.sql("SELECT nextval('books_seq')").query(Integer.class).single();
            book.setId(nextId);
        }

        String embeddingStr = formatVector(book.getEmbedding());

        jdbcClient.sql("""
                        INSERT INTO books (
                            book_id, owner_user_id, category_id, publish_year, pages,
                            cover_image_url, status, popularity_count, embedding
                        ) VALUES (
                            :id, :ownerId, :categoryId, :publishYear, :pages,
                            :coverImageUrl, :status, :popularityCount, CAST(:embedding AS vector)
                        )
                        """)
                .param("id", book.getId())
                .param("ownerId", book.getOwner() != null ? book.getOwner().getId() : null)
                .param("categoryId", book.getCategory() != null ? book.getCategory().getId() : null)
                .param("publishYear", book.getPublishYear())
                .param("pages", book.getPages())
                .param("coverImageUrl", book.getCoverImageUrl())
                .param("status", book.getStatus().name())
                .param("popularityCount", book.getPopularityCount())
                .param("embedding", embeddingStr)
                .update();

        if (book.getTranslations() == null) {
            return;
        }

        for (BookTranslation translation : book.getTranslations().values()) {
            jdbcClient.sql("""
                            INSERT INTO book_translations (book_id, language_code, title, book_language, description)
                            VALUES (:bookId, :languageCode, :title, :bookLanguage, :description)
                            """)
                    .param("bookId", book.getId())
                    .param("languageCode", translation.getLanguageCode())
                    .param("title", translation.getTitle())
                    .param("bookLanguage", translation.getBookLanguage())
                    .param("description", translation.getDescription())
                    .update();
        }
    }

    public void linkBookToAuthor(Integer bookId, Integer authorId) {
        jdbcClient.sql("""
                        INSERT INTO book_authors (book_id, author_id)
                        VALUES (:bookId, :authorId)
                        """)
                .param("bookId", bookId)
                .param("authorId", authorId)
                .update();
    }

    public boolean existsBookAuthorLink(Integer bookId, Integer authorId) {
        return jdbcClient.sql("""
                        SELECT COUNT(*) FROM book_authors
                        WHERE book_id = :bookId AND author_id = :authorId
                        """)
                .param("bookId", bookId)
                .param("authorId", authorId)
                .query(Long.class)
                .single() > 0;
    }

    public void saveCategory(Category category) {
        if (category.getId() == null) {
            Integer nextId = jdbcClient.sql("SELECT nextval('categories_seq')").query(Integer.class).single();
            category.setId(nextId);
        }

        jdbcClient.sql("""
                        INSERT INTO categories (category_id, popularity_count)
                        VALUES (:id, :popularityCount)
                        """)
                .param("id", category.getId())
                .param("popularityCount", category.getPopularityCount())
                .update();

        if (category.getTranslations() == null) {
            return;
        }

        for (CategoryTranslation translation : category.getTranslations().values()) {
            jdbcClient.sql("""
                            INSERT INTO category_translations (category_id, language_code, name, description)
                            VALUES (:categoryId, :languageCode, :name, :description)
                            """)
                    .param("categoryId", category.getId())
                    .param("languageCode", translation.getLanguageCode())
                    .param("name", translation.getName())
                    .param("description", translation.getDescription())
                    .update();
        }
    }

    public Category findCategoryById(Integer id) {
        Category category = jdbcClient.sql("SELECT category_id, popularity_count FROM categories WHERE category_id = :id")
                .param("id", id)
                .query((rs, rowNum) -> Category.builder()
                        .id(rs.getInt("category_id"))
                        .popularityCount(rs.getInt("popularity_count"))
                        .build())
                .optional()
                .orElse(null);

        if (category == null) {
            return null;
        }

        List<CategoryTranslation> translations = jdbcClient
                .sql("SELECT category_id, language_code, name, description FROM category_translations WHERE category_id = :categoryId")
                .param("categoryId", id)
                .query((rs, rowNum) -> CategoryTranslation.builder()
                        .categoryId(rs.getInt("category_id"))
                        .languageCode(rs.getString("language_code"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .build())
                .list();
        for (CategoryTranslation translation : translations) {
            category.getTranslations().put(translation.getLanguageCode(), translation);
        }

        return category;
    }

    public long countCategories() {
        return jdbcClient.sql("SELECT COUNT(*) FROM categories").query(Long.class).single();
    }

    public long countCategoryTranslations() {
        return jdbcClient.sql("SELECT COUNT(*) FROM category_translations").query(Long.class).single();
    }

    public Book findBookById(Integer id) {
        Book book = jdbcClient.sql("""
                        SELECT book_id, owner_user_id, category_id, publish_year, pages, cover_image_url, status,
                               popularity_count, CAST(embedding AS text) AS embedding
                        FROM books
                        WHERE book_id = :id
                        """)
                .param("id", id)
                .query((rs, rowNum) -> {
                    Book b = new Book();
                    b.setId(rs.getInt("book_id"));
                    Integer ownerId = rs.getObject("owner_user_id") != null ? rs.getInt("owner_user_id") : null;
                    if (ownerId != null) {
                        User stubOwner = new User();
                        stubOwner.setId(ownerId);
                        b.setOwner(stubOwner);
                    }
                    Integer catId = rs.getObject("category_id") != null ? rs.getInt("category_id") : null;
                    if (catId != null) {
                        Category stubCat = new Category();
                        stubCat.setId(catId);
                        b.setCategory(stubCat);
                    }
                    b.setPublishYear(rs.getObject("publish_year") != null ? rs.getShort("publish_year") : null);
                    b.setPages(rs.getObject("pages") != null ? rs.getShort("pages") : null);
                    b.setCoverImageUrl(rs.getString("cover_image_url"));
                    b.setStatus(BookStatus.valueOf(rs.getString("status")));
                    b.setPopularityCount(rs.getInt("popularity_count"));
                    b.setEmbedding(parseVector(rs.getString("embedding")));
                    return b;
                })
                .optional()
                .orElse(null);

        if (book == null) {
            return book;
        }

        List<BookTranslation> translations = jdbcClient
                .sql("SELECT book_id, language_code, title, book_language, description FROM book_translations WHERE book_id = :bookId")
                .param("bookId", id)
                .query((rs, rowNum) -> BookTranslation.builder()
                        .bookId(rs.getInt("book_id"))
                        .languageCode(rs.getString("language_code"))
                        .title(rs.getString("title"))
                        .bookLanguage(rs.getString("book_language"))
                        .description(rs.getString("description"))
                        .build())
                .list();
        for (BookTranslation translation : translations) {
            book.getTranslations().put(translation.getLanguageCode(), translation);
        }

        return book;
    }

    public long countBooks() {
        return jdbcClient.sql("SELECT COUNT(*) FROM books").query(Long.class).single();
    }

    public long countBookTranslations() {
        return jdbcClient.sql("SELECT COUNT(*) FROM book_translations").query(Long.class).single();
    }

    public void saveCollection(Collection collection) {
        if (collection.getId() == null) {
            Integer nextId = jdbcClient.sql("SELECT nextval('collections_seq')").query(Integer.class).single();
            collection.setId(nextId);
        }
        if (collection.getCreatedAt() == null) {
            collection.setCreatedAt(LocalDateTime.now());
        }
        if (collection.getUpdatedAt() == null) {
            collection.setUpdatedAt(LocalDateTime.now());
        }

        jdbcClient.sql("""
                        INSERT INTO collections (collection_id, user_id, parent_id, name, description, created_at, updated_at)
                        VALUES (:id, :userId, :parentId, :name, :description, :createdAt, :updatedAt)
                        """)
                .param("id", collection.getId())
                .param("userId", collection.getUser().getId())
                .param("parentId", collection.getParent() != null ? collection.getParent().getId() : null)
                .param("name", collection.getName())
                .param("description", collection.getDescription())
                .param("createdAt", collection.getCreatedAt())
                .param("updatedAt", collection.getUpdatedAt())
                .update();
    }

    public Collection findCollectionById(Integer id) {
        return jdbcClient.sql("""
                        SELECT collection_id, user_id, parent_id, name, description, created_at, updated_at
                        FROM collections WHERE collection_id = :id
                        """)
                .param("id", id)
                .query((rs, rowNum) -> {
                    Collection c = new Collection();
                    c.setId(rs.getInt("collection_id"));
                    User stubUser = new User();
                    stubUser.setId(rs.getInt("user_id"));
                    c.setUser(stubUser);
                    Integer parentId = rs.getObject("parent_id") != null ? rs.getInt("parent_id") : null;
                    if (parentId != null) {
                        Collection parent = new Collection();
                        parent.setId(parentId);
                        c.setParent(parent);
                    }
                    c.setName(rs.getString("name"));
                    c.setDescription(rs.getString("description"));
                    c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    c.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return c;
                })
                .optional()
                .orElse(null);
    }

    public long countCollections() {
        return jdbcClient.sql("SELECT COUNT(*) FROM collections").query(Long.class).single();
    }

    public void saveLibraryBook(LibraryBook libraryBook) {
        if (libraryBook.getId() == null) {
            Integer nextId = jdbcClient.sql("SELECT nextval('library_books_seq')").query(Integer.class).single();
            libraryBook.setId(nextId);
        }
        if (libraryBook.getAddedAt() == null) {
            libraryBook.setAddedAt(LocalDateTime.now());
        }

        jdbcClient.sql("""
                        INSERT INTO library_books (
                            library_book_id, book_id, user_id, status, added_at, finished_at, rating,
                            title, publish_year, pages, language, description, location,
                            custom_author_name, custom_category_name
                        )
                        VALUES (
                            :id, :bookId, :userId, :status, :addedAt, :finishedAt, :rating,
                            :title, :publishYear, :pages, :language, :description, :location,
                            :customAuthorName, :customCategoryName
                        )
                        """)
                .param("id", libraryBook.getId())
                .param("bookId", libraryBook.getBook().getId())
                .param("userId", libraryBook.getUser().getId())
                .param("status", libraryBook.getStatus().name())
                .param("addedAt", libraryBook.getAddedAt())
                .param("finishedAt", libraryBook.getFinishedAt() != null ? java.sql.Date.valueOf(libraryBook.getFinishedAt()) : null)
                .param("rating", libraryBook.getRating())
                .param("title", libraryBook.getTitle())
                .param("publishYear", libraryBook.getPublishYear())
                .param("pages", libraryBook.getPages())
                .param("language", libraryBook.getLanguage())
                .param("description", libraryBook.getDescription())
                .param("location", libraryBook.getLocation())
                .param("customAuthorName", libraryBook.getCustomAuthorName())
                .param("customCategoryName", libraryBook.getCustomCategoryName())
                .update();
    }

    public LibraryBook findLibraryBookById(Integer id) {
        return jdbcClient.sql("""
                        SELECT library_book_id, book_id, user_id, status, added_at, finished_at, rating,
                               title, publish_year, pages, language, description, location,
                               custom_author_name, custom_category_name
                        FROM library_books WHERE library_book_id = :id
                        """)
                .param("id", id)
                .query((rs, rowNum) -> {
                    LibraryBook lb = new LibraryBook();
                    lb.setId(rs.getInt("library_book_id"));

                    Book stubBook = new Book();
                    stubBook.setId(rs.getInt("book_id"));
                    lb.setBook(stubBook);

                    User stubUser = new User();
                    stubUser.setId(rs.getInt("user_id"));
                    lb.setUser(stubUser);

                    lb.setStatus(org.example.library.library_book.domain.LibraryBookStatus.valueOf(rs.getString("status")));
                    lb.setAddedAt(rs.getTimestamp("added_at").toLocalDateTime());
                    lb.setFinishedAt(rs.getDate("finished_at") != null ? rs.getDate("finished_at").toLocalDate() : null);
                    lb.setRating(rs.getObject("rating") != null ? rs.getByte("rating") : null);
                    lb.setTitle(rs.getString("title"));
                    lb.setPublishYear(rs.getObject("publish_year") != null ? rs.getShort("publish_year") : null);
                    lb.setPages(rs.getObject("pages") != null ? rs.getShort("pages") : null);
                    lb.setLanguage(rs.getString("language"));
                    lb.setDescription(rs.getString("description"));
                    lb.setLocation(rs.getString("location"));
                    lb.setCustomAuthorName(rs.getString("custom_author_name"));
                    lb.setCustomCategoryName(rs.getString("custom_category_name"));
                    return lb;
                })
                .optional()
                .orElse(null);
    }

    public long countLibraryBooks() {
        return jdbcClient.sql("SELECT COUNT(*) FROM library_books").query(Long.class).single();
    }

    public void saveCollectionBook(CollectionBook collectionBook) {
        if (collectionBook.getAddedAt() == null) {
            collectionBook.setAddedAt(LocalDateTime.now());
        }

        jdbcClient.sql("""
                        INSERT INTO collection_books (collection_id, library_book_id, added_at)
                        VALUES (:collectionId, :libraryBookId, :addedAt)
                        """)
                .param("collectionId", collectionBook.getId().getCollectionId())
                .param("libraryBookId", collectionBook.getId().getLibraryBookId())
                .param("addedAt", collectionBook.getAddedAt())
                .update();
    }

    public CollectionBook findCollectionBookById(Integer collectionId, Integer libraryBookId) {
        return jdbcClient.sql("""
                        SELECT collection_id, library_book_id, added_at
                        FROM collection_books
                        WHERE collection_id = :collectionId AND library_book_id = :libraryBookId
                        """)
                .param("collectionId", collectionId)
                .param("libraryBookId", libraryBookId)
                .query((rs, rowNum) -> {
                    CollectionBook cb = new CollectionBook();
                    cb.setId(new CollectionBookId(rs.getInt("collection_id"), rs.getInt("library_book_id")));
                    cb.setAddedAt(rs.getTimestamp("added_at").toLocalDateTime());
                    return cb;
                })
                .optional()
                .orElse(null);
    }

    public long countCollectionBooks() {
        return jdbcClient.sql("SELECT COUNT(*) FROM collection_books").query(Long.class).single();
    }

    public void saveNote(Note note) {
        if (note.getId() == null) {
            Integer nextId = jdbcClient.sql("SELECT nextval('notes_seq')").query(Integer.class).single();
            note.setId(nextId);
        }
        if (note.getCreatedAt() == null) {
            note.setCreatedAt(LocalDateTime.now());
        }
        if (note.getUpdatedAt() == null) {
            note.setUpdatedAt(LocalDateTime.now());
        }

        jdbcClient.sql("""
                        INSERT INTO notes (
                            note_id, library_book_id, content, note_type, raw_transcript,
                            transcription_model, formatting_model, voice_created_at, created_at, updated_at
                        )
                        VALUES (
                            :id, :libraryBookId, :content, :noteType, :rawTranscript,
                            :transcriptionModel, :formattingModel, :voiceCreatedAt, :createdAt, :updatedAt
                        )
                        """)
                .param("id", note.getId())
                .param("libraryBookId", note.getLibraryBook().getId())
                .param("content", note.getContent())
                .param("noteType", note.getNoteType().name())
                .param("rawTranscript", note.getRawTranscript())
                .param("transcriptionModel", note.getTranscriptionModel())
                .param("formattingModel", note.getFormattingModel())
                .param("voiceCreatedAt", note.getVoiceCreatedAt())
                .param("createdAt", note.getCreatedAt())
                .param("updatedAt", note.getUpdatedAt())
                .update();
    }

    public Note findNoteById(Integer id) {
        return jdbcClient.sql("""
                        SELECT note_id, library_book_id, content, note_type, raw_transcript,
                               transcription_model, formatting_model, voice_created_at, created_at, updated_at
                        FROM notes WHERE note_id = :id
                        """)
                .param("id", id)
                .query((rs, rowNum) -> {
                    Note note = new Note();
                    note.setId(rs.getInt("note_id"));

                    LibraryBook stubBook = new LibraryBook();
                    stubBook.setId(rs.getInt("library_book_id"));
                    note.setLibraryBook(stubBook);

                    note.setContent(rs.getString("content"));
                    note.setNoteType(org.example.library.note.domain.Note.NoteType.valueOf(rs.getString("note_type")));
                    note.setRawTranscript(rs.getString("raw_transcript"));
                    note.setTranscriptionModel(rs.getString("transcription_model"));
                    note.setFormattingModel(rs.getString("formatting_model"));
                    note.setVoiceCreatedAt(rs.getTimestamp("voice_created_at") != null
                            ? rs.getTimestamp("voice_created_at").toLocalDateTime()
                            : null);
                    note.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    note.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return note;
                })
                .optional()
                .orElse(null);
    }

    public long countNotes() {
        return jdbcClient.sql("SELECT COUNT(*) FROM notes").query(Long.class).single();
    }

    public void saveQuote(Quote quote) {
        if (quote.getId() == null) {
            Integer nextId = jdbcClient.sql("SELECT nextval('quotes_seq')").query(Integer.class).single();
            quote.setId(nextId);
        }
        if (quote.getCreatedAt() == null) {
            quote.setCreatedAt(LocalDateTime.now());
        }
        if (quote.getUpdatedAt() == null) {
            quote.setUpdatedAt(LocalDateTime.now());
        }

        jdbcClient.sql("""
                        INSERT INTO quotes (
                            quote_id, library_book_id, text, page, comment, created_at, updated_at
                        )
                        VALUES (
                            :id, :libraryBookId, :text, :page, :comment, :createdAt, :updatedAt
                        )
                        """)
                .param("id", quote.getId())
                .param("libraryBookId", quote.getLibraryBook().getId())
                .param("text", quote.getText())
                .param("page", quote.getPage())
                .param("comment", quote.getComment())
                .param("createdAt", quote.getCreatedAt())
                .param("updatedAt", quote.getUpdatedAt())
                .update();
    }

    public Quote findQuoteById(Integer id) {
        return jdbcClient.sql("""
                        SELECT quote_id, library_book_id, text, page, comment, created_at, updated_at
                        FROM quotes WHERE quote_id = :id
                        """)
                .param("id", id)
                .query((rs, rowNum) -> {
                    Quote quote = new Quote();
                    quote.setId(rs.getInt("quote_id"));

                    LibraryBook stubBook = new LibraryBook();
                    stubBook.setId(rs.getInt("library_book_id"));
                    quote.setLibraryBook(stubBook);

                    quote.setText(rs.getString("text"));
                    quote.setPage(rs.getString("page"));
                    quote.setComment(rs.getString("comment"));
                    quote.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    quote.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return quote;
                })
                .optional()
                .orElse(null);
    }

    public long countQuotes() {
        return jdbcClient.sql("SELECT COUNT(*) FROM quotes").query(Long.class).single();
    }

    private String formatVector(float[] vector) {
        if (vector == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");

        return sb.toString();
    }

    private float[] parseVector(String vectorStr) {
        if (vectorStr == null) {
            return null;
        }

        String cleanStr = vectorStr.substring(1, vectorStr.length() - 1);
        if (cleanStr.isEmpty()) {
            return new float[0];
        }

        String[] parts = cleanStr.split(",");
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i].trim());
        }

        return vector;
    }

    public void saveReadingGoal(ReadingGoal goal) {
        if (goal.getId() == null) {
            Integer nextId = jdbcClient.sql("SELECT nextval('reading_goals_seq')").query(Integer.class).single();
            goal.setId(nextId);
        }

        jdbcClient.sql("""
                        INSERT INTO reading_goals (goal_id, user_id, year, target_books, target_pages)
                        VALUES (:id, :userId, :year, :targetBooks, :targetPages)
                        """)
                .param("id", goal.getId())
                .param("userId", goal.getUser().getId())
                .param("year", goal.getYear())
                .param("targetBooks", goal.getTargetBooks())
                .param("targetPages", goal.getTargetPages())
                .update();
    }

    public ReadingGoal findReadingGoalById(Integer id) {
        return jdbcClient.sql("""
                        SELECT goal_id, user_id, year, target_books, target_pages
                        FROM reading_goals WHERE goal_id = :id
                        """)
                .param("id", id)
                .query((rs, rowNum) -> {
                    ReadingGoal rg = new ReadingGoal();
                    rg.setId(rs.getInt("goal_id"));
                    User stubUser = new User();
                    stubUser.setId(rs.getInt("user_id"));
                    rg.setUser(stubUser);
                    rg.setYear(rs.getInt("year"));
                    rg.setTargetBooks(rs.getInt("target_books"));
                    rg.setTargetPages(rs.getInt("target_pages"));
                    return rg;
                })
                .optional()
                .orElse(null);
    }

    public long countReadingGoals() {
        return jdbcClient.sql("SELECT COUNT(*) FROM reading_goals").query(Long.class).single();
    }

    public void saveUserProfileVector(UserProfileVector profile) {
        String embeddingStr = formatVector(profile.getEmbedding());

        jdbcClient.sql("""
                        INSERT INTO user_profile_vectors (user_id, embedding, updated_at)
                        VALUES (:userId, CAST(:embedding AS vector), :updatedAt)
                        """)
                .param("userId", profile.getUserId())
                .param("embedding", embeddingStr)
                .param("updatedAt", profile.getUpdatedAt())
                .update();
    }

    public UserProfileVector findUserProfileVectorById(Integer userId) {
        return jdbcClient.sql("""
                        SELECT user_id, CAST(embedding AS text) AS embedding, updated_at
                        FROM user_profile_vectors WHERE user_id = :userId
                        """)
                .param("userId", userId)
                .query((rs, rowNum) -> {
                    UserProfileVector up = new UserProfileVector();
                    up.setUserId(rs.getInt("user_id"));
                    up.setEmbedding(parseVector(rs.getString("embedding")));
                    up.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return up;
                })
                .optional()
                .orElse(null);
    }

    public long countUserProfileVectors() {
        return jdbcClient.sql("SELECT COUNT(*) FROM user_profile_vectors").query(Long.class).single();
    }

}
