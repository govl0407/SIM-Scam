import axios from "axios";

const api = axios.create({
    baseURL: "http://211.188.56.185:8080",
    withCredentials: true,
});

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
            if (!content.trim()) return null;
            if (role !== "user" && role !== "assistant") return null;
            return { role, content };
        })
        .filter(Boolean);
}

export async function sendChat(text, opts = {}) {
    const scenario = normalizeScenario(opts.scenario);
    const history = normalizeHistory(opts.history);

    const res = await api.post(
        "/api/chat/message",
        { message: text, history },
        { params: { scenario } }
    );

    return res.data;
}

export async function sendDecision(event, answer, opts = {}) {
    const scenario = normalizeScenario(opts.scenario);
    const history = normalizeHistory(opts.history);

    const res = await api.post(
        "/api/chat/event-response",
        { event, answer, history },
        { params: { scenario } }
    );

    return res.data;
}
