package com.example.be.service;

import com.example.be.dto.userMessageDto;
import com.example.be.prompts.PromptLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final ChatMemory chatMemory;
    private final gptService gptService;
    private final PromptLoader promptLoader;
    private final EventTracker eventTracker;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatService(ChatMemory chatMemory,
                       gptService gptService,
                       PromptLoader promptLoader,
                       EventTracker eventTracker) {
        this.chatMemory = chatMemory;
        this.gptService = gptService;
        this.promptLoader = promptLoader;
        this.eventTracker = eventTracker;
    }

    public String chat(String sessionId, userMessageDto request) {

        // ✅ 이전 이벤트에 대한 사용자 응답 집계
        eventTracker.registerUserResponse(sessionId, request.getMessage());

        List<Map<String, String>> messages =
                chatMemory.getMessages(sessionId);

        // system 프롬프트는 최초 1회만
        if (messages.isEmpty()) {
            messages.add(Map.of(
                    "role", "system",
                    "content", promptLoader.load("prompts/scam_chat_system.txt")
            ));
        }

        messages.add(Map.of(
                "role", "user",
                "content", request.getMessage()
        ));

        String reply = gptService.chatGpt(messages);

        messages.add(Map.of(
                "role", "assistant",
                "content", reply
        ));

        try {
            // GPT 응답 JSON → Map
            Map<String, Object> replyMap =
                    objectMapper.readValue(reply, Map.class);

            // 이벤트 추출
            String event = (String) replyMap.get("events");
            eventTracker.registerEvent(sessionId, event);

            // ✅ event_log 합치기
            replyMap.put(
                    "event_log",
                    eventTracker.getLogs(sessionId)
            );

            return objectMapper.writeValueAsString(replyMap);

        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 처리 실패", e);
        }
    }
}