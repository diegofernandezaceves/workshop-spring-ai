package dev.code.financial.assistant.ai.controller;

import dev.code.financial.assistant.ai.dto.ChatRequestDto;
import dev.code.financial.assistant.ai.dto.ChatResponseDto;
import dev.code.financial.assistant.ai.dto.analyze.FraudAlertDto;
import dev.code.financial.assistant.ai.service.ChatService;
import dev.code.financial.assistant.ai.service.FinancialAdvisorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final FinancialAdvisorService financialAdvisorService;

    @PostMapping("/chat")
    public ChatResponseDto chatAssistant(@RequestBody ChatRequestDto chatRequestDto) {
        return this.chatService.chat(chatRequestDto);
    }


    @PostMapping("/chat/analyze")
    public FraudAlertDto chatAnalyze(@RequestBody ChatRequestDto chatRequestDto) {
        return this.chatService.analyze(chatRequestDto);
    }

    @PostMapping("/chat/advisor")
    public ChatResponseDto advisor(@RequestBody ChatRequestDto chatRequestDto) {
        return new ChatResponseDto(this.financialAdvisorService.advise(chatRequestDto.message()));
    }

}
