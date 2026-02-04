<script setup>
import { computed, ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const tracks = [
  {
    id: "romance",
    label: "Romance",
    title: "연애 시뮬레이션",
    one: "호감은 빠르게, 부탁은 더 빠르게.",
    desc: "감정 몰입 → 관계 유지 압박 → 작은 부탁 → 금전/개인정보 요구",
    tone: "pink",
  },
  {
    id: "job",
    label: "Job",
    title: "해외 취업 제안 시뮬레이션",
    one: "합격은 쉬워 보여도, 요구는 즉시 시작됩니다.",
    desc: "고연봉/해외 근무 제안 → 빠른 합격 → 항공권/숙소 제공 → 일정/이동 통제 시도 ",
    tone: "violet",
  },
  {
    id: "invest",
    label: "Invest",
    title: "투자 권유 시뮬레이션",
    one: "수익 인증 뒤엔, 더 큰 입금이 따라옵니다.",
    desc: "소액 수익 → 신뢰 형성 → 추가 입금 압박 → 회수 불가",
    tone: "blue",
  },
];

const selectedId = ref(null);
const selected = computed(() => tracks.find((t) => t.id === selectedId.value));

const start = () => {
  if (!selectedId.value) return;
  router.push(`/chat?track=${selectedId.value}`);
};
</script>

<template>
  <main class="screen">
    <!-- 배경 레이어(전체 화면) -->
    <div class="bg" aria-hidden="true" />

    <!-- 상단 바 -->
    <header class="top">
      <div class="brand">
        <div class="logo">SIM-SCAM : ESCAPE</div>
        <div class="sub">Scam Conversation Escape Simulation</div>
      </div>
      <RouterLink to="/about" class="chip link">ABOUT</RouterLink>
    </header>

    <!-- 메인 -->
    <section class="hero">
      <div class="copy">
        <div class="kicker">PROLOGUE</div>

        <h1 class="h1">
          지금 이 대화,<br />
          끝까지 가도 괜찮을까요?
        </h1>

        <p class="p">
          아직은 괜찮아 보이죠.<br />
          <strong class="strong">대부분의 피해는 “의심하지 않는 순간”에 시작됩니다.</strong>
        </p>

        <div class="hint">
          <span class="dot" />
          트랙을 선택하면 “대화”가 바로 시작됩니다.
        </div>
      </div>

      <!-- 선택 패널 -->
      <aside class="panel">
        <div class="panel-head">
          <div class="panel-title">TRACK SELECT</div>
          <div class="panel-sub">하나를 고르세요. 되돌릴 수 없습니다.</div>
        </div>

        <div class="cards">
          <button
              v-for="t in tracks"
              :key="t.id"
              class="card"
              :class="[{ on: t.id === selectedId }, t.tone]"
              @click="selectedId = t.id"
          >
            <div class="label">{{ t.label }}</div>
            <div class="title">{{ t.title }}</div>
            <div class="one">{{ t.one }}</div>
            <div class="desc">{{ t.desc }}</div>

            <div class="selected" v-if="t.id === selectedId">
              선택됨
              <span class="check">✓</span>
            </div>
          </button>
        </div>

        <button class="cta" :disabled="!selectedId" @click="start">
          대화에 들어가기
          <span class="arrow">→</span>
        </button>

        <div class="notice">
          ※ 교육·예방 목적의 시뮬레이션입니다. 실제 금전 송금/개인정보 거래는 발생하지 않습니다.
        </div>
      </aside>
    </section>
  </main>
</template>

<style scoped>
/* ===== 화면 전체 ===== */
.screen {
  min-height: 100vh;
  position: relative;
  color: #eef2ff;
  padding: 28px 18px 40px;
  overflow: hidden;
}

/* 배경을 “전체 화면”으로 깔기 (가운데만 안 보이게) */
.bg {
  position: fixed;
  inset: 0;
  z-index: -1;
  background:
      radial-gradient(900px 520px at 18% 12%, rgba(255, 79, 180, 0.18), transparent 60%),
      radial-gradient(860px 520px at 82% 18%, rgba(122, 149, 255, 0.16), transparent 60%),
      radial-gradient(900px 620px at 50% 120%, rgba(255, 220, 140, 0.10), transparent 55%),
      linear-gradient(180deg, #0b0f1f, #131a37);
}

/* ===== 상단 ===== */
.top {
  max-width: 1120px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.logo {
  font-weight: 900;
  letter-spacing: 0.14em;
  font-size: 14px;
}

.sub {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(238, 242, 255, 0.62);
}

.link{
  text-decoration: none;
  color: inherit;
}

.chip {
  font-size: 12px;
  font-weight: 800;
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.06);
}

/* ===== 메인 레이아웃 ===== */
.hero {
  max-width: 1120px;
  margin: 26px auto 0;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 22px;
  align-items: start;
}

/* ===== 좌측 카피 ===== */
.copy {
  padding: 10px 4px;
}

.kicker {
  display: inline-block;
  font-size: 12px;
  letter-spacing: 0.18em;
  font-weight: 800;
  color: rgba(238, 242, 255, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(255, 255, 255, 0.06);
  padding: 8px 10px;
  border-radius: 999px;
}

.h1 {
  margin: 14px 0 12px;
  font-size: 56px;
  line-height: 1.04;
  letter-spacing: -0.04em;
  font-weight: 900;
}

.p {
  margin: 0;
  font-size: 16px;
  line-height: 1.75;
  color: rgba(238, 242, 255, 0.74);
}

.strong {
  color: rgba(255, 255, 255, 0.92);
  font-weight: 800;
}

.hint {
  margin-top: 16px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: rgba(238, 242, 255, 0.62);
  font-size: 13px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.65);
  box-shadow: 0 0 0 6px rgba(255, 255, 255, 0.06);
}

/* ===== 우측 패널 ===== */
.panel {
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.06);
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.35);
  padding: 16px;
}

