import axios from "axios";

const BASE = "http://localhost:8080";

// 채팅 보내기
export async function sendChat(message) {
    const res = await axios.post(`${BASE}/test/chat`, { message });
    return res.data; // { text, image, events }
}

//  예/아니오 선택 보내기
export async function sendDecision(sessionId, eventId, choice) {
    const res = await axios.post(`${BASE}/event/decision`, {
        sessionId,
        eventId,
        choice, // "YES" | "NO"
    });
    return res.data;
}
