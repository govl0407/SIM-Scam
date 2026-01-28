<script setup>
import { ref, nextTick } from 'vue'
import { sendChat, sendDecision } from '../api/chatApi'

const input = ref('')
const chats = ref([])
const boxRef = ref(null)

// 이벤트 선택창용
const sessionId = ref('test-session')   // 백엔드랑 맞추면 됨
const pendingEvent = ref(null)          // { type, value: { eventId, question, ... } }

function normalizeResponse(data) {
  // 백엔드가 문자열(JSON 문자열) 또는 객체(Map)로 올 수 있어서 둘 다 처리
  if (typeof data === 'string') {
    try {
      return JSON.parse(data)
    } catch {
      return { text: data }
    }
  }
  return data
}

function pickInvestEvent(parsed) {
  // case 1: events가 배열인 경우 (정상 설계)
  if (Array.isArray(parsed?.events)) {
    return parsed.events.find(e => e?.type === 'INVEST_REQUEST') ?? null
  }

  // case 2: events가 문자열인 경우
  if (typeof parsed?.events === 'string') {
    return {
      type: 'INVEST_REQUEST',
      value: {
        eventId: 'TEMP_EVENT',              // 임시 ID
        question: parsed.events             // "개인정보 요구" 같은 문구
      }
    }
  }

  return null
}

async function scrollToBottom() {
  await nextTick()
  if (boxRef.value) boxRef.value.scrollTop = boxRef.value.scrollHeight
}

const send = async () => {
  const text = input.value.trim()
  if (!text) return

  chats.value.push({ role: 'user', text })
  input.value = ''

  await scrollToBottom()

  try {
    const data = await sendChat(text)
    const parsed = normalizeResponse(data)

    chats.value.push({
      role: 'bot',
      text: parsed?.text ?? parsed?.reply ?? '(응답 없음)'
    })

    // 이벤트가 오면 선택창 띄우기
    pendingEvent.value = pickInvestEvent(parsed)

  } catch (e) {
    chats.value.push({ role: 'bot', text: '연결 실패' })
    console.error(e)
  }

  await scrollToBottom()
}

const decide = async (choice) => {

  const userText = choice === 'YES' ? '예' : '아니오'
  chats.value.push({ role: 'user', text: userText })

  // 중복 클릭 방지로 일단 선택창 숨김
  pendingEvent.value = null

  await scrollToBottom()

  try {
    const data = await sendChat(userText)
    const parsed = normalizeResponse(data)

    chats.value.push({
      role: 'bot',
      text: parsed?.text ?? parsed?.reply ?? '(응답 없음)'
    })

    // 다음 이벤트가 또 오면 계속 띄우기
    pendingEvent.value = pickInvestEvent(parsed)

  } catch (e) {
    chats.value.push({ role: 'bot', text: '선택 전송 실패' })
    console.error(e)
  }

  await scrollToBottom()
}
</script>

<template>
  <main class="dm">
    <!-- 좌측: DM 리스트 -->
    <aside class="sidebar">
      <div class="sidebarTop">
        <div class="appTitle">DM</div>
        <div class="hint">로맨스스캠 체험</div>
      </div>

      <!-- 데모용: 대화방 1개만 -->
      <button class="room active" type="button">
        <img class="roomAvatar" src="/img/씹덕1.jpeg" alt="미아" />
        <div class="roomMeta">
          <div class="roomName">최정민(전과10범)</div>
          <div class="roomLast">
            {{ chats.length ? chats[chats.length - 1].text : '대화를 시작해보세요' }}
          </div>
        </div>
      </button>

      <div class="sidebarBottom">
        <div class="miniTip">TIP: 수상하면 “아니오” 선택</div>
      </div>
    </aside>

    <!-- 우측: 채팅 -->
    <section class="panel">
      <!-- 상단 헤더 -->
      <header class="topbar">
        <div class="profile">
          <img class="roomAvatar" src="/img/씹덕1.jpeg" alt="미아" />
          <div class="info">
            <div class="name">최정민</div>
            <div class="status">online 🟢</div>
          </div>
        </div>

        <div class="actions">
          <!-- 전화 -->
          <button class="ghost" type="button" title="통화">
            <svg
                class="icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
            >
              <path d="M22 16.92v3a2 2 0 0 1-2.18 2
               19.79 19.79 0 0 1-8.63-3.07
               19.5 19.5 0 0 1-6-6
               19.79 19.79 0 0 1-3.07-8.67
               A2 2 0 0 1 4.11 2h3
               a2 2 0 0 1 2 1.72
               c.12.81.3 1.6.54 2.36
               a2 2 0 0 1-.45 2.11L8.09 9.91
               a16 16 0 0 0 6 6l1.72-1.72
               a2 2 0 0 1 2.11-.45
               c.76.24 1.55.42 2.36.54
               a2 2 0 0 1 1.72 2z"/>
            </svg>
          </button>
          <button class="ghost" type="button" title="정보" @click="showInfo = true">
            <svg
                class="icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
            >
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="16" x2="12" y2="12" />
              <line x1="12" y1="8" x2="12.01" y2="8" />
            </svg>
          </button>
        </div>
      </header>

      <!-- 메시지 영역 -->
      <section class="chat" ref="boxRef">
        <div class="dateLine">오늘</div>

        <div
            v-for="(c, i) in chats"
            :key="i"
            :class="['row', c.role === 'user' ? 'me' : 'them']"
        >
          <div class="bubble">
            <div class="text">{{ c.text }}</div>
          </div>
        </div>

        <!-- 이벤트 선택 카드 -->
        <div v-if="pendingEvent" class="eventCard">
          <div class="eventTitle">선택 이벤트</div>
          <div class="eventQ">
            {{ pendingEvent?.value?.question ?? '투자 하실래요?' }}
          </div>
          <div class="eventBtns">
            <button class="yes" @click="decide('YES')">예</button>
            <button class="no" @click="decide('NO')">아니오</button>
          </div>
        </div>
      </section>

      <!-- 입력 영역 -->
      <footer class="composer">
        <input
            v-model="input"
            @keyup.enter="send"
            placeholder="메시지 입력…"
        />
        <button class="sendBtn" @click="send">전송</button>
      </footer>
    </section>
  </main>
