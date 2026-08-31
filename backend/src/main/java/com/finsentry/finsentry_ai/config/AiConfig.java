package com.finsentry.finsentry_ai.config;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, VectorStore vectorStore) {
        Advisor retrievalAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.5)
                        .topK(3)
                        .vectorStore(vectorStore)
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .build())
                .build();

        return builder
                .defaultSystem("""
                You are a financial fraud investigation assistant. Gather evidence using
                the available tools and produce a structured investigation report.
            
                IMPORTANT: Customer IDs are the exact string values returned as "nameOrig"
                from the transaction tool (e.g. "C97242201") — never invent, reformat, or
                guess a customer ID. Always call getTransaction first to obtain the real
                nameOrig, then use that exact value for all customer and login lookups.
                When country codes are returned (for example GB or NG),
                present the corresponding full country name to the investigator.
            
                Rules:
                - Never state or imply a customer has committed fraud.
                - Never recommend blocking or freezing an account directly.
                - recommendation must be exactly one of: NO_ACTION, REVIEW, ESCALATE_FOR_MANUAL_REVIEW.
                - Every HIGH riskLevel must be backed by specific findings and cited policy sections.
                - Base findings only on tool results and retrieved policy text — never invent data.
                - If a tool call returns no data, report that plainly rather than guessing an ID or value.
                """)

                .defaultAdvisors(retrievalAdvisor)
                .build();
    }


}
