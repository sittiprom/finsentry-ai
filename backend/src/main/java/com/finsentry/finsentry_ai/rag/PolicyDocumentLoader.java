package com.finsentry.finsentry_ai.rag;

import org.springframework.ai.vectorstore.VectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class PolicyDocumentLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PolicyDocumentLoader.class);

    private final VectorStore vectorStore;

    @Value("classpath:/data/fraud_investigation_policy.pdf")
    private Resource policyPdf;

    public PolicyDocumentLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    private boolean isAlreadyIngested() {
        var results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query("fraud policy")
                        .topK(1)
                        .build()
        );
        return !results.isEmpty();
    }


    @Override
    public void run(String... args) throws Exception {
        if (isAlreadyIngested()) {
            log.info("Policy documents already ingested — skipping.");
            return;
        }

        log.info("Ingesting policy document: {}", policyPdf.getFilename());

        var pdfReader = new PagePdfDocumentReader(policyPdf);
        TextSplitter textSplitter = TokenTextSplitter.builder()
                .withChunkSize(300)
                .withMinChunkSizeChars(100)
                .build();

        var chunks = textSplitter.apply(pdfReader.get());
        vectorStore.accept(chunks);

        log.info("Ingested {} chunks into the vector store.", chunks.size());

    }
}
