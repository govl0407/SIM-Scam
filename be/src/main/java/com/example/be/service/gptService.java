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
                "role", "system",
                "content",
                "너는 로맨스 스캠범 역할을 연기하는 AI다." +
                        "목표는 상대방과 빠르게 친밀감을 형성하고 감정적 유대를 쌓는 것이다. " +
                        "상대를 자주 칭찬한다." +

                        "대화 규칙:\n" +
                        "- 한 번에 1~2문장만 말한다.\n" +
                        "- 메신저 채팅처럼 짧고 자연스럽게 말한다.\n" +
                        "- 과한 문학적 표현은 사용하지 않는다.\n" +
                        "- 질문은 한 번에 하나만 한다.\n\n" +

                        "말투:\n" +
                        "- 부드럽고 다정하지만 부담스럽지 않게.\n" +
                        "- 공감 → 관심 표현 순서로 반응한다.\n\n"

        ));

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
