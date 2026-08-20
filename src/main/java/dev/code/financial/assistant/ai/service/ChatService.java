package dev.code.financial.assistant.ai.service;

import dev.code.financial.assistant.ai.dto.ChatRequestDto;
import dev.code.financial.assistant.ai.dto.ChatResponseDto;
import dev.code.financial.assistant.ai.dto.analyze.FraudAlertDto;
import dev.code.financial.assistant.ai.tool.TransactionTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Slf4j
@Service
public class ChatService {

    private static final String DEFAULT_CONVERSATION_ID = "banking-assistant-default";

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder,
                       @Value("classpath:prompts/system-prompt.txt") Resource systemPrompt,
                       ChatMemory chatMemory,
                       VectorStore transactionsVectorStore,
                       TransactionTools transactionTools) {

        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(transactionsVectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(80)
                        .similarityThreshold(0.2)
                        .build())
                .build();

        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build();

        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(questionAnswerAdvisor, messageChatMemoryAdvisor, new SimpleLoggerAdvisor())
                .defaultTools(transactionTools)
                .build();
    }

    public ChatResponseDto chat(ChatRequestDto chatRequestDto) {

        String content = this.chatClient.prompt()
                .user(chatRequestDto.message())
                .advisors(this.setDefaultConversationId())
                .call()
                .content();

        return new ChatResponseDto(content);
    }

    public FraudAlertDto analyze(ChatRequestDto chatRequestDto) {

        return this.chatClient.prompt()
                .user(chatRequestDto.message())
                .advisors(this.setDefaultConversationId())
                .call()
                .entity(FraudAlertDto.class);

    }

    private Consumer<ChatClient.AdvisorSpec> setDefaultConversationId() {
        return advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, DEFAULT_CONVERSATION_ID);
    }

}
