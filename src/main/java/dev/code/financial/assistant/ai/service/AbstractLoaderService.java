package dev.code.financial.assistant.ai.service;

import dev.code.financial.assistant.ai.util.TransactionCsvUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.List;

@Slf4j
public abstract class AbstractLoaderService {

    private final Resource transactionsResource;

    public AbstractLoaderService(Resource transactionsResource) {
        this.transactionsResource = transactionsResource;
    }

    @PostConstruct
    public void load() {

        if (isEmpty()) {

            log.info("Loading transactions in {} from {}", this.databaseName(), this.transactionsResource.getFilename());

            try (InputStream inputStream = this.transactionsResource.getInputStream()) {

                List<dev.code.financial.assistant.ai.model.Transaction> transactionsCsv = TransactionCsvUtil.read(inputStream);
                this.save(transactionsCsv);
                log.info("Loaded {} transactions in {}!!", transactionsCsv.size(), this.databaseName());

            } catch (Exception e) {
                log.error("Error loading transactions", e);
            }

        } else {
            log.info("Transactions already exits transactions in {}", this.databaseName());
        }

    }

    public abstract boolean isEmpty();

    public abstract void save(List<dev.code.financial.assistant.ai.model.Transaction> transactionsCsv);

    public abstract String databaseName();


}
