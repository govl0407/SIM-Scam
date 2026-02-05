<template>
  <main class="about">
    <header class="hero">
      <div class="brand">
        <div class="kicker">ABOUT</div>
        <h1 class="title">SIM-SCAM : ESCAPE</h1>
        <p class="sub">
          로맨스 스캠·해외 취업 사기·투자 권유 등
          <b>‘의심하지 않는 순간’</b>을 체험하는 대화 시뮬레이션입니다.
        </p>
      </div>

    </header>

    <section class="grid">
      <article class="card">
        <h2>이 서비스는 무엇인가요?</h2>
        <p>
          실제 스캠 대화에서 자주 등장하는 흐름(감정 몰입 → 관계 압박 → 작은 부탁 → 금전/개인정보 요구)을
          기반으로, 사용자가 안전하게 ‘대응 연습’을 할 수 있도록 구성했습니다.
        </p>
      </article>

      <article class="card">
        <h2>어떻게 사용하나요?</h2>
        <ol class="steps">
          <li><b>트랙 선택</b> (연애 / 해외취업 / 투자)</li>
          <li><b>대화 진행</b> (상대의 요구 패턴 관찰)</li>
          <li><b>수상한 요청 거절</b> (금전·계정·개인정보 요구 등)</li>
        </ol>
      </article>

      <article class="card">
        <h2>무엇을 배우게 되나요?</h2>
        <ul class="bullets">
          <li>“급함/비밀”을 강요하는 말의 패턴</li>
          <li>신뢰를 만든 뒤 <b>작은 부탁</b>으로 시작하는 전개</li>
          <li>금전·개인정보·계정·송금 유도 문구의 특징</li>
        </ul>
      </article>

      <article class="card warn">
        <h2>안전 고지</h2>
        <p class="strong">
          본 서비스는 <b>교육·예방 목적의 시뮬레이션</b>입니다.
          실제 인물·계좌·송금·개인정보 거래는 발생하지 않습니다.
        </p>
        <p class="small">
          대화 내용은 사례를 참고해 각색될 수 있으며, 사용자의 실제 개인정보 입력을 요구하지 않습니다.
        </p>
      </article>
    </section>

    <footer class="foot">
      <div class="note">
        TIP: “지금 당장”, “비밀로 해줘”, “증빙 필요해” 같은 압박이 나오면 한 번 멈추고 확인하세요.
      </div>
      <div class="links">
        <RouterLink to="/" class="link">← 트랙 선택</RouterLink>
      </div>
    </footer>
  </main>
</template>

<script setup>
/* 필요 없으면 스크립트 비워도 됨 */
</script>

<style scoped>
.about {
  min-height: 100dvh;
  padding: 28px 20px;
  color: #f4f4f7;
  background:
      radial-gradient(circle at 15% 15%, rgba(122, 63, 255, 0.22), transparent 45%),
      radial-gradient(circle at 85% 30%, rgba(255, 92, 184, 0.18), transparent 45%),
      linear-gradient(180deg, #0b1020, #070a14);
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.hero {
  max-width: 1110px;
  margin: 0 auto;
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  padding: 18px;
  border-radius: 18px;
  border: 1px solid rgba(255,255,255,0.10);
  background: rgba(255,255,255,0.06);
  backdrop-filter: blur(10px);
}

.kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid rgba(255,255,255,0.14);
  font-weight: 800;
  letter-spacing: 0.14em;
  font-size: 12px;
  color: rgba(255,255,255,0.82);
}

.title {
  margin: 10px 0 6px;
  font-size: 34px;
  line-height: 1.05;
  letter-spacing: -0.02em;
}

.sub {
  white-space: nowrap;
  margin: 0;
  max-width: 560px;
  color: rgba(255,255,255,0.80);
  line-height: 1.55;
}



.cta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 42px;
  padding: 0 14px;
  border-radius: 14px;
  text-decoration: none;
  font-weight: 900;
  color: #0b1020;
  background: rgba(255,255,255,0.92);
  border: 1px solid rgba(255,255,255,0.18);
}
.btn:hover { filter: brightness(1.03); }
.btn.ghost {
  background: rgba(255,255,255,0.10);
  color: rgba(255,255,255,0.90);
  border: 1px solid rgba(255,255,255,0.16);
}

.grid {
  max-width: 1140px;
  margin: 0 auto;
  width: 100%;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.card {
  padding: 16px;
  border-radius: 18px;
  border: 1px solid rgba(255,255,255,0.10);
  background: rgba(255,255,255,0.06);
  backdrop-filter: blur(10px);
}

.card h2 {
  margin: 0 0 8px;
  font-size: 16px;
  letter-spacing: -0.01em;
}

.card p {
  margin: 0;
  color: rgba(255,255,255,0.78);
  line-height: 1.6;
}

.steps, .bullets {
  margin: 0;
  padding-left: 18px;
  color: rgba(255,255,255,0.78);
  line-height: 1.6;
}

.warn {
  border-color: rgba(255, 122, 170, 0.22);
  background: rgba(255, 92, 184, 0.08);
}

.strong {
  color: rgba(255,255,255,0.90);
  font-weight: 700;
}

.small {
  margin-top: 10px !important;
  font-size: 12px;
  color: rgba(255,255,255,0.70);
}

.foot {
  max-width: 1140px;
  margin: 0 auto;
  width: 100%;
  padding: 12px 18px 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.note {
  color: rgba(255,255,255,0.72);
  font-size: 12px;
}

.links {
  display: flex;
  gap: 12px;
}

.link {
  color: rgba(255,255,255,0.86);
  text-decoration: none;
  font-weight: 800;
  font-size: 12px;
  padding: 8px 10px;
  border-radius: 12px;
  border: 1px solid rgba(255,255,255,0.14);
  background: rgba(255,255,255,0.06);
}
.link:hover { background: rgba(255,255,255,0.10); }

@media (max-width: 900px) {
  .hero { flex-direction: column; align-items: flex-start; }
  .cta { justify-content: flex-start; }
  .grid { grid-template-columns: 1fr; }
  .title { font-size: 28px; }
}
</style>
