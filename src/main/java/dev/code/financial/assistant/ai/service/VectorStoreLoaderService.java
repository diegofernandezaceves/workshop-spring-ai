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
public class VectorStoreLoaderService extends AbstractLoaderService {

    private final VectorStore transactionsVectorStore;

    public VectorStoreLoaderService(@Value("classpath:data/transactions.csv") Resource transactionsResource,
                                    VectorStore transactionsVectorStore) {

        super(transactionsResource);
        this.transactionsVectorStore = transactionsVectorStore;
    }

    @Override
    public boolean isEmpty() {

        SearchRequest request = SearchRequest.builder()
                .query("transaction")
                .topK(1)
                .build();

        List<Document> results = this.transactionsVectorStore.similaritySearch(request);
        return results.isEmpty();
    }

    @Override
    public void save(List<Transaction> transactionsCsv) {

        List<Document> documents = transactionsCsv.stream()
                .map(this::toDocument)
                .toList();

        this.transactionsVectorStore.add(documents);

    }

    @Override
    public String databaseName() {
        return "ChromaDB";
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
