package com.example.be.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class gptService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4.1-mini"; // 대화 모델

    private final String apiKey;

    public gptService() {
        Dotenv dotenv = Dotenv.load();
        this.apiKey = dotenv.get("OPENAI_API_KEY");
    }

    // gpt한테 질문 1개만
    public String textGpt(String message) {
        RestTemplate restTemplate = new RestTemplate();

        // Header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // Body
        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "user",
                "content", message
        ));

        body.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(OPENAI_URL, request, Map.class);

        Map<String, Object> choice =
                (Map<String, Object>) ((List<?>) response.getBody().get("choices")).get(0);

        Map<String, Object> gptMessage =
                (Map<String, Object>) choice.get("message");

        return gptMessage.get("content").toString();
    }

    // gpt랑 대화
    public String chatGpt(List<Map<String, String>> messages) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // role/content 정규화
        List<Map<String, String>> normalized = new ArrayList<>();
        for (Map<String, String> m : messages) {
            String role = m.get("role");
            String content = m.get("content");

            if (content == null) content = "";
            if (role == null) role = "user";

            if (role.equalsIgnoreCase("bot")) role = "assistant";
            if (role.equalsIgnoreCase("ai")) role = "assistant";
            if (role.equalsIgnoreCase("me")) role = "user";
            if (role.equalsIgnoreCase("them")) role = "assistant";

            if (!List.of("system", "user", "assistant").contains(role)) {
                role = "user";
            }

            normalized.add(Map.of("role", role, "content", content));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("messages", normalized);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(OPENAI_URL, request, Map.class);

        Map<String, Object> choice =
                (Map<String, Object>) ((List<?>) response.getBody().get("choices")).get(0);

        Map<String, Object> gptMessage =
                (Map<String, Object>) choice.get("message");

        return gptMessage.get("content").toString();
    }
}
