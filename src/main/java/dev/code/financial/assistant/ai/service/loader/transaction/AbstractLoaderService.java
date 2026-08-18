package dev.code.financial.assistant.ai.service.loader.transaction;

import dev.code.financial.assistant.ai.dto.TransactionDto;
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

                List<TransactionDto> transactionsDto = TransactionCsvUtil.read(inputStream);
                this.save(transactionsDto);
                log.info("Loaded {} transactions in {}!!", transactionsDto.size(), this.databaseName());

            } catch (Exception e) {
                log.error("Error loading transactions", e);
            }

        } else {
            log.info("Transactions already exits transactions in {}", this.databaseName());
        }

    }

    public abstract boolean isEmpty();

    public abstract void save(List<TransactionDto> transactionsDto);

    public abstract String databaseName();


}
