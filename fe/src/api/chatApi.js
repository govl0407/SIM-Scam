import axios from "axios";

const BASE = "http://211.188.56.185:8080";

function normalizeScenario(s) {
    const v = (s ?? "").toString().trim();
    return v || "romance";
}

function normalizeHistory(history) {
    if (!Array.isArray(history)) return [];

    return history
        .map((m) => {
            const role = (m?.role ?? "").toString().trim();
            const content = (m?.content ?? m?.text ?? "").toString();

            // role은 user/assistant만 허용 (system은 서버에서 붙이는 걸 추천)
            if (!content.trim()) return null;
            if (role !== "user" && role !== "assistant") return null;

            return { role, content };
        })
        .filter(Boolean);
}

// 일반 채팅 보내기
// sendChat(text, { scenario, history })
export async function sendChat(text, opts = {}) {
    const scenario = normalizeScenario(opts.scenario);
    const history = normalizeHistory(opts.history);

    const res = await axios.post(
        `${BASE}/api/chat/message`,
        {
            message: text,
            history,
        },
        { params: { scenario } }
    );

    return res.data;
}

// 예 / 아니오 선택 보내기 (이벤트 응답)
// sendDecision(event, answer, { scenario, history })
export async function sendDecision(event, answer, opts = {}) {
    const scenario = normalizeScenario(opts.scenario);
    const history = normalizeHistory(opts.history);

    const res = await axios.post(
        `${BASE}/api/chat/event-response`,
        {
            event,
            answer,  // "yes" | "no"
            history,
        },
        { params: { scenario } }
    );

    return res.data;
}
