package dev.code.financial.assistant.ai.service.loader.article;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FinancialLoaderService {

    private static final TokenTextSplitter TOKEN_TEXT_SPLITTER = TokenTextSplitter.builder()
            .withChunkSize(500)
            .withMinChunkSizeChars(100)
            .withKeepSeparator(true)
            .build();

    private final VectorStore financialVectorStore;
    private final List<Resource> financialResources;

    public FinancialLoaderService(@Value("classpath:articles/*") List<Resource> financialResources,
                                  VectorStore financialVectorStore) {
        this.financialVectorStore = financialVectorStore;
        this.financialResources = financialResources;
    }

    @PostConstruct
    public void load() {
        log.info("Loading financial articles in Chroma...");

        this.financialResources.stream()
                .map(resource -> {
                    Map<String, String> articleData = this.getArticleData(resource);
                    return Map.of(
                            "title", articleData.get("title"),
                            "topic", articleData.get("topic"),
                            "source", resource);
                })
                .filter(metadata -> {
                    String title = getTitle(metadata);
                    if (isEmpty(title)) {
                        return true;
                    }
                    log.info("The article already exists in Chroma: {}", title);
                    return false;
                })
                .forEach(this::saveArticles);

    }

    private Map<String, String> getArticleData(Resource resource) {
        String originalFilename = resource.getFilename();
        String[] splitFilename = originalFilename.split("_");
        return Map.of(
                "topic", splitFilename[0],
                "title", splitFilename[1].replace(".html", "")
        );
    }

    private String getTitle(Map<String, Object> metadata) {
        return metadata.getOrDefault("title", "").toString();
    }

    private boolean isEmpty(String title) {

        Filter.Expression expresion = new FilterExpressionBuilder()
                .eq("title", title)
                .build();

        SearchRequest request = SearchRequest.builder()
                .query(title)
                .topK(1)
                .filterExpression(expresion)
                .build();

        List<Document> results = this.financialVectorStore.similaritySearch(request);
        return results.isEmpty();
    }

    private List<Document> getDocuments(Map<String, Object> metadata) {

        Resource resource = (Resource) metadata.get("source");

        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> rawDocs = reader.get();

        return TOKEN_TEXT_SPLITTER.apply(rawDocs)
                .stream()
                .map(document -> {
                    String text = document.getText();
                    return new Document(text, metadata);
                })
                .toList();
    }

    private void saveArticles(Map<String, Object> metadata) {

        String title = getTitle(metadata);
        log.info("Processing article: {}", title);

        try {

            List<Document> chunks = this.getDocuments(metadata);
            financialVectorStore.add(chunks);
            log.info("Processed article: {} ({} chunks)", title, chunks.size());

        } catch (Exception e) {
            log.error("Error loading the article {}: {}", title, e.getMessage());
        }
    }

}
