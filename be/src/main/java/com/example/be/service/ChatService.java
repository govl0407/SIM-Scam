package com.example.be.service;

import com.example.be.dto.userMessageDto;
import com.example.be.prompts.PromptLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
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

        List<Map<String, String>> messages =
                chatMemory.getMessages(sessionId);

        // system 프롬프트는 최초 1회만
        if (messages.isEmpty()) {
            messages.add(Map.of(
                    "role", "system",
                    //"content", promptLoader.load("prompts/scam_chat_system.txt")
                    "content", promptLoader.load("prompts/romance_scam_prompt.txt")
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

            String gptEvent = (String) replyMap.get("events");

            // 서버가 이벤트 최종 결정
            String finalEvent = eventTracker.tryRegisterEvent(sessionId, gptEvent);

            // GPT가 보낸 이벤트와 다르면 덮어쓰기
            replyMap.put("events", finalEvent);

            // 로그 추가
            replyMap.put("event_log", eventTracker.getLogs(sessionId));

            return objectMapper.writeValueAsString(replyMap);

        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 처리 실패", e);
        }
    }
    public String chatWithEventResponse(
            String sessionId,
            String event,
            String answer
    ) {
        eventTracker.resolveEvent(sessionId, event, answer);

        List<Map<String, String>> messages = chatMemory.getMessages(sessionId);

        Map<String, Object> payload = Map.of(
                "event", event,
                "answer", answer
        );

        try {
            messages.add(Map.of(
                    "role", "user",
                    "content", objectMapper.writeValueAsString(payload)
            ));


        String reply = gptService.chatGpt(messages);

        messages.add(Map.of(
                "role", "assistant",
                "content", reply
        ));

        Map<String, Object> replyMap =
                objectMapper.readValue(reply, Map.class);

        // event_response 이후에는 반드시 null
        replyMap.put("events", null);
        replyMap.put("event_log", eventTracker.getLogs(sessionId));

        return objectMapper.writeValueAsString(replyMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}