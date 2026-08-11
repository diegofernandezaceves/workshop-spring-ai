package dev.code.financial.assistant.ai.dto.analyze;

import java.util.List;

public record FraudAlertDto(
        boolean fraudDetected,
        RiskLevel riskLevel,
        String reason,
        String recommendation,
        List<String> transactionIds) {
}
