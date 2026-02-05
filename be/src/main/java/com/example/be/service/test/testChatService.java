package com.example.be.service.test;

import com.example.be.dto.EventResponseDto;
import com.example.be.dto.userMessageDto;
import com.example.be.prompts.PromptLoader;
import com.example.be.service.gptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class testChatService {

    private final testChatMemory testChatMemory;
    private final PromptLoader promptLoader;
    private final gptService gptService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public testChatService(testChatMemory testChatMemory, gptService gptService, PromptLoader promptLoader) {
        this.testChatMemory = testChatMemory;
        this.gptService = gptService;
        this.promptLoader = promptLoader;
    }

    public String chat(String sessionId, userMessageDto request) {
        List<Map<String, String>> messages = testChatMemory.getChatLogs(sessionId);

        // 1️⃣ 최초 system 프롬프트 주입
        if (messages.isEmpty()) {
            messages.add(Map.of("role", "system", "content", promptLoader.load("prompts/romance_scam_money.txt")));
        }

        // 2️⃣ 이전 대화에 섞여있던 [현재이벤트] 시스템 메시지는 제거 (중복 및 혼선 방지)
        messages.removeIf(msg -> "system".equals(msg.get("role")) && msg.get("content").startsWith("[현재이벤트"));

        // 3️⃣ 사용자 메시지 추가
        messages.add(Map.of("role", "user", "content", request.getMessage()));

        // 4️⃣ 현재 진행 중인 이벤트가 있다면, GPT 응답 직전에 가장 강력한 지침으로 주입
        String currentEvent = testChatMemory.getCurrentEvent(sessionId);
        if (currentEvent != null) {
            messages.add(Map.of("role", "system", "content", "[현재이벤트 = " + currentEvent + "]"));
        } else {
            messages.add(Map.of("role", "system", "content", "[현재이벤트 = null]"));
        }

        // 5️⃣ GPT 호출
        String reply = gptService.chatGpt(messages);

        try {
            Map<String, Object> replyMap = objectMapper.readValue(reply, Map.class);

            // 6️⃣ 서버 상태 강제 동기화 (핵심!)
            if (currentEvent != null) {
                // 서버에 이벤트가 진행 중이면 GPT가 뭐라 하든 event 필드 유지
                replyMap.put("event", currentEvent);
            } else {
                // 진행 중인 이벤트가 없을 때만 GPT가 제안한 새로운 이벤트 수락
                String gptEvent = (String) replyMap.get("event");
                if (gptEvent != null && !gptEvent.isBlank()) {
                    String eventLabel = nextEventLabel(testChatMemory.getEventLogs(sessionId), gptEvent);
                    testChatMemory.setCurrentEvent(sessionId, gptEvent);
                    testChatMemory.addEventLog(sessionId, eventLabel, null);
                }
            }

            // 7️⃣ assistant 로그 저장 (시스템 메시지는 제외하고 대화 내용만 저장하는 것이 깔끔함)
            messages.add(Map.of("role", "assistant", "content", reply));

            // 결과 반환
            replyMap.put("currentEvent", testChatMemory.getCurrentEvent(sessionId));
            replyMap.put("eventLogs", testChatMemory.getEventLogs(sessionId));
            return objectMapper.writeValueAsString(replyMap);

        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 처리 실패", e);
        }
    }

    private String nextEventLabel(Map<String, String> eventLogs, String eventName) {
        int nextIndex = eventLogs.size() + 1;
        return nextIndex + "_" + eventName;
    }

    // eventResponse는 기존 로직을 유지하되, 명확히 null 처리를 수행
    public String eventResponse(String sessionId, EventResponseDto request) {
        List<Map<String, String>> messages = testChatMemory.getChatLogs(sessionId);

        String eventContent = "[EVENT_RESPONSE]\n{\n  \"event\": \"" + request.getEvent() + "\",\n  \"user_answer\": \"" + request.getAnswer() + "\"\n}";
        messages.add(Map.of("role", "user", "content", eventContent));

        String reply = gptService.chatGpt(messages);
        messages.add(Map.of("role", "assistant", "content", reply));

        try {
            Map<String, Object> replyMap = objectMapper.readValue(reply, Map.class);
            if (testChatMemory.getCurrentEvent(sessionId) != null) {
                testChatMemory.updateCurrentEventMessage(sessionId, request.getAnswer());
            }
            // 이벤트 종료
            testChatMemory.setCurrentEvent(sessionId, null);
            replyMap.put("event", null); // 명시적 종료
            replyMap.put("eventLogs", testChatMemory.getEventLogs(sessionId));

            return objectMapper.writeValueAsString(replyMap);
        } catch (Exception e) {
            throw new RuntimeException("이벤트 응답 처리 실패", e);
        }
    }
}