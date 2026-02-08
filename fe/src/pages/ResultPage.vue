<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { resetChat } from "../api/chatApi";

const route = useRoute();
const router = useRouter();

const DEFAULT_RESULT = {
  currentEvent: null,
  eventLogs: {},
  stage: null,
  userId: null,
  scenarioId: null,
  trackId: null,
  createdAt: null,
};

function getUserIdFromStorage() {
  return localStorage.getItem("simscam_user_id") || "guest";
}
function getLastScenarioFromStorage(userId) {
  return localStorage.getItem(`simscam_last_scenario:${userId}`) || "romance";
}
function getLastTrackFromStorage(userId) {
  return localStorage.getItem(`simscam_last_track:${userId}`) || "romance";
}

function safeParse(json) {
  try {
    return JSON.parse(json);
  } catch {
    return null;
  }
}

// localStorage에서 결과 로드(최신 결과 우선 + 레거시 호환)
function loadFallbackResult() {
  const userId = getUserIdFromStorage();
  const lastScenario = getLastScenarioFromStorage(userId);
  const lastTrack = getLastTrackFromStorage(userId);

  // 유저 최신 결과
  try {
    const s2 = localStorage.getItem(`scam_result_latest:${userId}`);
    if (s2) return JSON.parse(s2);
  } catch {}

  // 마지막 트랙/시나리오 조합 먼저
  try {
    const directKey = `scam_result:${userId}:${lastTrack}:${lastScenario}`;
    const v = localStorage.getItem(directKey);
    if (v) return JSON.parse(v);
  } catch {}

  // 신키 탐색(유저 기준) - 마지막 시나리오 우선 매칭
  try {
    const keys = Object.keys(localStorage);
    for (const k of keys) {
      // scam_result:<userId>:<trackId>:<scenarioId>
      if (k.startsWith(`scam_result:${userId}:`) && k.endsWith(`:${lastScenario}`)) {
        const v = localStorage.getItem(k);
        if (v) return JSON.parse(v);
      }
    }
  } catch {}

  // 구키(user:scenario)
  try {
    const s = localStorage.getItem(`scam_result:${userId}:${lastScenario}`);
    if (s) return JSON.parse(s);
  } catch {}

  // 레거시 단일 키
  try {
    const legacy = localStorage.getItem("scam_result");
    if (legacy) return JSON.parse(legacy);
  } catch {}

  return null;
}

const raw = computed(() => {
  const fromState = route.state?.result ?? null;
  if (fromState) return fromState;

  const fromFallback = loadFallbackResult();
  if (fromFallback) return fromFallback;

  const fromQuery = route.query?.result ? safeParse(route.query.result) : null;
  if (fromQuery) return fromQuery;

  return DEFAULT_RESULT;
});

const result = computed(() => raw.value || DEFAULT_RESULT);


//  날짜/식별자
function formatKoreanDate(msOrIso) {
  const d = msOrIso ? new Date(msOrIso) : new Date();
  if (Number.isNaN(d.getTime())) return "";
  return d.toLocaleDateString("ko-KR", { year: "numeric", month: "long", day: "numeric" });
}
const recordDate = computed(() => formatKoreanDate(result.value?.createdAt));

const userId = computed(() => result.value?.userId || getUserIdFromStorage());
const scenarioId = computed(() => result.value?.scenarioId || getLastScenarioFromStorage(userId.value));
const trackId = computed(() => result.value?.trackId || getLastTrackFromStorage(userId.value) || "romance");

const attemptNo = ref(null);

function getAttemptNoByUserTrackScenario(u, t, s, createdAt) {
  if (!createdAt) return null;

  const arr = [];
  try {
    const keys = Object.keys(localStorage);
    for (const k of keys) {
      if (!k.startsWith(`scam_result:${u}:`)) continue;

      const v = localStorage.getItem(k);
      if (!v) continue;

      const obj = JSON.parse(v);

      if ((obj?.trackId || null) !== t) continue;
      if ((obj?.scenarioId || null) !== s) continue;
      if (!obj?.createdAt) continue;

      arr.push(obj.createdAt);
    }
  } catch {}

  const uniq = Array.from(new Set(arr)).sort((a, b) => a - b);
  const idx = uniq.indexOf(createdAt);
  return idx >= 0 ? idx + 1 : null;
}

