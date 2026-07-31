package org.example.library.book.service;

import lombok.RequiredArgsConstructor;
import org.example.library.book.dto.BookDetails;
import org.example.library.book.dto.GlobalBookDetails;
import org.example.library.book.dto.LibraryBookDetails;
import org.example.library.book.mapper.BookMapper;
import org.example.library.book.repository.BookDisplayViewRepository;
import org.example.library.collection.service.CollectionService;
import org.example.library.common.exception.NotFoundException;
import org.example.library.library_book.mapper.LibraryBookMapper;
import org.example.library.library_book.repository.LibraryBookRepository;
import org.example.library.library_book.repository.LibraryBookViewRepository;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookDetailsService {

    private final CollectionService collectionService;
    private final LibraryBookRepository libraryBookRepository;
    private final LibraryBookViewRepository libraryBookViewRepository;
    private final BookDisplayViewRepository bookDisplayViewRepository;
    private final BookMapper bookMapper;
    private final LibraryBookMapper libraryBookMapper;

    @Transactional(readOnly = true)
    public BookDetails getDetails(Integer bookId, Integer userId) {
        var lang = LocaleContextHolder.getLocale().getLanguage();

        var libraryBookViewOpt = libraryBookViewRepository.findByBookIdAndUserIdAndLanguageCode(bookId, userId, lang);
        var bookRatingSummary = libraryBookRepository.findAverageRatingAndCountByBookId(bookId);

        if (libraryBookViewOpt.isEmpty()) {
            var bookView = bookDisplayViewRepository.findByIdAndLanguageCode(bookId, lang)
                    .orElseThrow(() -> new NotFoundException("error.book.not_found"));
            var book = bookMapper.toBookDto(bookView);

            return GlobalBookDetails.from(book, bookRatingSummary);
        }

        var libraryBook = libraryBookMapper.toDto(libraryBookViewOpt.get());
        var collectionsContainingLibraryBook = collectionService.getCollectionsContainingLibraryBook(userId, libraryBook.getId());

        return LibraryBookDetails.from(libraryBook, bookRatingSummary, collectionsContainingLibraryBook);
    }

}
