package com.example.be.service;

import com.example.be.dto.EventResponseDto;
import com.example.be.dto.userMessageDto;
import com.example.be.prompts.PromptLoader;
import com.example.be.prompts.ScenarioType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ChatService {

    private final ChatMemory ChatMemory;
    private final PromptLoader promptLoader;
    private final gptService gptService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatService(ChatMemory ChatMemory, gptService gptService, PromptLoader promptLoader) {
        this.ChatMemory = ChatMemory;
        this.gptService = gptService;
        this.promptLoader = promptLoader;
    }

    public String chat(String sessionId, userMessageDto request, String scenarioKey) {
        // 1. 키 조합 (세션별 + 시나리오별 독립 공간 확보)
        String compositeKey = sessionId + ":" + scenarioKey;
        List<Map<String, String>> messages = ChatMemory.getChatLogs(compositeKey);

        if (messages.isEmpty()) {
            // 2. 매핑 테이블에서 안전하게 경로 조회
            String safePath = ScenarioType.getPath(scenarioKey);
            messages.add(Map.of("role", "system", "content", promptLoader.load(safePath)));
        }

        // 2️⃣ 이전 대화에 섞여있던 [현재이벤트] 시스템 메시지는 제거 (중복 및 혼선 방지)
        messages.removeIf(msg -> "system".equals(msg.get("role")) && msg.get("content").startsWith("[현재이벤트"));

        // 3️⃣ 사용자 메시지 추가
        messages.add(Map.of("role", "user", "content", request.getMessage()));

        // 4️⃣ 현재 진행 중인 이벤트가 있다면, GPT 응답 직전에 가장 강력한 지침으로 주입
        String currentEvent = ChatMemory.getCurrentEvent(compositeKey);
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
                    String eventLabel = nextEventLabel(ChatMemory.getEventLogs(compositeKey), gptEvent);
                    ChatMemory.setCurrentEvent(compositeKey, gptEvent);
                    ChatMemory.addEventLog(compositeKey, eventLabel, null);
                }
            }

            // 7️⃣ assistant 로그 저장 (시스템 메시지는 제외하고 대화 내용만 저장하는 것이 깔끔함)
            messages.add(Map.of("role", "assistant", "content", reply));

            // 결과 반환
            replyMap.put("currentEvent", ChatMemory.getCurrentEvent(compositeKey));
            replyMap.put("eventLogs", ChatMemory.getEventLogs(compositeKey));
            return objectMapper.writeValueAsString(replyMap);

        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 처리 실패", e);
        }
    }

    private String nextEventLabel(Map<String, String> eventLogs, String eventName) {
        int nextIndex = eventLogs.size() + 1;
        return nextIndex + "_" + eventName;
    }
    public String eventResponse(String sessionId, EventResponseDto request, String scenarioKey) {
        String compositeKey = sessionId + ":" + scenarioKey;
        List<Map<String, String>> messages = ChatMemory.getChatLogs(compositeKey);

        // 1. 사용자 응답 추가
        String eventContent = "[EVENT_RESPONSE]\n{\n  \"event\": \"" + request.getEvent() + "\",\n  \"user_answer\": \"" + request.getAnswer() + "\"\n}";
        messages.add(Map.of("role", "user", "content", eventContent));

        // 2. GPT 답변 생성
        String reply = gptService.chatGpt(messages);
        messages.add(Map.of("role", "assistant", "content", reply));

        try {
            // 3. 🎯 메모리 업데이트 (순서 중요!)
            // 현재 떠있는 currentEvent 라벨을 찾아 사용자의 answer를 먼저 저장
            ChatMemory.updateCurrentEventMessage(compositeKey, request.getAnswer());

            // 4. 저장이 끝난 후 이벤트를 종료(null) 상태로 변경
            ChatMemory.setCurrentEvent(compositeKey, null);

            Map<String, Object> replyMap = objectMapper.readValue(reply, Map.class);
            replyMap.put("event", null);
            replyMap.put("eventLogs", ChatMemory.getEventLogs(compositeKey));

            return objectMapper.writeValueAsString(replyMap);
        } catch (Exception e) {
            throw new RuntimeException("이벤트 응답 처리 실패", e);
        }
    }

    public Map<String, String> getPersonaInfo(String scenarioKey) {
        String safePath = ScenarioType.getPath(scenarioKey);
        String fullPrompt = promptLoader.load(safePath);

        Map<String, String> personaMap = new HashMap<>();

        try {
            // 1. "[AI 페르소나]" 섹션 위치 찾기
            String sectionHeader = "[AI 페르소나]";
            int startIndex = fullPrompt.indexOf(sectionHeader);
            if (startIndex == -1) return personaMap;

            // 2. 해당 섹션 이후의 내용만 추출 (다음 섹션 '[' 전까지)
            String content = fullPrompt.substring(startIndex + sectionHeader.length());
            int nextSectionIndex = content.indexOf("[");
            if (nextSectionIndex != -1) {
                content = content.substring(0, nextSectionIndex);
            }

            // 3. 줄바꿈으로 나누어 key : value 추출
            String[] lines = content.split("\n");
            for (String line : lines) {
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    personaMap.put(key, value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("페르소나 정보 파싱 실패", e);
        }

        return personaMap;
    }
}