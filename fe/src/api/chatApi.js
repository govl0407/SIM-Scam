import axios from "axios";

const api = axios.create({
    baseURL: "http://211.188.56.185:8080",
    //  이제 쿠키 세션에 의존하지 않음 (남겨도 되지만 의미가 거의 없음)
    withCredentials: true,
});

function normalizeScenario(s) {
    const v = (s ?? "").toString().trim();
    return v || "romance";
}

/**  HTTPS/HTTP 상관없이 고정되는 클라이언트 sid */
function getClientSid() {
    let sid = localStorage.getItem("simscam_client_sid");
    if (!sid) {
        sid = "sid_" + Math.random().toString(36).slice(2) + Date.now().toString(36);
        localStorage.setItem("simscam_client_sid", sid);
    }
    return sid;
}

export async function sendChat(text, opts = {}) {
    const scenario = normalizeScenario(opts.scenario);
    const sid = getClientSid();

    const res = await api.post(
        "/api/chat/message",
        { message: text },
        { params: { scenario, sid } } //  sid 추가
    );

    return res.data;
}

export async function sendDecision(event, answer, opts = {}) {
    const scenario = normalizeScenario(opts.scenario);
    const sid = getClientSid();

    const res = await api.post(
        "/api/chat/event-response",
        { event, answer },
        { params: { scenario, sid } } // sid 추가
    );

    return res.data;
}

export async function getPersona(scenarioOrOpts = {}) {
    const scenario =
        typeof scenarioOrOpts === "string"
            ? normalizeScenario(scenarioOrOpts)
            : normalizeScenario(scenarioOrOpts?.scenario);

    const sid = getClientSid();

    const res = await api.get("/api/chat/persona", {
        params: { scenario, sid }, //  sid 추가 (선택이지만 통일)
    });

    return res.data;
}

export async function resetChat(opts = {}) {
    const sid = getClientSid();
    const scenario = (opts.scenario ?? "").toString().trim();

    const params = scenario ? { sid, scenario } : { sid };

    const res = await api.get("/api/chat/reset", { params });
    return res.data; // { message, status }
}