const recordTitle = computed(() => (attemptNo.value ? `체험 #${attemptNo.value}` : "이번 체험"));


const EVENT_UI = {
  개인정보요구: {
    title: "개인정보 요구",
    level: "위험",
    whyWrong: [
      "연락처, 이메일, 주소는 단순한 정보가 아니라 ‘관계가 현실로 넘어가는 경계선’이에요.",
      "“더 편하게 이야기하자”, “중요한 걸 보내야 한다”는 명목은 전형적인 접근 패턴이에요.",
    ],
    guide: [
      "요청 즉시 거절하고 대화 중단",
      "개인 연락처·이메일·주소 요청은 정중히 거절하고 플랫폼 안에서만 대화 유지",
      "개인 정보가 필요한 상황이라고 느껴지면 즉시 거리 두기",
    ],
    phrases: ["“개인 연락처나 이메일, 주소는 공유하지 않아요.”", "“이야기는 이 플랫폼 안에서만 하고 싶어요.”"],
    score: 3,
  },
  금전요구: {
    title: "금전 요구",
    level: "매우 위험",
    whyWrong: [
      "상대는 개인적인 어려움(생활비, 비자 문제, 갑작스러운 위기)을 이유로 \n당신의 연민과 책임감을 자극해 돈을 요청했어요.",
    ],
    guide: ["송금/결제 즉시 중단", "이미 보냈다면 은행·결제수단 고객센터에 즉시 연락", "대화 기록 캡처 후 신고"],
    phrases: ["“돈 관련 요청은 불가능해요.”", "“사기 의심돼서 신고하겠습니다.”"],
    score: 5,
  },
  투자권유: {
    title: "투자 권유",
    level: "위험",
    whyWrong: ["고수익·원금보장·리딩방은 사기 가능성이 높아요.", "출금 제한 후 추가 입금 유도 패턴이 많아요."],
    guide: ["원금/수익 보장 문구는 즉시 경고 신호로 판단", "검증된 금융기관/공식 앱 외 링크는 클릭 금지", "개인 계좌 입금 유도는 바로 차단"],
    phrases: ["“검증되지 않은 투자 제안은 받지 않아요.”", "“공식 채널 아닌 링크는 클릭하지 않겠습니다.”"],
    score: 4,
  },
  앱설치유도: {
    title: "앱 설치 유도",
    level: "매우 위험",
    whyWrong: ["원격제어/악성앱 설치로 금융앱 탈취가 가능해요.", "‘보안앱’ ‘인증앱’ ‘업무앱’이라고 포장하는 경우가 많아요."],
    guide: ["링크/파일 설치 요청 즉시 거절", "설치했다면 즉시 삭제 + 보안검사 + 금융앱 비밀번호 변경", "공식 스토어 외 설치는 금지"],
    phrases: ["“앱 설치는 못 합니다.”", "“공식 스토어/공식 채널로만 진행할게요.”"],
    score: 5,
  },
  사이트가입유도: {
    title: "사이트 가입 유도",
    level: "위험",
    whyWrong: ["가짜 사이트로 개인정보·카드정보를 수집할 수 있어요.", "가입을 빌미로 인증번호 입력을 유도하기도 해요."],
    guide: ["모르는 사이트 가입 금지", "URL을 검색/검증(공식 도메인 확인)", "의심되면 즉시 중단 + 신고"],
    phrases: ["“모르는 사이트 가입은 하지 않아요.”", "“공식 도메인 확인 후에만 진행할게요.”"],
    score: 3,
  },
};


