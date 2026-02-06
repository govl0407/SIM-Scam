<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

//유저/시나리오 식별 (로그인 붙이면 simscam_user_id를 저장한다고 가정)
function getUserIdFromStorage() {
  return localStorage.getItem("simscam_user_id") || "guest";
}
function getLastScenarioFromStorage(userId) {
  return localStorage.getItem(`simscam_last_scenario:${userId}`) || "default";
}

function loadFallbackResult() {
  const userId = getUserIdFromStorage();
  const scenarioId = getLastScenarioFromStorage(userId);

  // 1) 유저+시나리오 결과
  try {
    const s = localStorage.getItem(`scam_result:${userId}:${scenarioId}`);
    if (s) return JSON.parse(s);
  } catch {}

  // 2) 유저 최신 결과
  try {
    const s2 = localStorage.getItem(`scam_result_latest:${userId}`);
    if (s2) return JSON.parse(s2);
  } catch {}

  // 3) 기존 호환
  try {
    const legacy = localStorage.getItem("scam_result");
    if (legacy) return JSON.parse(legacy);
  } catch {}

  return null;
}

const raw =
    route.state?.result ||
    loadFallbackResult() ||
    (route.query.result ? JSON.parse(route.query.result) : null) ||
    { currentEvent: null, eventLogs: {}, sessionId: null, stage: null, userId: null, scenarioId: null, createdAt: null };

const result = computed(() => raw || { currentEvent: null, eventLogs: {}, sessionId: null, stage: null, userId: null, scenarioId: null, createdAt: null });


function formatKoreanDate(msOrIso) {
  const d = msOrIso ? new Date(msOrIso) : new Date();
  if (Number.isNaN(d.getTime())) return "";
  return d.toLocaleDateString("ko-KR", { year: "numeric", month: "long", day: "numeric" });
}
const recordDate = computed(() => formatKoreanDate(result.value?.createdAt));

const userId = computed(() => result.value?.userId || getUserIdFromStorage());
const scenarioId = computed(() => result.value?.scenarioId || getLastScenarioFromStorage(userId.value));

const attemptNo = ref(null);

function getAttemptNoByUserScenario(u, s, sessionId) {
  const MAP_KEY = `simscam_attempt_map:${u}:${s}`;
  const COUNTER_KEY = `simscam_attempt_counter:${u}:${s}`;

  let map = {};
  try {
    map = JSON.parse(localStorage.getItem(MAP_KEY) || "{}");
  } catch {
    map = {};
  }

  if (!sessionId) return null;

  if (map[sessionId]) return Number(map[sessionId]);

  const next = Number(localStorage.getItem(COUNTER_KEY) || "0") + 1;
  localStorage.setItem(COUNTER_KEY, String(next));
  map[sessionId] = next;
  localStorage.setItem(MAP_KEY, JSON.stringify(map));
  return next;
}

const recordTitle = computed(() => (attemptNo.value ? `체험 #${attemptNo.value}` : "이번 체험"));

