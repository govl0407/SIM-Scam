package com.example.be.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final ChatMemory chatMemory;
    private final gptService gptService;

    public ChatService(ChatMemory chatMemory, gptService gptService) {
        this.chatMemory = chatMemory;
        this.gptService = gptService;
    }

    public String chat(String sessionId, String userMessage) {

        List<Map<String, String>> messages =
                chatMemory.getMessages(sessionId);

        // 시스템 프롬프트 초기화
        if (messages.isEmpty()) {
            messages.add(Map.of(
                    "role", "system",
                    "content",
                    "항상 대화는 ~냥으로 끝내.\n" +
                            "반드시 아래 JSON 형식으로만 응답하라.\n" +
                            "{ \"text\": \"...\", \"events\": [] }"
            ));
        }

        // 사용자 메시지 저장
        messages.add(Map.of(
                "role", "user",
                "content", userMessage
        ));

        // GPT 호출
        String reply = gptService.chatGpt(messages);

        // GPT 응답 저장
        messages.add(Map.of(
                "role", "assistant",
                "content", reply
        ));

        return reply;
    }
}
