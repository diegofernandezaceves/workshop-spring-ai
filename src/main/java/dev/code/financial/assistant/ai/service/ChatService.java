package dev.code.financial.assistant.ai.service;

import dev.code.financial.assistant.ai.model.ChatRequest;
import dev.code.financial.assistant.ai.model.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public ChatResponse chat(ChatRequest chatRequest) {

        String content = this.chatClient.prompt()
                .user(chatRequest.message())
                .call()
                .content();

        return new ChatResponse(content);
    }

}
