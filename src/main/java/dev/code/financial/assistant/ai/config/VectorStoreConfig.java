package dev.code.financial.assistant.ai.config;

import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class VectorStoreConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder().requestFactory(new SimpleClientHttpRequestFactory());
    }

    @Bean
    public ChromaApi chromaApi(@Value("${spring.ai.vectorstore.chroma.client.host}") String host,
                               @Value("${spring.ai.vectorstore.chroma.client.port}") int port,
                               RestClient.Builder restClientBuilder,
                               JsonMapper jsonMapper) {
        return new ChromaApi(host + ":" + port, restClientBuilder, jsonMapper);
    }

    @Bean("transactionsVectorStore")
    public VectorStore transactionsVectorStore(ChromaApi chromaApi,
                                               EmbeddingModel embeddingModel,
                                               @Value("${spring.ai.banking.collection-name}") String collectionName) {
        return ChromaVectorStore.builder(chromaApi, embeddingModel)
                .collectionName(collectionName)
                .initializeSchema(true)
                .build();
    }

    @Bean("financialVectorStore")
    public VectorStore financialVectorStore(ChromaApi chromaApi,
                                            EmbeddingModel embeddingModel,
                                            @Value("${spring.ai.financial.collection-name}") String collectionName) {
        return ChromaVectorStore.builder(chromaApi, embeddingModel)
                .collectionName(collectionName)
                .initializeSchema(true)
                .build();
    }

}