// 이벤트 템플릿(가이드)
const EVENT_UI = {
  개인정보요구: {
    title: "개인정보 요구",
    level: "위험",
    whyWrong: [
      "연락처, 이메일, 주소는 단순한 정보가 아니라 ‘관계가 현실로 넘어가는 경계선’이에요.",
      "“더 편하게 이야기하자”, “중요한 걸 보내야 한다”는 명목은 전형적인 접근 패턴이에요."
    ],
    guide: [
      "요청 즉시 거절하고 대화 중단",
      "개인 연락처·이메일·주소 요청은 정중히 거절하고 플랫폼 안에서만 대화 유지",
      "개인 정보가 필요한 상황이라고 느껴지면 즉시 거리 두기",
    ],
    phrases: [
      "“개인 연락처나 이메일, 주소는 공유하지 않아요.”",
      "“이야기는 이 플랫폼 안에서만 하고 싶어요.”",
    ],
    score: 3,
  },
  금전요구: {
    title: "금전 요구",
    level: "매우 위험",
    whyWrong: [
      "수수료/보증금/긴급송금은 대표적인 사기 패턴이에요.",
      "시간 압박(오늘까지/지금만)은 위험도를 크게 올려요.",
    ],
    guide: [
      "송금/결제 즉시 중단",
      "이미 보냈다면 은행·결제수단 고객센터에 즉시 연락",
      "대화 기록 캡처 후 신고",
    ],
    phrases: [
      "“돈 관련 요청은 불가능해요.”",
      "“사기 의심돼서 신고하겠습니다.”",
    ],
    score: 5,
  },
  투자권유: {
    title: "투자 권유",
    level: "위험",
    whyWrong: [
      "고수익·원금보장·리딩방은 사기 가능성이 높아요.",
      "출금 제한 후 추가 입금 유도 패턴이 많아요.",
    ],
    guide: [
      "원금/수익 보장 문구는 즉시 경고 신호로 판단",
      "검증된 금융기관/공식 앱 외 링크는 클릭 금지",
      "개인 계좌 입금 유도는 바로 차단",
    ],
    phrases: [
      "“검증되지 않은 투자 제안은 받지 않아요.”",
      "“공식 채널 아닌 링크는 클릭하지 않겠습니다.”",
    ],
    score: 4,
  },
  앱설치유도: {
    title: "앱 설치 유도",
    level: "매우 위험",
    whyWrong: [
      "원격제어/악성앱 설치로 금융앱 탈취가 가능해요.",
      "‘보안앱’ ‘인증앱’ ‘업무앱’이라고 포장하는 경우가 많아요.",
    ],
    guide: [
      "링크/파일 설치 요청 즉시 거절",
      "설치했다면 즉시 삭제 + 보안검사 + 금융앱 비밀번호 변경",
      "공식 스토어 외 설치는 금지",
    ],
    phrases: [
      "“앱 설치는 못 합니다.”",
      "“공식 스토어/공식 채널로만 진행할게요.”",
    ],
    score: 5,
  },
  사이트가입유도: {
    title: "사이트 가입 유도",
    level: "위험",
    whyWrong: [
      "가짜 사이트로 개인정보·카드정보를 수집할 수 있어요.",
      "가입을 빌미로 인증번호 입력을 유도하기도 해요.",
    ],
    guide: [
      "모르는 사이트 가입 금지",
      "URL을 검색/검증(공식 도메인 확인)",
      "의심되면 즉시 중단 + 신고",
    ],
    phrases: [
      "“모르는 사이트 가입은 하지 않아요.”",
      "“공식 도메인 확인 후에만 진행할게요.”",
    ],
    score: 3,
  },
};

// eventLogs → 타임라인 배열
const timeline = computed(() => {
  const logs = result.value?.eventLogs || {};
  const entries = Object.entries(logs);

  const items = entries.map(([key, val]) => {
    const m = String(key).match(/^(\d+)_([^]+)$/);
    const step = m ? Number(m[1]) : null;
    const event = m ? m[2] : key;

    const answer = (val ?? "").toString().toLowerCase(); // "yes"/"no"
    const ui = EVENT_UI[event] || {
      title: event,
      level: "알 수 없음",
      whyWrong: ["이 이벤트에 대한 가이드 템플릿이 아직 없어요."],
      guide: ["EVENT_UI에 템플릿을 추가해 주세요."],
      phrases: [],
      score: 1,
    };

    return { key, step, event, answer, ui };
  });

  items.sort((a, b) => (a.step ?? 9999) - (b.step ?? 9999));
  return items;
});

const wrongNotes = computed(() => timeline.value.filter((t) => t.answer === "yes"));
const correctNotes = computed(() => timeline.value.filter((t) => t.answer === "no"));

/** 위험 점수(yes만 합산) */
const riskScore = computed(() => wrongNotes.value.reduce((sum, t) => sum + (t.ui.score ?? 1), 0));

