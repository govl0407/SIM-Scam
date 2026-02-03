<script setup>
import { ref, computed, nextTick, onMounted, watch } from 'vue'
import { sendChat } from '../api/chatApi'

const input = ref('')
const chats = ref([])

const boxRef = ref(null)
const inputRef = ref(null)

const showInfo = ref(false)


const atBottom = ref(true)
const showNewMsgBtn = ref(false)
const newMsgCount = ref(0)


const MAX_RENDER = 50
const LOAD_STEP = 30
const renderCount = ref(MAX_RENDER)

const canLoadMore = computed(() => chats.value.length > renderCount.value)

const startIndex = computed(() => {
  const len = chats.value.length
  return Math.max(0, len - renderCount.value)
})

const visibleChats = computed(() => {
  return chats.value.slice(startIndex.value)
})


const sessionId = ref('test-session')
const pendingEvent = ref(null)

function normalizeResponse(data) {
  if (typeof data === 'string') {
    try { return JSON.parse(data) } catch { return { text: data } }
  }
  return data
}

function pickInvestEvent(parsed) {
  if (Array.isArray(parsed?.events)) {
    return parsed.events.find(e => e?.type === 'INVEST_REQUEST') ?? null
  }
  if (typeof parsed?.events === 'string') {
    return { type: 'INVEST_REQUEST', value: { eventId: 'TEMP_EVENT', question: parsed.events } }
  }
  return null
}


const focusInput = async () => {
  await nextTick()
  inputRef.value?.focus?.({ preventScroll: true })
}


function isNearBottom(el, threshold = 160) {
  return el.scrollHeight - el.scrollTop - el.clientHeight < threshold
}

function resetNewMsg() {
  newMsgCount.value = 0
  showNewMsgBtn.value = false
}


function updateScrollState() {
  const el = boxRef.value
  if (!el) return

  atBottom.value = isNearBottom(el)


  if (atBottom.value) resetNewMsg()

  if (el.scrollTop < 30) {
    loadOlderAuto()
  }
}


let scrollRaf = 0

async function scrollToBottom(forceSmooth = false) {
  await nextTick()
  const el = boxRef.value
  if (!el) return

  if (scrollRaf) cancelAnimationFrame(scrollRaf)

  scrollRaf = requestAnimationFrame(() => {
    // 사용자가 위에서 읽는 중이면 자동 스크롤 금지
    if (!forceSmooth && !atBottom.value) {
      showNewMsgBtn.value = true
      return
    }

    const top = el.scrollHeight

    if (forceSmooth) el.scrollTo({ top, behavior: 'smooth' })
    else el.scrollTop = top

    atBottom.value = true
    resetNewMsg()
  })
}

function jumpToBottom() {
  scrollToBottom(true)
}


let loadingOlder = false

async function loadOlder() {
  const el = boxRef.value
  if (!el) return
  if (!canLoadMore.value) return
  if (loadingOlder) return

  loadingOlder = true

  const prevScrollHeight = el.scrollHeight
  const prevScrollTop = el.scrollTop

  renderCount.value = Math.min(chats.value.length, renderCount.value + LOAD_STEP)

  await nextTick()

  const newScrollHeight = el.scrollHeight
  const diff = newScrollHeight - prevScrollHeight

  el.scrollTop = prevScrollTop + diff

  loadingOlder = false
}

// 스크롤 맨 위 자동 로드가 너무 자주 호출되는 거 방지
let autoLoadLock = false
async function loadOlderAuto() {
  if (autoLoadLock) return
  autoLoadLock = true
  await loadOlder()
  setTimeout(() => { autoLoadLock = false }, 250)
}

watch(
    () => chats.value.length,
    async (newLen, oldLen) => {
      if (newLen <= (oldLen ?? 0)) return

      const added = Math.max(1, newLen - (oldLen ?? 0))

      if (atBottom.value) {
        await scrollToBottom(false)
        return
      }

      // 위에서 읽고 있으면 카운트 + 버튼만
      newMsgCount.value += added
      showNewMsgBtn.value = true
    }
)

onMounted(async () => {
  await focusInput()
  await scrollToBottom(false)
})

const send = async () => {
  const text = input.value.trim()
  if (!text) return

  chats.value.push({ role: 'user', text })
  input.value = ''
  focusInput()

  try {
    const data = await sendChat(text)
    const parsed = normalizeResponse(data)

    chats.value.push({
      role: 'bot',
      text: parsed?.text ?? parsed?.reply ?? '(응답 없음)'
    })

    pendingEvent.value = pickInvestEvent(parsed)
  } catch (e) {
    chats.value.push({ role: 'bot', text: '연결 실패' })
    console.error(e)
  } finally {
    focusInput()
  }
}

