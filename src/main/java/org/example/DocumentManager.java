package org.example;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {
    private final List<Document> documentsDB = new ArrayList<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.id != null){
            Document persistentDocument = findById(document.id).orElseThrow();

            // Only update fields if they are different
            if (!Objects.equals(persistentDocument.getAuthor(), document.getAuthor())) {
                persistentDocument.setAuthor(document.getAuthor());
            }
            if (!Objects.equals(persistentDocument.getContent(), document.getContent())) {
                persistentDocument.setContent(document.getContent());
            }
            if (!Objects.equals(persistentDocument.getTitle(), document.getTitle())) {
                persistentDocument.setTitle(document.getTitle());
            }
            return persistentDocument;
        }

        Document newDocument = Document.builder()
                .id(UUID.randomUUID().toString())
                .created(Instant.now())
                .content(document.content)
                .author(document.author)
                .title(document.title)
                .build();

        documentsDB.add(newDocument);

        return newDocument;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        Stream<Document> documentStream = documentsDB.stream();

        // Title Prefix filtering
        if (request.getTitlePrefixes() != null && !request.getTitlePrefixes().isEmpty()) {
            documentStream = documentStream.filter(document ->
                    request.getTitlePrefixes().stream()
                            .anyMatch(prefix -> document.getTitle().startsWith(prefix)));
        }

        // Content filtering
        if (request.getContainsContents() != null && !request.getContainsContents().isEmpty()) {
            documentStream = documentStream.filter(document ->
                    request.getContainsContents().stream()
                            .anyMatch(content -> document.getContent().contains(content)));
        }

        // Author filtering
        if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
            documentStream = documentStream.filter(document ->
                    request.getAuthorIds().stream()
                            .anyMatch(authorId -> document.getAuthor().getId().equals(authorId)));
        }

        // Date range filtering
        if (request.getCreatedFrom() != null) {
            documentStream = documentStream.filter(document ->
                    document.getCreated().isAfter(request.getCreatedFrom()));
        }

        if (request.getCreatedTo() != null) {
            documentStream = documentStream.filter(document ->
                    document.getCreated().isBefore(request.getCreatedTo()));
        }

        return documentStream.toList();
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return documentsDB.stream()
                .filter(doc -> doc.getId().equals(id))
                .findFirst();
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
