package dev.code.financial.assistant.ai.controller;

import dev.code.financial.assistant.ai.model.ChatRequest;
import dev.code.financial.assistant.ai.model.ChatResponse;
import dev.code.financial.assistant.ai.service.ChatService;
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

    @PostMapping("/chat")
    public ChatResponse chatAssistant(@RequestBody ChatRequest chatRequest) {
        return this.chatService.chat(chatRequest);
    }

}
