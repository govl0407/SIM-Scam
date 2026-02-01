package com.example.be.controller;

import com.example.be.dto.EventResponseDto;
import com.example.be.dto.userMessageDto;
import com.example.be.service.ChatService;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/test")
public class testController {

    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public testController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody userMessageDto request) {

        String sessionId = "test-session";
        String strJson = chatService.chat(sessionId, request);

        try {
            // GPT가 준 JSON 문자열 → Map으로 변환
            return objectMapper.readValue(strJson, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 JSON 파싱 실패: " + strJson, e);
        }
    }
    @PostMapping("/event-response")
    public Map<String, Object> eventResponse(
            @RequestBody EventResponseDto request
    ) {

        String result = chatService.chatWithEventResponse(
                request.getSessionId(),
                request.getEvent(),
                request.getAnswer()
        );

        try {
            return objectMapper.readValue(result, Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/get")
    public String testGet() {
        return "hello";
    }
}