const escapeResult = computed(() => {
  const s = riskScore.value;
  const wrong = wrongNotes.value.length;

  if (wrong === 0) {
    return {
      title: "✅ 탈출 성공!",
      desc: "위험 신호에 응답하지 않았어요. 다음 시나리오도 도전해볼까요?",
      meta: `놓친 신호 0개 · ESCAPE SCORE ${s}`,
      tone: "ok",
    };
  }
  if (s >= 9) {
    return {
      title: "❌ 탈출에 실패했어요",
      desc: "이번 시뮬레이션에서 위험 신호에 여러 번 응답했어요. 다음엔 바로 중단/차단이 안전해요.",
      meta: `놓친 신호 ${wrong}개 · ESCAPE SCORE ${s}`,
      tone: "bad",
    };
  }
  if (s >= 5) {
    return {
      title: "⚠️ 탈출이 어려웠어요",
      desc: "사기 가능성이 높은 신호에 응답했어요. ‘공식 채널’로 직접 확인하는 습관이 필요해요.",
      meta: `놓친 신호 ${wrong}개 · ESCAPE SCORE ${s}`,
      tone: "warn",
    };
  }
  return {
    title: "⚠️ 거의 탈출했어요",
    desc: "수상한 신호가 있었어요. 다음 대화에선 더 빠르게 거절해보세요.",
    meta: `놓친 신호 ${wrong}개 · ESCAPE SCORE ${s}`,
    tone: "warn",
  };
});

function signalLabel(step) {
  const n = step ?? "-";
  return `위험 신호 ${n}`;
}


onMounted(() => {
  const logs = result.value?.eventLogs || {};
  if (!logs || Object.keys(logs).length === 0) {
    router.replace("/chat");
    return;
  }

  // userId/scenarioId 최신값 저장(다음 fallback을 위해)
  localStorage.setItem(`simscam_last_scenario:${userId.value}`, scenarioId.value);

  // attempt 번호 세팅 (유저별+시나리오별)
  attemptNo.value = getAttemptNoByUserScenario(userId.value, scenarioId.value, result.value?.sessionId);
});

function backToChat() {
  router.push("/chat");
}
function goHome() {
  router.push("/");
}
</script>

<template>
  <div class="page">
    <header class="top">
      <div class="brand">
        <div class="brandName">SIMSCAM: ESCAPE</div>
        <div class="brandSub">대화 결과 분석</div>
        <div class="brandMeta">{{ recordTitle }} · {{ recordDate }}</div>
      </div>
    </header>

    <section class="card">
      <h2 class="h2">요약</h2>

      <div v-if="wrongNotes.length > 0" class="muted" style="margin-top:6px;">
        ⚠️ 위험한 순간이 기록됐어요. 실제 상황이라면 금전/계정 피해로 이어질 수 있습니다.
      </div>
      <div v-else class="muted" style="margin-top:6px;">
        ✅ 안전한 선택을 지켜냈어요. 다음 대화에서도 같은 기준을 유지하면 안전합니다.
      </div>

      <div class="muted" style="margin-top:10px;">
        기록된 위험 신호: {{ wrongNotes.length }}건
      </div>
    </section>


    <section class="card">
      <h2 class="h2">위험 신호 기록</h2>
      <div v-if="timeline.length === 0" class="muted">기록된 위험 신호가 없어요.</div>

      <div v-else class="timeline">
        <div v-for="t in timeline" :key="t.key" class="row">
          <div class="badge">{{ signalLabel(t.step) }}</div>

          <div class="mid">
            <div class="rowTitle">{{ t.ui.title }}</div>
            <div class="rowSub">{{ t.ui.level }}</div>
          </div>

          <div class="right">
            <span class="pill" :class="{ yes: t.answer === 'yes', no: t.answer === 'no' }">
              {{ t.answer === "yes" ? "❌ 위험 신호에 응답함" : "✅ 안전하게 회피함" }}
            </span>
          </div>
        </div>
      </div>
    </section>

    <section class="card" v-if="wrongNotes.length > 0">
      <h2 class="h2">탈출 가이드E</h2>
      <div class="muted">여기서 이렇게 했으면 탈출할 수 있었어요.</div>

      <div v-for="t in wrongNotes" :key="t.key" class="note">
        <div class="noteHead">
          <div class="noteTitle">❌ {{ t.ui.title }}에 걸렸어요</div>
          <span class="pill yes">위험</span>
        </div>

        <div class="block">
          <div class="label">이 선택이 위험한 이유</div>
          <ul>
            <li v-for="(w, i) in t.ui.whyWrong" :key="i">{{ w }}</li>
          </ul>
        </div>

        <div class="block">
          <div class="label">탈출하려면 이렇게 행동하세요</div>
          <ul>
            <li v-for="(g, i) in t.ui.guide" :key="i">{{ g }}</li>
          </ul>
        </div>

        <div class="block" v-if="t.ui.phrases?.length">
          <div class="label">실제 상황에서 이렇게 말하세요</div>
          <ul>
            <li v-for="(p, i) in t.ui.phrases" :key="i">{{ p }}</li>
          </ul>
        </div>
      </div>
    </section>

    <section class="card" v-if="correctNotes.length > 0">
      <h2 class="h2">탈출에 성공한 선택</h2>
      <div class="muted">아래 선택들은 사기 상황에서 올바른 탈출 행동이에요.</div>

      <div v-for="t in correctNotes" :key="t.key" class="good">
        <div class="goodTitle">✅ {{ t.ui.title }}</div>
        <div class="goodDesc">좋아요. 이런 요청은 계속 거절하는 게 안전해요.</div>
      </div>
    </section>

    <section class="card">
      <h2 class="h2">이 대화가 현실이었다면</h2>
      <div class="muted">지금 이 순간을 기준으로 기억하세요.</div>
      <ul class="rules">
        <li><b>신뢰는 증명되기 전까지 먼저 주어지지 않습니다.</b></li>
        <li>급한 부탁은 대부분, 급하게 판단하길 바라는 신호입니다.</li>
        <li><b>불안하다는 감정이 들었다면</b> 그 자체가 신호입니다. 기록하고, 신고하고, 대화를 끊으세요.</li>
      </ul>
    </section>


    <footer class="bottom">
      <button class="btn" @click="backToChat">🔁 다시 탈출 시도하기</button>
      <button class="btn primary" @click="goHome">🎮 다른 시나리오 플레이</button>
    </footer>
  </div>