.panel-head {
  padding: 4px 4px 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.10);
  margin-bottom: 12px;
}

.panel-title {
  font-weight: 900;
  letter-spacing: 0.14em;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.86);
}

.panel-sub {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(238, 242, 255, 0.62);
}

/* ===== 트랙 카드 ===== */
.cards {
  display: grid;
  gap: 10px;
  margin-top: 12px;
}

.card {
  text-align: left;
  border-radius: 16px;
  padding: 14px;
  cursor: pointer;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(0, 0, 0, 0.16);
  transition: transform 0.12s ease, border-color 0.12s ease, background 0.12s ease;
  position: relative;
}

.card:hover {
  transform: translateY(-1px);
  background: rgba(0, 0, 0, 0.22);
}

.card.on {
  border-color: rgba(255, 255, 255, 0.28);
  background: rgba(0, 0, 0, 0.28);
}

.label {
  font-size: 12px;
  letter-spacing: 0.12em;
  font-weight: 800;
  color: rgba(238, 242, 255, 0.62);
}

.title {
  margin-top: 8px;
  font-size: 16px;
  font-weight: 900;
  color: rgba(255, 255, 255, 0.92);
}

.one {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(238, 242, 255, 0.72);
  font-weight: 700;
}

.desc {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.6;
  color: rgba(238, 242, 255, 0.56);
}

.selected {
  position: absolute;
  right: 12px;
  top: 12px;
  font-size: 12px;
  font-weight: 800;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.9);
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.check {
  font-weight: 900;
}

/* 트랙별 은은한 포인트 */
.card.pink.on { box-shadow: 0 22px 70px rgba(255, 79, 180, 0.10); }
.card.violet.on { box-shadow: 0 22px 70px rgba(168, 107, 255, 0.12); }
.card.blue.on { box-shadow: 0 22px 70px rgba(122, 149, 255, 0.12); }

/* ===== CTA ===== */
.cta {
  width: 100%;
  margin-top: 14px;
  height: 48px;
  border-radius: 999px;
  border: none;
  cursor: pointer;
  font-weight: 900;
  background: rgba(255, 255, 255, 0.92);
  color: #0b0f1f;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.cta:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.arrow {
  font-weight: 900;
}

/* ===== 하단 안내 ===== */
.notice {
  margin-top: 12px;
  font-size: 12px;
  color: rgba(238, 242, 255, 0.52);
  border-top: 1px solid rgba(255, 255, 255, 0.10);
  padding-top: 12px;
}

/* ===== 반응형 ===== */
@media (max-width: 980px) {
  .hero {
    grid-template-columns: 1fr;
  }
  .h1 {
    font-size: 46px;
  }
}
@media (max-width: 520px) {
  .h1 {
    font-size: 36px;
  }
}
</style>
