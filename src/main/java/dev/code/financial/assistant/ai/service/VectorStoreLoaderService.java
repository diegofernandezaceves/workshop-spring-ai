package dev.code.financial.assistant.ai.service;

import dev.code.financial.assistant.ai.dto.TransactionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

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
    public void save(List<TransactionDto> transactionsDto) {

        List<Document> documents = transactionsDto.stream()
                .map(this::toDocument)
                .toList();

        this.transactionsVectorStore.add(documents);

    }

    @Override
    public String databaseName() {
        return "ChromaDB";
    }

    private Document toDocument(TransactionDto transactionDto) {

        String type = transactionDto.amount().signum() == -1 ? "Payment" : "Income";
        String content = String.format("TransactionId: %s | %s at %s on %s for %s€ — category: %s",
                transactionDto.transactionId(),
                type,
                transactionDto.description(),
                transactionDto.datetime(),
                transactionDto.amount(),
                transactionDto.category());

        Map<String, Object> metadata = Map.of(
                "transactionId", transactionDto.transactionId(),
                "type", type,
                "description", transactionDto.description(),
                "datetime", transactionDto.datetime(),
                "category", transactionDto.category(),
                "amount", transactionDto.amount());

        return new Document(transactionDto.transactionId(), content, metadata);
    }
}