</template>
<style scoped>
.page{
  min-height: 100dvh;
  height: auto;
  overflow: visible;

  padding: 26px 18px calc(96px + env(safe-area-inset-bottom));

  color: rgba(255, 255, 255, 0.92);

  background:
      radial-gradient(900px 520px at 18% 12%, rgba(168, 85, 247, 0.28), transparent 60%),
      radial-gradient(820px 520px at 78% 18%, rgba(59, 130, 246, 0.22), transparent 62%),
      radial-gradient(900px 700px at 48% 78%, rgba(236, 72, 153, 0.12), transparent 65%),
      linear-gradient(180deg, #070A14 0%, #070A14 40%, #050712 100%);
}


/* 상단 */
.top {
display: flex;
align-items: flex-end;
justify-content: space-between;
margin-bottom: 14px;
gap: 16px;
}

.brandName {
display: inline-flex;
align-items: center;
padding: 6px 12px;
border-radius: 999px;
border: 1px solid rgba(255, 255, 255, 0.10);
background: rgba(255, 255, 255, 0.06);
color: rgba(255, 255, 255, 0.75);
font-weight: 900;
letter-spacing: 0.08em;
font-size: 12px;
}

.brandSub {
margin-top: 10px;
font-size: clamp(28px, 3.2vw, 44px);
font-weight: 950;
line-height: 1.05;
letter-spacing: -0.02em;
color: rgba(255, 255, 255, 0.96);
}

.brandMeta {
margin-top: 10px;
color: rgba(255, 255, 255, 0.72);
font-size: 14px;
line-height: 1.6;
font-weight: 600;
max-width: 56ch;
}

/* 카드(유리질감) */
.card {
background: rgba(255, 255, 255, 0.06);
border: 1px solid rgba(255, 255, 255, 0.10);
border-radius: 18px;
padding: 16px;
margin-top: 14px;
box-shadow: 0 14px 34px rgba(0, 0, 0, 0.35);
backdrop-filter: blur(10px);
}

.muted {
color: rgba(255, 255, 255, 0.62);
font-size: 13px;
}

/* 요약 박스 */
.riskBox {
margin-top: 12px;
border-radius: 16px;
padding: 14px;
border: 1px solid rgba(255, 255, 255, 0.10);
background: rgba(255, 255, 255, 0.06);
}

.riskBox.ok {
border-color: rgba(74, 222, 128, 0.25);
background: rgba(74, 222, 128, 0.08);
}

.riskBox.warn {
border-color: rgba(251, 191, 36, 0.22);
background: rgba(251, 191, 36, 0.08);
}

.riskBox.bad {
border-color: rgba(248, 113, 113, 0.28);
background: rgba(248, 113, 113, 0.10);
}

.riskTitle {
font-weight: 950;
font-size: 18px;
color: rgba(255, 255, 255, 0.95);
}

.riskDesc {
margin-top: 8px;
color: rgba(255, 255, 255, 0.78);
line-height: 1.65;
}

.riskMeta {
margin-top: 10px;
color: rgba(255, 255, 255, 0.68);
font-size: 13px;
font-weight: 700;
}

.riskHint {
margin-top: 10px;
color: rgba(255, 255, 255, 0.55);
font-size: 12px;
}

/* 섹션 타이틀 */
.h2 {
margin: 0 0 10px;
font-size: 16px;
font-weight: 950;
color: rgba(255, 255, 255, 0.92);
letter-spacing: -0.01em;
}

/* 타임라인 */
.timeline {
display: flex;
flex-direction: column;
gap: 10px;
}

.row {
display: grid;
grid-template-columns: 130px 1fr auto;
gap: 12px;
align-items: center;

border-radius: 16px;
padding: 12px;
background: rgba(255, 255, 255, 0.04);
border: 1px solid rgba(255, 255, 255, 0.08);
}

.badge {
display: inline-flex;
align-items: center;
justify-content: center;
padding: 7px 10px;
border-radius: 999px;
border: 1px solid rgba(255, 255, 255, 0.12);
background: rgba(255, 255, 255, 0.06);
font-weight: 900;
font-size: 12px;
color: rgba(255, 255, 255, 0.82);
}

.rowTitle {
font-weight: 950;
color: rgba(255, 255, 255, 0.92);
}

.rowSub {
color: rgba(255, 255, 255, 0.62);
font-size: 12px;
margin-top: 3px;
}

.pill {
display: inline-flex;
align-items: center;
font-size: 12px;
padding: 7px 10px;
border-radius: 999px;
border: 1px solid rgba(255, 255, 255, 0.12);
background: rgba(255, 255, 255, 0.06);
font-weight: 900;
color: rgba(255, 255, 255, 0.82);
}

.pill.yes {
border-color: rgba(248, 113, 113, 0.35);
background: rgba(248, 113, 113, 0.12);
}

.pill.no {
border-color: rgba(74, 222, 128, 0.28);
background: rgba(74, 222, 128, 0.10);
}

/* 오답노트 카드 */
.note {
margin-top: 12px;
border-radius: 16px;
padding: 14px;
background: rgba(255, 255, 255, 0.05);
border: 1px solid rgba(255, 255, 255, 0.10);
}

.noteHead {
display: flex;
justify-content: space-between;
align-items: center;
gap: 10px;
}

.noteTitle {
font-weight: 950;
color: rgba(255, 255, 255, 0.92);
}

.block {
margin-top: 12px;
}

.label {
font-weight: 950;
margin-bottom: 6px;
color: rgba(255, 255, 255, 0.88);
}

ul {
margin: 0;
padding-left: 18px;
color: rgba(255, 255, 255, 0.78);
line-height: 1.7;
}

.rules {
margin-top: 8px;
}

/* 잘한 대응 */
.good {
margin-top: 10px;
border-radius: 16px;
padding: 12px;
background: rgba(74, 222, 128, 0.08);
border: 1px solid rgba(74, 222, 128, 0.18);
}

.goodTitle {
font-weight: 950;
color: rgba(255, 255, 255, 0.92);
}

.goodDesc {
color: rgba(255, 255, 255, 0.72);
font-size: 14px;
margin-top: 4px;
}

/* 하단 버튼 */
.bottom {
display: flex;
justify-content: flex-end;
gap: 12px;
margin-top: 18px;
}

.btn {
border: 1px solid rgba(255, 255, 255, 0.12);
background: rgba(255, 255, 255, 0.08);
color: rgba(255, 255, 255, 0.92);
padding: 10px 14px;
border-radius: 12px;
cursor: pointer;
font-weight: 950;
}

.btn:hover {
background: rgba(255, 255, 255, 0.12);
}

.btn.primary {
background: rgba(255, 255, 255, 0.92);
color: #0B1020;
border-color: rgba(255, 255, 255, 0.0);
}

.btn.primary:hover {
background: rgba(255, 255, 255, 0.98);
}

@media (max-width: 640px) {
.row { grid-template-columns: 1fr; gap: 8px; }
.right { justify-self: start; }
}
</style>