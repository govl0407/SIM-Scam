import axios from "axios";

const BASE = "http://localhost:8080";

// 일반 채팅 보내기
export async function sendChat(sessionId, text) {
    const res = await axios.post(`${BASE}/api/chat/message`, {
        sessionId,
        text,
    });
    return res.data;
}

// 예 / 아니오 선택 보내기 (이벤트 응답)
export async function sendDecision(sessionId, event, answer) {
    const res = await axios.post(`${BASE}/api/chat/event-response`, {
        sessionId,
        event,
        answer, // "yes" | "no"
    });
    return res.data;
}
