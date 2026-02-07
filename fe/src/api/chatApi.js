import axios from "axios";

const api = axios.create({
    baseURL: "http://211.188.56.185:8080",
    withCredentials: true,
});

function normalizeScenario(s) {
    const v = (s ?? "").toString().trim();
    return v || "romance";
}

export async function sendChat(text, opts = {}) {
    const scenario = normalizeScenario(opts.scenario);

    const res = await api.post(
        "/api/chat/message",
        { message: text },
        { params: { scenario } }
    );

    return res.data;
}

export async function sendDecision(event, answer, opts = {}) {
    const scenario = normalizeScenario(opts.scenario);

    const res = await api.post(
        "/api/chat/event-response",
        { event, answer },
        { params: { scenario } }
    );

    return res.data;
}
