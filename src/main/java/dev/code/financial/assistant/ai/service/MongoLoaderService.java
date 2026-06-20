package dev.code.financial.assistant.ai.service;

import dev.code.financial.assistant.ai.entity.Transaction;
import dev.code.financial.assistant.ai.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MongoLoaderService extends AbstractLoaderService {

    private final TransactionRepository transactionRepository;

    public MongoLoaderService(@Value("classpath:data/transactions.csv") Resource transactionsResource,
                              TransactionRepository transactionRepository) {

        super(transactionsResource);
        this.transactionRepository = transactionRepository;
    }

    @Override
    public boolean isEmpty() {
        return transactionRepository.count() == 0;
    }

    @Override
    public void save(List<dev.code.financial.assistant.ai.model.Transaction> transactionsCsv) {

        List<Transaction> transactions = transactionsCsv.stream()
                .map(this::toTransaction)
                .toList();

        this.transactionRepository.saveAll(transactions);

    }

    @Override
    public String databaseName() {
        return "MongoDB";
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