const decide = async (choice) => {
  const userText = choice === 'YES' ? '예' : '아니오'

  chats.value.push({ role: 'user', text: userText })
  pendingEvent.value = null
  focusInput()

  try {
    const data = await sendChat(userText)
    const parsed = normalizeResponse(data)

    chats.value.push({
      role: 'bot',
      text: parsed?.text ?? parsed?.reply ?? '(응답 없음)'
    })

    pendingEvent.value = pickInvestEvent(parsed)
  } catch (e) {
    chats.value.push({ role: 'bot', text: '선택 전송 실패' })
    console.error(e)
  } finally {
    focusInput()
  }
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
      <header class="topbar">
        <div class="profile">
          <img class="roomAvatar" src="/img/씹덕1.jpeg" alt="미아" />
          <div class="info">
            <div class="name">최정민</div>
            <div class="status">online 🟢</div>
          </div>
        </div>

        <div class="actions">
          <button class="ghost" type="button" title="통화">
            <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <path
                  d="M22 16.92v3a2 2 0 0 1-2.18 2
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
                 a2 2 0 0 1 1.72 2z"
              />
            </svg>
          </button>

          <button class="ghost" type="button" title="정보" @click="showInfo = true">
            <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="16" x2="12" y2="12" />
              <line x1="12" y1="8" x2="12.01" y2="8" />
            </svg>
          </button>
        </div>
      </header>

      <section class="chat" ref="boxRef" @scroll="updateScrollState">
        <!-- ✅ 위쪽 더보기 버튼 (원하면 자동 로드만 쓰고 이 버튼은 빼도 됨) -->
        <div class="loadMoreWrap" v-if="canLoadMore">
          <button class="loadMoreBtn" type="button" @click="loadOlder">
            이전 메시지 더보기
          </button>
        </div>

        <div class="dateLine">오늘</div>

        <div
            v-for="(c, i) in visibleChats"
            :key="startIndex + i"
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

        <!-- 새 메시지 버튼 -->
        <button
            v-if="showNewMsgBtn"
            class="newMsgBtn"
            type="button"
            @click="jumpToBottom"
        >
          새 메시지 {{ newMsgCount > 0 ? newMsgCount : '' }} ↓
        </button>
      </section>

      <footer class="composer">
        <input
            ref="inputRef"
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
  color: #111;
  height: 100dvh;
  min-height: 100svh;
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 14px;
  overflow: hidden;
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
.appTitle { font-size: 18px; font-weight: 800; }
.hint { margin-top: 4px; font-size: 12px; color: #666; }

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
.room:hover { background: #fafafa; }
.room.active { background: #f6f1ff; }
.roomAvatar { width: 42px; height: 42px; border-radius: 12px; }
.roomMeta { flex: 1; min-width: 0; }
.roomName { font-weight: 700; font-size: 14px; color: #000; }
.roomLast {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.sidebarBottom {
  margin-top: auto;
  padding: 12px 14px;
  border-top: 1px solid #f1f1f1;
  font-size: 12px;
  color: #666;
}

/* 우측 */
.panel {
  border: 1px solid #eee;
  border-radius: 16px;
  background: #fff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
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
.profile { display: flex; align-items: center; gap: 10px; }
.name { font-weight: 800; color: #000; }
.status { font-size: 12px; color: #666; margin-top: 2px; }
.actions { display: flex; gap: 8px; }
.icon { width: 22px; height: 22px; color: #000; display: block;}
.ghost {
  border: 1px solid #eee;
  background: #fff;
  border-radius: 12px;
  width: 40px;
  height: 40px;
  padding: 0;
  cursor: pointer;

  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 0;
}
.ghost:hover { background: #f6f6f6; }
.ghost:active { transform: scale(0.96); }

/* 채팅 */
.chat {
  position: relative;
  flex: 1;
  overflow-y: auto;
  padding: 14px;
  min-height: 0;
  background:
      radial-gradient(circle at 10% 10%, rgba(122, 63, 255, 0.08), transparent 40%),
      radial-gradient(circle at 90% 30%, rgba(255, 92, 184, 0.08), transparent 40%),
      #fafafa;
}

/* ✅ 위 더보기 */
.loadMoreWrap {
  position: sticky;
  top: 0;
  z-index: 6;
  display: flex;
  justify-content: center;
  padding: 8px 0;
  background: linear-gradient(#fafafa, rgba(250,250,250,0));
}
.loadMoreBtn {
  border: 1px solid #e7e7e7;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(6px);
  padding: 8px 12px;
  border-radius: 999px;
  cursor: pointer;
  font-weight: 800;
}
.loadMoreBtn:hover { background: #fff; }

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

.row { display: flex; margin: 8px 0; }
.row.them { justify-content: flex-start; }
.row.me { justify-content: flex-end; }
.bubble {
  max-width: 58%;
  padding: 10px 12px;
  border-radius: 16px;
  border: 1px solid #e9e9e9;
  background: #fff;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.35;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.04);
}
.row.me .bubble { background: #efe9ff; border-color: #e1d6ff; }
.text { font-size: 14px; }

/* 이벤트 카드 */
.eventCard {
  margin-top: 12px;
  border: 1px solid #eee;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  padding: 12px;
  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.06);
}
.eventTitle { font-weight: 800; font-size: 13px; margin-bottom: 8px; }
.eventQ { font-size: 13px; color: #333; margin-bottom: 10px; white-space: pre-wrap; }
.eventBtns { display: flex; gap: 10px; }
.eventBtns button {
  flex: 1;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid #e7e7e7;
  cursor: pointer;
  font-weight: 700;
}
.eventBtns .yes { background: #f3edff; border-color: #dfd2ff; }
.eventBtns .no { background: #fff0f6; border-color: #ffd0e6; }

/* 새 메시지 버튼 */
.newMsgBtn {
  position: sticky;
  bottom: 14px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 5;
  padding: 10px 14px;
  border-radius: 999px;
  border: 1px solid #e7e7e7;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(6px);
  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.10);
  cursor: pointer;
  font-weight: 800;
}
.newMsgBtn:hover { background: #fff; }

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
.composer input:focus { border-color: #d9c9ff; }
.sendBtn {
  padding: 12px 14px;
  border: 1px solid #e7e7e7;
  background: #fff;
  border-radius: 14px;
  cursor: pointer;
  font-weight: 800;
}
.sendBtn:hover { background: #fafafa; }

@media (max-width: 900px) {
  .dm { grid-template-columns: 1fr; height: 100vh; }
  .sidebar { display: none; }
}
</style>
