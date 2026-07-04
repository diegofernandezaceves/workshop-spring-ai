package dev.code.financial.assistant.ai.tool;

import dev.code.financial.assistant.ai.dto.TotalAmount;
import dev.code.financial.assistant.ai.dto.TransactionFrequent;
import dev.code.financial.assistant.ai.entity.Transaction;
import dev.code.financial.assistant.ai.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class TransactionTools {

    private final TransactionRepository transactionRepository;

    public TransactionTools(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(description = """
            Returns the last N transactions ordered by date descending.
            IMPORTANT: use ONLY when the user asks to see recent movements or their latest transactions.
            """)
    public List<Transaction> getLatestTransactions(int limit) {
        log.info("Tool getLatestTransactions — limit: {}", limit);
        Sort sort = Sort.by(Sort.Direction.DESC, "datetime");
        return transactionRepository.findAll(sort).stream()
                .limit(limit)
                .toList();
    }

    @Tool(description = """
            Returns the total amount spent in a category between two specific dates.
            IMPORTANT: use ALWAYS when the user mentions dates, months or time ranges together with a category.
            Date format: yyyy-MM-dd (e.g. 2026-01-01).
            Available categories: restaurante, ocio, comercio, transferencia, bizum.
            """)
    public BigDecimal getTotalByCategoryAndDateRange(String category, String fromDate, String toDate) {
        LocalDateTime from = LocalDate.parse(fromDate).atStartOfDay();
        LocalDateTime to = LocalDate.parse(toDate).atTime(23, 59, 59);
        log.info("Tool getTotalByCategoryAndDateRange — category: {}, from: {}, to: {}", category, from, to);
        return transactionRepository.findByCategoryAndDatetimeBetween(category, from, to).stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Tool(description = """
            Returns the total amount spent on a specific place or concept within a date range,
            searching by keyword in the description.
            IMPORTANT: use ALWAYS when the user asks how much they spenton a specific place,
            brand or concept (e.g. "Karting", "Starbucks", "Netflix") within a time period.
            Date format: yyyy-MM-dd (e.g. 2026-03-01).
            """)
    public BigDecimal getTotalByDescriptionAndDateRange(String keyword, String fromDate, String toDate) {
        LocalDateTime from = LocalDate.parse(fromDate).atStartOfDay();
        LocalDateTime to = LocalDate.parse(toDate).atTime(23, 59, 59);
        log.info("Tool getTotalByDescriptionAndDateRange — keyword: {}, from: {}, to: {}", keyword, from, to);
        return transactionRepository
                .getTotalByDescriptionAndDateRange(keyword, from, to)
                .map(TotalAmount::total)
                .orElse(BigDecimal.ZERO);
    }

    @Tool(description = """
            Returns all transactions between two specific datetimes.
            IMPORTANT: use ALWAYS when analyzing movements in a short period of time or detecting multiple consecutive payments.
            Format: yyyy-MM-ddTHH:mm:ss (e.g. 2026-04-01T00:00:00).
            """)
    public List<Transaction> getTransactionsByDateRange(String fromDatetime, String toDatetime) {
        LocalDateTime from = LocalDateTime.parse(fromDatetime);
        LocalDateTime to = LocalDateTime.parse(toDatetime);
        log.info("Tool getTransactionsByDateRange — from: {}, to: {}", from, to);
        return transactionRepository.findByDatetimeBetween(from, to);
    }

    @Tool(description = """
            Returns the most frequent places visited, filtered by category, ordered by number of visits descending.
            IMPORTANT: use ALWAYS when the user asks which place they visit most often, their favourite restaurant,
            supermarket or any frequently visited place.
            Available categories: restaurante, ocio, comercio, transferencia, bizum.
            """)
    public List<TransactionFrequent> getMostFrequentDescriptionsByCategory(String category) {
        log.info("Tool getMostFrequentDescriptionsByCategory — category: {}", category);
        return transactionRepository.getMostFrequentDescriptionsByCategory(category);
    }

}
