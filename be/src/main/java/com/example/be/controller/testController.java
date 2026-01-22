package com.example.be.controller;

import com.example.be.dto.testDto;
import com.example.be.service.ChatMemory;
import com.example.be.service.gptService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*") // 프론트 테스트용 (나중에 제한)
public class testController {
    private final gptService gptService;
    private final ChatMemory chatMemory;

    public testController(ChatMemory chatMemory, gptService gptService) {
        this.chatMemory = chatMemory;
        this.gptService = gptService;
    }

    @PostMapping("/test/text")
    public String receiveChat(@RequestBody testDto request) {
        return gptService.textGpt(request.getMessage());
    }
    @PostMapping("/test/chat")
    public String chat(@RequestBody testDto request) {

        String sessionId = "test-session"; // 나중에 userId로 변경

        List<Map<String, String>> messages =
                chatMemory.getMessages(sessionId);
        //기본 설정
        if (messages.isEmpty()) {
            messages.add(Map.of(
                    "role", "system",
                    "content", "항상 대화는 ~냥으로 끝내"
            ));
        }

        // (1) 사용자 메시지 저장
        messages.add(Map.of(
                "role", "user",
                "content", request.getMessage()
        ));

        // (2) GPT 호출
        String reply = gptService.chatGpt(messages);

        // (3) GPT 답변 저장
        messages.add(Map.of(
                "role", "assistant",
                "content", reply
        ));

        return reply;
    }

    @GetMapping("/test/get")
    public String testGet(){
        return "hello";
    }

}

