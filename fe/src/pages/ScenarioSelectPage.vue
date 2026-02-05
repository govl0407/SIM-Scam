<template>
  <div class="wrap">
    <header class="top">
      <h1>시나리오 선택</h1>
      <p class="sub">원하는 유형을 골라 시뮬레이션을 시작해요.</p>
    </header>

    <section class="grid">
      <button
          v-for="s in scenarios"
          :key="s.id"
          class="card"
          :class="s.tone"
          @click="pick(s)"
      >
        <div class="label">{{ s.label }}</div>
        <div class="title">{{ s.title }}</div>
        <div class="one">{{ s.one }}</div>
        <div class="desc">{{ s.desc }}</div>

        <div class="cta">
          <span>선택</span>
          <span aria-hidden="true">→</span>
        </div>
      </button>
    </section>

    <footer class="bottom">
      <button class="ghost" @click="goBack">뒤로</button>
      <button class="primary" :disabled="!selected" @click="start">
        {{ selected ? `"${selected.title}" 시작하기` : "시나리오를 선택해 주세요" }}
      </button>
    </footer>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const selected = ref(null);

const scenarios = [
  {
    id: "romance",
    label: "Romance",
    title: "로맨스 스캠",
    one: "호감은 빠르게, 요구는 더 빠르게.",
    desc: "감정 몰입 → 관계/미래 약속 → 외부 메신저 유도 → 개인정보/금전 요구",
    tone: "pink",
  },
  {
    id: "job",
    label: "Job",
    title: "해외취업 제안",
    one: "합격이 쉬울수록 더 의심해야 해요.",
    desc: "고연봉 제안 → 빠른 합격 → 항공권/숙소 미끼 → 이동/일정 통제",
    tone: "violet",
  },
  {
    id: "invest",
    label: "Invest",
    title: "투자/리딩방",
    one: "수익 인증 뒤엔 ‘더 큰 입금’이 와요.",
    desc: "소액 수익 인증 → 신뢰 쌓기 → 추가입금 유도 → 출금 지연/수수료 요구",
    tone: "mint",
  },
];

function pick(s) {
  selected.value = s;
  // 1) 가장 쉬운 방식: sessionStorage에 저장 (새로고침에도 유지됨)
  sessionStorage.setItem("scenarioId", s.id);
}

function start() {
  if (!selected.value) return;

  // 2) 라우트로도 넘겨두면 디버깅 편함
  router.push({
    path: "/chat",
    query: { scenario: selected.value.id },
  });
}

function goBack() {
  router.back();
}
</script>

<style scoped>
.wrap {
  max-width: 980px;
  margin: 0 auto;
  padding: 28px 18px 24px;
  color: #111;
}
.top h1 {
  font-size: 28px;
  margin: 0;
  letter-spacing: -0.4px;
}
.sub {
  margin: 8px 0 18px;
  opacity: 0.75;
}
.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.card {
  text-align: left;
  border: 1px solid rgba(0,0,0,0.08);
  border-radius: 18px;
  padding: 16px;
  background: rgba(255,255,255,0.75);
  box-shadow: 0 6px 24px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: transform .12s ease, box-shadow .12s ease;
}
.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(0,0,0,0.10);
}
.label {
  font-size: 12px;
  opacity: 0.7;
  margin-bottom: 8px;
}
.title {
  font-size: 18px;
  font-weight: 800;
  margin-bottom: 8px;
}
.one {
  font-weight: 700;
  margin-bottom: 10px;
}
.desc {
  font-size: 13px;
  line-height: 1.5;
  opacity: 0.85;
  min-height: 54px;
}
.cta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 14px;
  font-weight: 800;
}

/* 톤 */
.pink { outline: 3px solid rgba(255, 120, 170, 0.18); }
.violet { outline: 3px solid rgba(160, 120, 255, 0.18); }
.mint { outline: 3px solid rgba(80, 200, 170, 0.18); }

.bottom {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 18px;
}
.ghost, .primary {
  border-radius: 14px;
  padding: 10px 14px;
  font-weight: 800;
  border: 1px solid rgba(0,0,0,0.12);
  background: white;
  cursor: pointer;
}
.primary {
  background: #111;
  color: #fff;
  border-color: #111;
}
.primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 900px) {
  .grid { grid-template-columns: 1fr; }
}
</style>
