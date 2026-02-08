package com.example.be.service;

import com.example.be.dto.userMessageDto;
import com.example.be.prompts.PromptLoader;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final ChatMemory chatMemory;
    private final gptService gptService;
    private final PromptLoader promptLoader;

    public ChatService(ChatMemory chatMemory,
                       gptService gptService,
                       PromptLoader promptLoader) {
        this.chatMemory = chatMemory;
        this.gptService = gptService;
        this.promptLoader = promptLoader;
    }

    public String chat(String sessionId, userMessageDto request) {

        List<Map<String, String>> messages =
                chatMemory.getMessages(sessionId);

        String systemPrompt =
                promptLoader.load("prompts/scam_chat_system.txt");

        messages.add(Map.of(
                "role", "system",
                "content", systemPrompt
        ));

        messages.add(Map.of(
                "role", "user",
                "content", request.getMessage()
        ));

        String reply = gptService.chatGpt(messages);

        messages.add(Map.of(
                "role", "assistant",
                "content", reply
        ));

        return """
        {
          "text": "%s",
          "image": null,
          "events": [
            {
              "type": "INVEST_REQUEST",
              "value": {
                "eventId": "INVEST_001",
                "question": "투자 하시겠습니까?"
              }
            }
          ]
        }
        """.formatted(escapeJson(reply));
    }


    private String escapeJson(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