</template>

<style scoped>
.dm {
  height: calc(100vh - 40px);
  max-width: 1100px;
  margin: 20px auto;
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 14px;
  font-family: system-ui, -apple-system, sans-serif;
}

/* 좌측 */
.sidebar {
  border: 1px solid #eee;
  border-radius: 16px;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebarTop {
  padding: 16px 14px;
  border-bottom: 1px solid #f1f1f1;
  background: linear-gradient(180deg, #faf7ff, #fff);
}

.appTitle {
  font-size: 18px;
  font-weight: 800;
}

.hint {
  margin-top: 4px;
  font-size: 12px;
  color: #666;
}

.room {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 12px;
  border: 0;
  background: #fff;
  cursor: pointer;
  text-align: left;
}

.room:hover {
  background: #fafafa;
}

.room.active {
  background: #f6f1ff;
}

.roomAvatar {
  width: 42px;
  height: 42px;
  border-radius: 12px;
}

.roomMeta {
  flex: 1;
  min-width: 0;
}

.roomName {
  font-weight: 700;
  font-size: 14px;
  color: #000;
}

.roomLast {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.roomBadge {
  font-size: 11px;
  padding: 4px 8px;
  border-radius: 999px;
  border: 1px solid #e2d8ff;
  color: #5a32c7;
  background: #fbf8ff;
}

.sidebarBottom {
  margin-top: auto;
  padding: 12px 14px;
  border-top: 1px solid #f1f1f1;
  font-size: 12px;
  color: #666;
}

/* 우측 패널 */
.panel {
  border: 1px solid #eee;
  border-radius: 16px;
  background: #fff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-bottom: 1px solid #f1f1f1;
  background: #fff;
}

.profile {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 14px;
}

.name {
  font-weight: 800;
  color: #000;
}

.status {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

.actions {
  display: flex;
  gap: 8px;
}

.icon {
  width: 22px;
  height: 22px;
  color: #000;   /* 전화/정보 동일한 색 */
}

.ghost {
  border: 1px solid #eee;
  background: #fff;
  border-radius: 12px;
  padding: 8px;
  cursor: pointer;
}

.ghost:hover {
  background: #f6f6f6;
}

.ghost:active {
  transform: scale(0.96);
}


/* 채팅 */
.chat {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
  background:
      radial-gradient(circle at 10% 10%, rgba(122, 63, 255, 0.08), transparent 40%),
      radial-gradient(circle at 90% 30%, rgba(255, 92, 184, 0.08), transparent 40%),
      #fafafa;
}

.dateLine {
  width: fit-content;
  margin: 0 auto 12px;
  font-size: 12px;
  color: #777;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid #eee;
  padding: 6px 10px;
  border-radius: 999px;
}

.row {
  display: flex;
  margin: 8px 0;
}

.row.them {
  justify-content: flex-start;
}

.row.me {
  justify-content: flex-end;
}

.bubble {
  max-width: 58%;
  padding: 10px 12px;
  border-radius: 16px;
  border: 1px solid #e9e9e9;
  background: #fff;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.35;
  box-shadow: 0 6px 18px rgba(0,0,0,0.04);
}

.row.me .bubble {
  background: #efe9ff;
  border-color: #e1d6ff;
}

.text {
  font-size: 14px;
}

/* 이벤트 카드 */
.eventCard {
  margin-top: 12px;
  border: 1px solid #eee;
  background: rgba(255,255,255,0.9);
  border-radius: 16px;
  padding: 12px;
  box-shadow: 0 10px 22px rgba(0,0,0,0.06);
}

.eventTitle {
  font-weight: 800;
  font-size: 13px;
  margin-bottom: 8px;
}

.eventQ {
  font-size: 13px;
  color: #333;
  margin-bottom: 10px;
  white-space: pre-wrap;
}

.eventBtns {
  display: flex;
  gap: 10px;
}

.eventBtns button {
  flex: 1;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid #e7e7e7;
  cursor: pointer;
  font-weight: 700;
}

.eventBtns .yes {
  background: #f3edff;
  border-color: #dfd2ff;
}

.eventBtns .no {
  background: #fff0f6;
  border-color: #ffd0e6;
}

/* 입력 */
.composer {
  display: flex;
  gap: 10px;
  padding: 12px;
  border-top: 1px solid #f1f1f1;
  background: #fff;
}

.composer input {
  flex: 1;
  padding: 12px 12px;
  border: 1px solid #eee;
  border-radius: 14px;
  outline: none;
}

.composer input:focus {
  border-color: #d9c9ff;
}

.sendBtn {
  padding: 12px 14px;
  border: 1px solid #e7e7e7;
  background: #fff;
  border-radius: 14px;
  cursor: pointer;
  font-weight: 800;
}

.sendBtn:hover {
  background: #fafafa;
}


/* 작은 화면은 1컬럼으로 */
@media (max-width: 900px) {
  .dm {
    grid-template-columns: 1fr;
    height: auto;
  }
  .sidebar {
    display: none;
  }
}

</style>
