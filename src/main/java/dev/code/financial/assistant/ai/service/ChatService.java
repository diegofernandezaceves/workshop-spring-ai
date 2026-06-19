package dev.code.financial.assistant.ai.service;

import dev.code.financial.assistant.ai.model.ChatRequest;
import dev.code.financial.assistant.ai.model.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder,
                       @Value("classpath:prompts/system-prompt.txt") Resource systemPrompt,
                       VectorStore vectorStore) {

        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(80)
                        .similarityThreshold(0.2)
                        .build())
                .build();

        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(questionAnswerAdvisor)
                .build();
    }

    public ChatResponse chat(ChatRequest chatRequest) {

        String content = this.chatClient.prompt()
                .user(chatRequest.message())
                .call()
                .content();

        return new ChatResponse(content);
    }

}
