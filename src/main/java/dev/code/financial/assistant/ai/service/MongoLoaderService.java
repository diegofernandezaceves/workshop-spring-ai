package dev.code.financial.assistant.ai.service;

import dev.code.financial.assistant.ai.entity.Transaction;
import dev.code.financial.assistant.ai.repository.TransactionRepository;
import dev.code.financial.assistant.ai.util.TransactionCsvUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
public class MongoLoaderService {

    private final Resource transactionsResource;
    private final TransactionRepository transactionRepository;

    public MongoLoaderService(@Value("classpath:data/transactions.csv") Resource transactionsResource,
                              TransactionRepository transactionRepository) {

        this.transactionsResource = transactionsResource;
        this.transactionRepository = transactionRepository;
    }

    @PostConstruct
    public void load() {

        if (isCollectionEmpty()) {

            log.info("Loading transactions from {}", this.transactionsResource.getFilename());

            try (InputStream inputStream = this.transactionsResource.getInputStream()) {

                List<Transaction> transactions = TransactionCsvUtil.read(inputStream).stream()
                        .map(this::toTransaction)
                        .toList();

                this.transactionRepository.saveAll(transactions);

                log.info("Loaded {} transactions!", transactions.size());

            } catch (Exception e) {
                log.error("Error loading transactions", e);
            }

        } else {
            log.info("Transactions already exits transactions from {}", this.transactionsResource.getFilename());
        }

    }

    public boolean isCollectionEmpty() {
        return transactionRepository.count() == 0;
    }

    public Transaction toTransaction(dev.code.financial.assistant.ai.model.Transaction transaction) {
        return Transaction.builder()
                .id(transaction.transactionId())
                .description(transaction.description())
                .amount(transaction.amount())
                .category(transaction.category())
                .balance(transaction.balance())
                .datetime(transaction.datetime())
                .build();
    }
}
