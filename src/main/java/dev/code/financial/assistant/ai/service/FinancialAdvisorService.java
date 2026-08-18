package dev.code.financial.assistant.ai.service;

import dev.code.financial.assistant.ai.tool.TransactionTools;
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

@Service
public class FinancialAdvisorService {

    private static final String DEFAULT_CONVERSATION_ID = "advisor-assistant-default";

    private final ChatClient chatClient;

    public FinancialAdvisorService(ChatClient.Builder chatClientBuilder,
                                   VectorStore financialVectorStore,
                                   VectorStore transactionsVectorStore,
                                   TransactionTools transactionTools,
                                   ChatMemory chatMemory,
                                   @Value("classpath:prompts/advisor-prompt.txt") Resource advisorPrompt) {

        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build();

        this.chatClient = chatClientBuilder
                .defaultSystem(advisorPrompt)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        messageChatMemoryAdvisor,
                        QuestionAnswerAdvisor.builder(financialVectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .topK(10)
                                        .similarityThreshold(0.3)
                                        .build())
                                .build(),
                        QuestionAnswerAdvisor.builder(transactionsVectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .topK(80)
                                        .similarityThreshold(0.2)
                                        .build())
                                .build()
                )
                .defaultTools(transactionTools)
                .build();
    }

    public String advise(String message) {
        return chatClient.prompt()
                .user(message)
                .advisors(this.setDefaultConversationId())
                .call()
                .content();
    }

    private Consumer<ChatClient.AdvisorSpec> setDefaultConversationId() {
        return advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, DEFAULT_CONVERSATION_ID);
    }

}
