# Domain Glossary

## Author
- **Author Catalog**: Global registry of authors, their translations, and biography data.
- **User Library Author**: Author scoped to a user's personal collection of books.
- **Author Management Seam**: Unified operations for author CRUD, multilingual translation sync, and book deletion dependency validation.
- **Translation Resolution**: 2-step fallback policy (requested locale → default locale). Every author must have a default locale translation present.

