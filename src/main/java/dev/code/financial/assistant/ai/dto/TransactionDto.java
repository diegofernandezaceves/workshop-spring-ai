package dev.code.financial.assistant.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonPropertyOrder({
        "transactionId",
        "amount",
        "category",
        "description",
        "balance",
        "datetime"
})
public record TransactionDto(
        String transactionId,
        BigDecimal amount,
        String category,
        String description,
        BigDecimal balance,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime datetime) {
}