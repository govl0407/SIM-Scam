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
    disabled: false, // 활성화
  },
  {
    id: "invest",
    label: "Invest",
    title: "투자 권유 시뮬레이션",
    one: "수익 인증 뒤엔, 더 큰 입금이 따라옵니다.",
    desc: "소액 수익 → 신뢰 형성 → 추가 입금 압박 → 회수 불가",
    tone: "blue",
    disabled: false,
  },
  {
    id: "job",
    label: "Job",
    title: "해외 취업 제안 시뮬레이션",
    one: "합격은 쉬워 보여도, 요구는 즉시 시작됩니다.",
    desc: "고연봉/해외 근무 제안 → 빠른 합격 → 항공권/숙소 제공 → 일정/이동 통제 시도",
    tone: "violet",
    disabled: true, //  비활성화 (개발 중)
  },
];
const selectScenario = (t) => {
  if (t.disabled) return; //  비활성 상태면 선택 방지
  selectedId.value = t.id;
};

const selectedId = ref(null);
const selected = computed(() => tracks.find((t) => t.id === selectedId.value));

const start = () => {
  if (!selectedId.value) return;
  router.push(`/chat?track=${selectedId.value}`);
};
</script>

<template>
  <main class="screen">
    <div class="bg" aria-hidden="true" />

    <header class="top">
      <div class="brand">
        <div class="logo">SIM-SCAM : ESCAPE</div>
        <div class="sub">Scam Conversation Escape Simulation</div>
      </div>
      <RouterLink to="/about" class="chip link">ABOUT</RouterLink>
    </header>

    <section class="hero">
      <div class="copy">
        <div class="kicker">PROLOGUE</div>

        <h1 class="h1">
          지금 이 대화,<br />
          어디까지 믿어도 될까요?
        </h1>

        <p class="p">
          시뮬레이션 속에서,
          스캠을 구별하는 감각을 키워보세요.
          <br />
          <strong class="strong">대화의 흐름 속에서 판단이 어떻게 흔들리는지 직접 경험할 수 있습니다.</strong>
        </p>



        <div class="hint">
          <span class="dot" />
          대화를 끝내는 순간, 당신은 스캠을 ‘대응할 수 있는 사람’이 됩니다.
        </div>
      </div>

      <aside class="panel">
        <div class="panel-head">
          <div class="panel-title">TRACK SELECT</div>
          <div class="panel-sub">하나를 선택하세요. 선택한 트랙으로 진행됩니다.</div>
        </div>

        <div class="cards">
          <button
              v-for="t in tracks"
              :key="t.id"
              class="card"
              :class="[{ on: t.id === selectedId, is_disabled: t.disabled }, t.tone]"
              :disabled="t.disabled"
              @click="selectScenario(t)"
          >
            <div v-if="t.disabled" class="dev-badge">준비 중</div>

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
          ※ 교육·예방 목적의 시뮬레이션입니다. 실제 개인정보를 전송하지 않도록 주의하세요.
        </div>
      </aside>
    </section>
  </main>
</template>

<style scoped>
.screen {
  min-height: 100vh;
  position: relative;
  color: #eef2ff;
  padding: 28px 18px 40px;
  overflow: hidden;
}

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

.link {
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

.hero {
  max-width: 1120px;
  margin: 26px auto 0;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 22px;
  align-items: start;
}

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

.cards {
  display: grid;
  gap: 12px;
  margin-top: 12px;
}

.card {
  text-align: left;
  border-radius: 16px;
  padding: 16px;
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

.card.pink.on { box-shadow: 0 22px 70px rgba(255, 79, 180, 0.10); }
.card.violet.on { box-shadow: 0 22px 70px rgba(168, 107, 255, 0.12); }

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

.notice {
  margin-top: 12px;
  font-size: 12px;
  color: rgba(238, 242, 255, 0.52);
  border-top: 1px solid rgba(255, 255, 255, 0.10);
  padding-top: 12px;
}

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

/* 비활성화 카드 스타일 */
.card.is_disabled {
  cursor: not-allowed;
  opacity: 0.6;
  filter: grayscale(0.8);
  border: 1px dashed rgba(255, 255, 255, 0.2); /* 점선 테두리로 미완성 느낌 부여 */
}

.card.is_disabled:hover {
  transform: none; /* 호버 애니메이션 제거 */
  background: rgba(0, 0, 0, 0.16);
}

/* 준비 중 배지 디자인 */
.dev-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: rgba(255, 255, 255, 0.8);
  font-size: 10px;
  font-weight: 800;
  padding: 4px 8px;
  border-radius: 4px;
  letter-spacing: 0.05em;
  backdrop-filter: blur(4px);
}

/* 기존 selected 배지와 겹치지 않도록 위치 조정 필요 시 수정 */
</style>
