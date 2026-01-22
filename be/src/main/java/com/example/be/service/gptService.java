package com.example.be.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class gptService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4.1-mini"; // 가볍고 테스트용 좋음

    private final String apiKey = System.getenv("OPENAI_API_KEY");

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

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(OPENAI_URL, request, Map.class);

        // GPT 응답 파싱
        Map<String, Object> choice =
                (Map<String, Object>) ((List<?>) response.getBody().get("choices")).get(0);

        Map<String, Object> gptMessage =
                (Map<String, Object>) choice.get("message");

        return gptMessage.get("content").toString();
    }
}
