# Domain Glossary

## Author

- **Author Catalog**: Global registry of authors, their translations, and biography data.
- **User Library Author**: Author scoped to a user's personal collection of books.
- **Author Management Seam**: Unified operations for author CRUD, multilingual translation sync, and book deletion dependency validation.
- **Translation Resolution**: 2-step fallback policy (requested locale → default locale). Every author must have a default locale translation present.

## Collection

- **Collection**: A named, user-owned container that holds library books and may be nested under another collection.
  _Avoid_: Shelf, folder
- **Collection hierarchy**: The parent–child nesting formed by a user's collections.
  _Avoid_: Collection tree
- **Top-level collection**: A collection with no parent; the head of its own branch of the hierarchy.
  _Avoid_: Root collection, root node
- **Parent collection / Child collection**: Two collections joined by a direct parent–child relation in the hierarchy.
- **Hierarchy level**: A collection's position in the hierarchy, counting the top level as level 1.
  _Avoid_: Depth
- **Maximum hierarchy level**: The deepest level a collection may occupy (level 4), enforced when creating a subcollection or moving a collection.
- **Ancestor / Descendant**: A collection's ancestors are the collections on the path from its top level down to its parent; its descendants are the collections nested beneath it.
- **Descendant branch**: A collection together with all its descendants, considered as a unit when the collection is moved.
  _Avoid_: Subtree
- **Collection membership**: A library book's association with a collection; a library book may belong to several collections.

## Book

- **Book**: A title in the global catalog. A book created locally by a user is owned by that user and is visible only to them.
- **Library book**: A user's personal library entry — either a book from the global catalog added to that user's library, or a locally created book that only that user sees.

