package org.example.chatmind.service.impl;

import org.example.chatmind.mapper.ChunkBgeM3Mapper;
import org.example.chatmind.model.entity.ChunkBgeM3;
import org.example.chatmind.service.RagService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class RagServiceImpl implements RagService {

    //封装本地调用
    private final WebClient  webClient;

    private final ChunkBgeM3Mapper chunkBgeM3Mapper;

    public RagServiceImpl(WebClient.Builder webClientBuilder, ChunkBgeM3Mapper chunkBgeM3Mapper) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:11434").build();
        this.chunkBgeM3Mapper = chunkBgeM3Mapper;
    }
    @Override
    public float[] embed(String text) {
        float[] embedding = webClient.post()
                .uri("/api/embeddings")
                .bodyValue(
                        Map.of(
                                "model", "bge-m3",
                                "prompt", text
                        )
                )
                .retrieve()
                .bodyToMono(float[].class)
                .block();
        Assert.notNull(embedding, "embedding is null");
        return embedding;
    }

    @Override
    public List<String> search(String kbId, String title) {
        float[] embed = embed(title);
        String vector = toPgVector(embed);
        List<ChunkBgeM3> chunkBgeM3s = chunkBgeM3Mapper.similaritySearch(kbId, vector, 3);
        return chunkBgeM3s.stream().map(ChunkBgeM3::getContent).toList();
    }

    private String toPgVector(float[] v) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < v.length; i++) {
            sb.append(v[i]);
            if (i < v.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
