package dev.code.financial.assistant.ai.service;

import dev.code.financial.assistant.ai.model.Transaction;
import dev.code.financial.assistant.ai.util.TransactionCsvUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class VectorStoreLoaderService {

    private final Resource transactionsResource;
    private final VectorStore transactionsVectorStore;

    public VectorStoreLoaderService(@Value("classpath:data/transactions.csv") Resource transactionsResource,
                                    VectorStore transactionsVectorStore) {

        this.transactionsResource = transactionsResource;
        this.transactionsVectorStore = transactionsVectorStore;
    }

    @PostConstruct
    public void load() {

        if (isChromaEmpty()) {

            log.info("Loading transactions from {}", this.transactionsResource.getFilename());

            try (InputStream inputStream = this.transactionsResource.getInputStream()) {

                List<Document> documents = TransactionCsvUtil.read(inputStream).stream()
                        .map(this::toDocument)
                        .toList();

                this.transactionsVectorStore.add(documents);
                log.info("Loaded {} transactions!", documents.size());

            } catch (IOException e) {
                log.error("Error loading transactions", e);
            }
        } else {
            log.info("Transactions already exits transactions from {}", this.transactionsResource.getFilename());
        }

    }

    public boolean isChromaEmpty() {

        SearchRequest request = SearchRequest.builder()
                .query("transaction")
                .topK(1)
                .build();

        List<Document> results = this.transactionsVectorStore.similaritySearch(request);
        return results.isEmpty();
    }

    private Document toDocument(Transaction transaction) {

        String type = transaction.amount().signum() == -1 ? "Pago" : "Ingreso";
        String content = String.format("%s en %s el %s por %s€ — categoría: %s",
                type,
                transaction.description(),
                transaction.datetime(),
                transaction.amount(),
                transaction.category());

        Map<String, Object> metadata = Map.of(
                "type", type,
                "description", transaction.description(),
                "datetime", transaction.datetime(),
                "category", transaction.category(),
                "amount", transaction.amount());

        return new Document(content, metadata);
    }
}