const timeline = computed(() => {
  const logs = result.value?.eventLogs || {};
  const entries = Object.entries(logs);

  const items = entries.map(([key, val]) => {
    const m = String(key).match(/^(\d+)_([^]+)$/);
    const step = m ? Number(m[1]) : null;
    const event = m ? m[2] : key;

    const answer = (val ?? "").toString().toLowerCase(); // "yes"/"no"
    const ui =
        EVENT_UI[event] || {
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

const resetting = ref(false);
const resetMsg = ref("");

// 결과/진행 관련 로컬스토리지 정리
function clearResultAndProgressStorage({ scope = "scenario" } = {}) {
  const u = userId.value || getUserIdFromStorage();
  const t = trackId.value || "romance";
  const s = scenarioId.value || "romance";

  // fallback에서 쓰는 키들 정리
  localStorage.removeItem(`scam_result_latest:${u}`);
  localStorage.removeItem(`scam_result:${u}:${t}:${s}`);
  localStorage.removeItem(`scam_result:${u}:${s}`); // 구키 호환
  localStorage.removeItem("scam_result"); // 레거시

  // 채팅 진행 상태
  localStorage.removeItem(`simscam_active_scenario:${u}:${t}`);

  if (scope === "all") {
    localStorage.removeItem(`simscam_last_scenario:${u}`);
    localStorage.removeItem(`simscam_last_track:${u}`);
  }
}


async function resetThisScenario() {
  resetting.value = true;
  resetMsg.value = "";
  try {
    const data = await resetChat({ scenario: scenarioId.value }); // /reset?sid&scenario
    clearResultAndProgressStorage({ scope: "scenario" });
    resetMsg.value = data?.message || "초기화 완료";
    router.replace({ path: "/chat", query: { scenario: scenarioId.value } });
  } catch (e) {
    resetMsg.value = e?.message || "초기화 실패";
  } finally {
    resetting.value = false;
  }
}


async function resetAll() {
  resetting.value = true;
  resetMsg.value = "";
  try {
    const data = await resetChat(); // /reset?sid
    clearResultAndProgressStorage({ scope: "all" });
    resetMsg.value = data?.message || "전체 초기화 완료";
    router.replace("/");
  } catch (e) {
    resetMsg.value = e?.message || "초기화 실패";
  } finally {
    resetting.value = false;
  }
}

/* =========================
 *  Mount
 * ========================= */

onMounted(() => {
  const logs = result.value?.eventLogs || {};
  if (!logs || Object.keys(logs).length === 0) {
    router.replace("/chat");
    return;
  }

  // 다음 fallback을 위해 최신 track/scenario 저장
  localStorage.setItem(`simscam_last_scenario:${userId.value}`, scenarioId.value);
  localStorage.setItem(`simscam_last_track:${userId.value}`, trackId.value);

  // attempt 번호 세팅 (유저별+트랙별+시나리오별, createdAt 기준)
  attemptNo.value = getAttemptNoByUserTrackScenario(
      userId.value,
      trackId.value,
      scenarioId.value,
      result.value?.createdAt
  );
});

function backToChat() {
  const u = getUserIdFromStorage();
  const t = trackId.value || "romance";
  localStorage.removeItem(`simscam_active_scenario:${u}:${t}`);
  router.push("/chat");
}

function goHome() {
  const u = getUserIdFromStorage();
  const t = trackId.value || "romance";
  localStorage.removeItem(`simscam_active_scenario:${u}:${t}`);
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

      <div v-if="wrongNotes.length > 0" class="muted" style="margin-top: 6px">
        ⚠️ 위험한 순간이 기록됐어요. 실제 상황이라면 금전/계정 피해로 이어질 수 있습니다.
      </div>
      <div v-else class="muted" style="margin-top: 6px">
        ✅ 안전한 선택을 지켜냈어요. 다음 대화에서도 같은 기준을 유지하면 안전합니다.
      </div>

      <div class="muted" style="margin-top: 10px">기록된 위험 신호: {{ wrongNotes.length }}건</div>
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
      <h2 class="h2">탈출 가이드</h2>
      <div class="muted">여기서 이렇게 했으면 탈출할 수 있었어요.</div>

      <div v-for="t in wrongNotes" :key="t.key" class="note">
        <div class="noteHead">
          <div class="noteTitle">❌ {{ t.ui.title }}에 걸렸어요</div>
          <span class="pill yes">위험</span>
        </div>

        <div class="block">
          <div class="label">이 선택이 위험한 이유</div>
          <ul>
            <li v-for="(w, i) in t.ui.whyWrong" :key="i" class="preLine">{{ w }}</li>
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
        <li>
          <b>불안하다는 감정이 들었다면</b> 그 자체가 신호입니다. 기록하고, 신고하고, 대화를 끊으세요.
        </li>
      </ul>
    </section>

    <footer class="bottom">
      <button class="btn" @click="backToChat">🔁 다시 탈출 시도하기</button>
      <button class="btn primary" @click="goHome">🎮 다른 시나리오 플레이</button>

      <button class="btn" :disabled="resetting" @click="resetThisScenario">
        {{ resetting ? "초기화 중..." : "🧹 이 시나리오 초기화" }}
      </button>
      <button class="btn danger" :disabled="resetting" @click="resetAll">
        {{ resetting ? "초기화 중..." : "🧨 전체 기록 초기화" }}
      </button>

      <div v-if="resetMsg" class="muted" style="margin-top: 10px">{{ resetMsg }}</div>
    </footer>
  </div>
</template>

<style scoped>
.page {
  min-height: 100dvh;
  height: auto;
  overflow: visible;

  padding: 26px 18px calc(96px + env(safe-area-inset-bottom));

  color: rgba(255, 255, 255, 0.92);

  background: radial-gradient(900px 520px at 18% 12%, rgba(168, 85, 247, 0.28), transparent 60%),
  radial-gradient(820px 520px at 78% 18%, rgba(59, 130, 246, 0.22), transparent 62%),
  radial-gradient(900px 700px at 48% 78%, rgba(236, 72, 153, 0.12), transparent 65%),
  linear-gradient(180deg, #070a14 0%, #070a14 40%, #050712 100%);
}


.btn.danger {
  border: 1px solid rgba(255, 120, 120, 0.55);
  background: rgba(255, 80, 80, 0.1);
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 카드 레이아웃 */
.card{
  margin-top: 14px;
  padding: 16px;
  border-radius: 18px;
  border: 1px solid rgba(255,255,255,0.08);
  background: rgba(255,255,255,0.06);
  backdrop-filter: blur(10px);
  box-shadow: 0 18px 50px rgba(0,0,0,0.25);
}

.top{
  padding: 6px 0 10px;
}

.h2{
  margin: 0;
  font-size: 18px;
  font-weight: 900;
  letter-spacing: -0.2px;
}

.muted{
  opacity: 0.78;
  font-size: 13px;
}

/* 타임라인 */
.timeline{
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.row{
  display: grid;
  grid-template-columns: 120px 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 12px 12px;
  border-radius: 16px;
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,255,255,0.08);
}

.badge{
  font-size: 12px;
  font-weight: 900;
  padding: 6px 10px;
  border-radius: 999px;
  width: fit-content;
  background: rgba(255,255,255,0.10);
  border: 1px solid rgba(255,255,255,0.10);
}

.rowTitle{
  font-weight: 900;
  font-size: 14px;
}

.rowSub{
  opacity: 0.75;
  font-size: 12px;
  margin-top: 2px;
}

.pill{
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 900;
  padding: 8px 10px;
  border-radius: 999px;
  border: 1px solid rgba(255,255,255,0.12);
  background: rgba(255,255,255,0.06);
  white-space: nowrap;
}

.pill.yes{
  border-color: rgba(255,80,80,0.35);
  background: rgba(255,80,80,0.12);
}

.pill.no{
  border-color: rgba(110,231,183,0.35);
  background: rgba(110,231,183,0.12);
}

/* 오답노트 카드 */
.note{
  margin-top: 12px;
  padding: 14px;
  border-radius: 18px;
  border: 1px solid rgba(255,80,80,0.20);
  background: rgba(255,80,80,0.08);
}

.noteHead{
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.noteTitle{
  font-weight: 900;
}

.block{
  margin-top: 10px;
  padding: 12px;
  border-radius: 16px;
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.08);
}

.label{
  font-weight: 900;
  margin-bottom: 6px;
}

.preLine{
  white-space: pre-line;
}

/* 정답 */
.good{
  margin-top: 10px;
  padding: 12px;
  border-radius: 16px;
  border: 1px solid rgba(110,231,183,0.20);
  background: rgba(110,231,183,0.08);
}

.goodTitle{ font-weight: 900; }
.goodDesc{ opacity: 0.8; margin-top: 4px; }

/* 규칙 리스트 */
.rules{
  margin: 10px 0 0;
  padding-left: 18px;
}
.rules li{ margin: 6px 0; }
/* 하단 버튼 영역 수정 */
.bottom {
  /* 화면 하단에 고정 */
  position: sticky;
  bottom: 0;
  left: 0;
  right: 0;

  /* 페이지 기본 padding(-18px)을 상쇄하여 가로로 꽉 채움 */
  margin: 16px -18px 0 -18px;

  /* 상단 경계선과 배경 효과 (Glassmorphism) */
  padding: 16px 18px calc(16px + env(safe-area-inset-bottom));
  background: linear-gradient(
      to top,
      rgba(7, 10, 20, 1) 0%,      /* 하단은 배경과 동일하게 어둡게 */
      rgba(7, 10, 20, 0.95) 60%,  /* 버튼 영역은 불투명하게 */
      rgba(7, 10, 20, 0) 100%     /* 상단은 자연스러운 그라데이션 */
  );
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);

  /* 다른 요소 위로 배치 */
  z-index: 100;

  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
/* 하단 버튼 영역 수정 */
.bottom {
  position: sticky;
  bottom: 0;
  left: 0;
  right: 0;

  /* 가로로 꽉 채우기 위해 마진 조정 */
  margin: 16px -18px 0 -18px;

  /* 배경 및 블러 효과 */
  padding: 12px 10px calc(12px + env(safe-area-inset-bottom));
  background: linear-gradient(
      to top,
      rgba(7, 10, 20, 1) 0%,
      rgba(7, 10, 20, 0.98) 70%,
      rgba(7, 10, 20, 0) 100%
  );
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  z-index: 100;

  /* 1줄 배치를 위한 설정 */
  display: flex;
  gap: 6px; /* 버튼 사이 간격 축소 */
  flex-wrap: nowrap; /* 줄바꿈 방지 */
  overflow-x: auto; /* 혹시 화면이 아주 작을 경우 스크롤 허용 */
}

.btn {
  /* 4개가 균등하게 들어가도록 설정 */
  flex: 1 1 0;
  min-width: 0; /* flex 환경에서 텍스트에 의해 늘어나는 것 방지 */

  display: inline-flex;
  flex-direction: column; /* 아이콘(이모지)과 텍스트를 위아래로 배치하거나 */
  justify-content: center;
  align-items: center;
  gap: 4px;

  padding: 10px 4px; /* 여백 축소 */
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.92);
  cursor: pointer;

  /* 텍스트 크기 축소 및 줄바꿈 방지 */
  font-weight: 700;
  font-size: 11px;
  letter-spacing: -0.5px;
  white-space: nowrap;
  word-break: keep-all;

  transition: all 0.2s ease;
}

/* 버튼 내 이모지나 아이콘 크기 제어 (필요시) */
.btn span {
  font-size: 14px;
}

.btn:active {
  transform: scale(0.95);
  background: rgba(255, 255, 255, 0.15);
}

.btn.primary {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.25);
}

.btn.danger {
  border-color: rgba(255, 120, 120, 0.3);
  background: rgba(255, 80, 80, 0.12);
  color: #ff9494;
}
-
/* 스크롤바 숨기기 (깔끔한 UI용) */
.bottom::-webkit-scrollbar {
  display: none;
}
</style>
