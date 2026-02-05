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

    public testChatService(testChatMemory testChatMemory,
                           gptService gptService,
                           PromptLoader promptLoader) {
        this.testChatMemory = testChatMemory;
        this.gptService = gptService;
        this.promptLoader = promptLoader;
    }

    public String chat(String sessionId, userMessageDto request) {

        List<Map<String, String>> messages =
                testChatMemory.getChatLogs(sessionId);

        // 최초 system 프롬프트
        if (messages.isEmpty()) {
            Map<String, String> system = new HashMap<>();
            system.put("role", "system");
            system.put("content",
                    promptLoader.load("prompts/romance_scam_money.txt"));
            messages.add(system);
        }

        // 현재 이벤트 system 메시지 갱신
        updateCurrentEventMessage(
                messages,
                testChatMemory.getCurrentEvent(sessionId)
        );

        // 사용자 메시지
        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", request.getMessage());
        messages.add(user);

        String reply = gptService.chatGpt(messages);

        // assistant 로그
        Map<String, String> assistant = new HashMap<>();
        assistant.put("role", "assistant");
        assistant.put("content", reply);
        messages.add(assistant);

        try {
            Map<String, Object> replyMap =
                    objectMapper.readValue(reply, Map.class);

            String gptEvent = (String) replyMap.get("event");
            String currentEvent = testChatMemory.getCurrentEvent(sessionId);

            if (gptEvent != null && currentEvent == null) {

                // 이벤트 라벨 생성 (개인정보요구_1, _2 ...)
                String eventLabel = nextEventLabel(
                        testChatMemory.getEventLogs(sessionId),
                        gptEvent
                );

                // currentEvent는 번호 없는 이벤트명만
                testChatMemory.setCurrentEvent(sessionId, gptEvent);

                // 로그에는 n_이벤트명 저장
                testChatMemory.addEventLog(sessionId, eventLabel, null);
            }
            replyMap.put(
                    "eventLogs",
                    testChatMemory.getEventLogs(sessionId)
            );
            return objectMapper.writeValueAsString(replyMap);

        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 처리 실패", e);
        }
    }

    private void updateCurrentEventMessage(
            List<Map<String, String>> messages,
            String currentEvent
    ) {
        String content = "[현재이벤트 = " + currentEvent + "]";

        for (Map<String, String> msg : messages) {
            if ("system".equals(msg.get("role"))
                    && msg.get("content").startsWith("[현재이벤트")) {
                msg.put("content", content); // ✅ 이제 안전
                return;
            }
        }

        Map<String, String> system = new HashMap<>();
        system.put("role", "system");
        system.put("content", content);
        messages.add(system);
    }
    private String nextEventLabel(
            Map<String, String> eventLogs,
            String eventName
    ) {
        int nextIndex = eventLogs.size() + 1;
        return nextIndex + "_" + eventName;
    }
    public String eventResponse(String sessionId, EventResponseDto request) {

        // 1️⃣ 기존 대화 기록 불러오기
        List<Map<String, String>> messages =
                testChatMemory.getChatLogs(sessionId);

        // 2️⃣ EVENT_RESPONSE 메시지 추가
        String eventContent =
                "[EVENT_RESPONSE]\n" +
                        "{\n" +
                        "  \"event\": \"" + request.getEvent() + "\",\n" +
                        "  \"user_answer\": \"" + request.getAnswer() + "\"\n" +
                        "}";

        messages.add(Map.of(
                "role", "user",
                "content", eventContent
        ));

        // 3️⃣ GPT 호출
        String reply = gptService.chatGpt(messages);

        // 4️⃣ assistant 로그 저장
        messages.add(Map.of(
                "role", "assistant",
                "content", reply
        ));

        try {
            Map<String, Object> replyMap =
                    objectMapper.readValue(reply, Map.class);

            String currentEvent = testChatMemory.getCurrentEvent(sessionId);

            // ✅ 5-1. 기존 이벤트 결과 저장 (n_이벤트명 찾아서 yes/no 기록)
            if (currentEvent != null) {
                testChatMemory.updateCurrentEventMessage(
                        sessionId,
                        request.getAnswer()
                );
            }

            // ✅ 5-2. 이벤트는 여기서 종료 (eventResponse의 역할)
            testChatMemory.setCurrentEvent(sessionId, null);

            // ✅ 5-3. 최신 eventLogs 포함해서 반환
            replyMap.put(
                    "eventLogs",
                    testChatMemory.getEventLogs(sessionId)
            );

            return objectMapper.writeValueAsString(replyMap);

        } catch (Exception e) {
            throw new RuntimeException("이벤트 응답 처리 실패", e);
        }
    }
}