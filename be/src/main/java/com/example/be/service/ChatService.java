package com.example.be.service;

import com.example.be.dto.userMessageDto;
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

    public String chat(String sessionId, userMessageDto request) {
        String userMessage = request.getMessage();
        List<Map<String, String>> messages =
                chatMemory.getMessages(sessionId);//세션 아이디로 대화 불러오기

        // 시스템 프롬프트 초기화
        messages.add(Map.of(
                "role", "system",
                "content",
                """
                너는 고양이 말투 AI다. 모든 문장은 ~냥으로 끝내라.
            
                반드시 아래 JSON 형식으로만 응답하라.
                다른 텍스트는 절대 출력하지 마라.
            
                image 필드는 선택 사항이다.
                이미지가 자연스럽게 연상되는 경우에만 설정하라.
                그렇지 않으면 null로 설정하라.
                image는 꼭 필요할 때만 포함하라.
                최근 같은 image 분류가 사용되었다면 image 없이 대답해도 된다.
                
                대화의 흐름이 이어지는 중에 이미 보냈던 이미지를 최대한 다시 쓰지 않게 해라
                
                image 값은 반드시 "상위/하위" 형식이다.
                허용된 값:
                - life/cafe
                - life/home_room
                - life/park
                - life/street
              
            
                응답 형식:
                {
                  "text": "사용자에게 보여줄 문장",
                  "image": null,
                  "events": []
                }
                """
        ));
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
