<template>
  <main class="dm">
    <!-- 좌측: DM 리스트 -->
    <aside class="sidebar">
      <div class="sidebarTop">
        <div class="appTitle">DM</div>
        <div class="hint">로맨스스캠 체험</div>
      </div>

      <button class="room active" type="button" @click="openProfile">
        <img class="roomAvatar" :src="persona.avatarUrl" :alt="persona.name" />
        <div class="roomMeta">
          <div class="roomName">
            {{ persona.name }}
            <span v-if="persona.age">({{ persona.age }})</span>
          </div>
          <div class="roomLast">
            {{ chats.length
              ? chats[chats.length - 1].text
              : (persona.subtitle || '대화를 시작해보세요') }}
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
        <div class="profile clickable" @click="openProfile">
          <img class="roomAvatar" :src="persona.avatarUrl" :alt="persona.name" />
          <div class="info">
            <div class="name">{{ persona.name }}</div>
            <div class="status">
              <span v-if="persona.job">{{ persona.job }}</span>
              <span v-if="persona.location"> · {{ persona.location }}</span>
              <span v-if="!persona.job && !persona.location">online</span>
            </div>
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
        <div class="loadMoreWrap" v-if="canLoadMore">
          <button class="loadMoreBtn" type="button" @click="loadOlder">
            이전 메시지 더보기
          </button>
        </div>

        <div class="dateLine">오늘</div>

        <div v-for="(c, i) in visibleChats" :key="startIndex + i">
          <div v-if="c.role === 'system'" class="systemRow">
            <div class="systemPill">{{ c.text }}</div>
          </div>

          <div v-else :class="['row', c.role === 'user' ? 'me' : 'them']">
            <div class="bubble">
              <div class="text">{{ c.text }}</div>
            </div>
          </div>
        </div>

        <!-- 이벤트 선택 카드 -->
        <div v-if="pendingEvent" class="eventCard">
          <div class="eventTitle">선택 이벤트</div>
          <div class="eventQ">
            {{ eventToQuestion(pendingEvent.event) }}
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

    <div v-if="showProfile" class="profileModal" @click.self="closeProfile">
      <div class="profileCard">
        <div class="pcTop">
          <img class="pcAvatar" :src="persona.avatarUrl" :alt="persona.name" />
          <div class="pcMeta">
            <div class="pcNameRow">
              <div class="pcName">{{ persona.name }}</div>
              <span class="pcBadge">verified</span>
            </div>
            <div class="pcSub">
              <span v-if="persona.age">{{ persona.age }}</span>
              <span v-if="persona.age && persona.location"> · </span>
              <span v-if="persona.location">{{ persona.location }}</span>
            </div>
            <div class="pcJob" v-if="persona.job">{{ persona.job }}</div>
          </div>

          <button class="pcClose" type="button" @click="closeProfile">✕</button>
        </div>

        <div class="pcStats">
          <div class="pcStat">
            <div class="num">{{ profileStats.posts }}</div>
            <div class="label">게시물</div>
          </div>
          <div class="pcStat">
            <div class="num">{{ profileStats.followers }}</div>
            <div class="label">팔로워</div>
          </div>
          <div class="pcStat">
            <div class="num">{{ profileStats.following }}</div>
            <div class="label">팔로잉</div>
          </div>
        </div>

        <div class="pcBio" v-if="persona.personality || persona.traits">
          <div class="pcBioLine" v-if="persona.personality"><b>성격</b> {{ persona.personality }}</div>
          <div class="pcBioLine" v-if="persona.traits"><b>특징</b> {{ persona.traits }}</div>
        </div>

        <div class="pcActions">
          <button class="pcBtn primary" type="button" @click="closeProfile">메시지 보내기</button>
          <button class="pcBtn" type="button">팔로잉</button>
        </div>
      </div>
    </div>

  </main>

</template>

<script setup>
import { ref, computed, nextTick, onMounted, watch } from 'vue'
import { sendChat, sendDecision, getPersona } from '../api/chatApi'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const localEventLogs = ref({})

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

const pendingEvent = ref(null)

const SCENARIOS_BY_TRACK = {
  romance: ['romance', 'invest'], // romance(=money), invest
  job: [],
  invest: [],
}

const selectedTrack = ref(null)
const selectedScenario = ref(null)

// 서버가 내려주는 세션ID(프론트 저장/복원 기준키)
const serverSessionId = ref(localStorage.getItem('simscam_server_session_id') || null)

/* =========================
 *  Persona
 * ========================= */

const persona = ref({
  name: '상대',
  age: '',
  job: '',
  location: '',
  personality: '',
  traits: '',
  subtitle: '',
  avatarUrl: '/img/씹덕1.jpeg',
})

function defaultAvatarByScenario(s) {
  // 파일 없으면 기존 이미지로만 써도 됨
  if (s === 'invest') return '/img/김부자.png'
  if (s === 'romance') return '/img/일본인 여자.png'
  return '/img/씹덕1.jpeg'
}

function mapPersona(raw = {}, scenarioId = 'romance') {
  const name = raw['이름'] ?? raw['닉네임'] ?? raw['Name'] ?? '상대'
  const age = raw['나이'] ?? raw['age'] ?? ''
  const job = raw['직업'] ?? raw['job'] ?? ''
  const location = raw['주소지'] ?? raw['거주지'] ?? raw['location'] ?? ''
  const personality = raw['성격'] ?? raw['personality'] ?? ''
  const traits = raw['특징'] ?? raw['traits'] ?? ''

  const subtitle = [job, location].filter(Boolean).join(' · ')
  const avatarUrl = defaultAvatarByScenario(scenarioId)

  return { name, age, job, location, personality, traits, subtitle, avatarUrl }
}

async function loadPersona() {
  const scenarioId = selectedScenario.value ?? 'romance'
  try {
    const raw = await getPersona(scenarioId)
    persona.value = mapPersona(raw, scenarioId)
  } catch {
    persona.value = {
      name: '상대',
      age: '',
      job: '',
      location: '',
      personality: '',
      traits: '',
      subtitle: '',
      avatarUrl: defaultAvatarByScenario(scenarioId),
    }
  }
}

const showProfile = ref(false)

// 인스타 연출용: persona 기반으로 stats를 그럴듯하게
const profileStats = ref({ posts: 0, followers: '0', following: 0 })

function buildProfileStats(p) {
  const job = (p.job ?? '').toString()
  const traits = (p.traits ?? '').toString()
  const location = (p.location ?? '').toString()
  const personality = (p.personality ?? '').toString()

  // 💰 부자 / 투자 / 자산 애널리스트
  const rich =
      job.includes('자산') ||
      job.includes('애널') ||
      job.includes('투자') ||
      traits.includes('성공') ||
      traits.includes('외제차')

  // 🇯🇵 일본인 유학생
  const japaneseStudent =
      job.includes('유학생') ||
      job.includes('학생') ||
      location.includes('일본') ||
      traits.includes('일본') ||
      personality.includes('일본')

  // 우선순위: 부자 > 일본인 유학생 > 일반
  if (rich) {
    return {
      posts: 73,
      followers: '3.2만',
      following: 123,
    }
  }

  if (japaneseStudent) {
    return {
      posts: 24,        // 일상 사진 위주
      followers: '1.2천',
      following: 612,   // 많이 팔로우하는 타입
    }
  }

  // 기본값
  return {
    posts: 18,
    followers: '1.2만',
    following: 321,
  }
}

function openProfile() {
  profileStats.value = buildProfileStats(persona.value)
  showProfile.value = true
}

function closeProfile() {
  showProfile.value = false
}


/* =========================
 *  유저/트랙/시나리오
 * ========================= */

function getUserId() {
  return localStorage.getItem('simscam_user_id') || 'guest'
}

function getTrackId() {
  const st = history.state?.track ?? route.state?.track
  if (typeof st === 'string' && st.trim()) return st.trim()

  const qp = route.query?.track
  if (typeof qp === 'string' && qp.trim()) return qp.trim()

  const pid = route.params?.track
  if (typeof pid === 'string' && pid.trim()) return pid.trim()

  return 'romance'
}

function ensureScenarioRandomEveryTime() {
  const trackId = getTrackId()
  selectedTrack.value = trackId

  const pool = SCENARIOS_BY_TRACK[trackId] || []
  const fallbackScenario = 'romance'

  const picked = pool.length
      ? pool[Math.floor(Math.random() * pool.length)]
      : fallbackScenario

  selectedScenario.value = picked
}

/* =========================
 *  sessionId 기반 localStorage 저장/복원
 * ========================= */

function chatStorageKey(sessionId, scenario) {
  const sid = sessionId || 'no-session'
  const sc = scenario || 'romance'
  return `scam_chat:${sid}:${sc}`
}

function saveChatToStorage() {
  const sid = serverSessionId.value
  const sc = selectedScenario.value || 'romance'
  if (!sid) return

  const payload = {
    sessionId: sid,
    scenario: sc,
    updatedAt: Date.now(),
    chats: chats.value,
    localEventLogs: localEventLogs.value,
  }

  try {
    localStorage.setItem(chatStorageKey(sid, sc), JSON.stringify(payload))
  } catch {
    // storage full 등은 조용히 무시
  }
}

function loadChatFromStorage() {
  const sid = serverSessionId.value
  const sc = selectedScenario.value || 'romance'
  if (!sid) return false

  const raw = localStorage.getItem(chatStorageKey(sid, sc))
  if (!raw) return false

  try {
    const saved = JSON.parse(raw)
    if (Array.isArray(saved?.chats)) chats.value = saved.chats
    if (saved?.localEventLogs && typeof saved.localEventLogs === 'object') localEventLogs.value = saved.localEventLogs
    return true
  } catch {
    return false
  }
}

/* =========================
 *  chat push helpers
 * ========================= */

function pushBot(text, extra = {}) {
  const norm = (text ?? '').toString().trim()
  if (!norm) {
    chats.value.push({ role: 'bot', text: '(응답 없음)', ...extra })
    return
  }

  // 직전 bot 메시지 찾기
  const lastBot = [...chats.value].reverse().find(m => m.role === 'bot')
  const lastNorm = (lastBot?.text ?? '').toString().trim()

  // 완전 동일하면 스킵
  if (lastNorm && lastNorm === norm) return

  chats.value.push({ role: 'bot', text: norm, ...extra })
}

/* =========================
 *  응답 normalize
 * ========================= */

function normalizeResponse(data) {
  if (typeof data === 'string') {
    try { return JSON.parse(data) } catch { return { text: data } }
  }
  return data ?? {}
}

function normalizeServerPayload(parsed) {
  const text =
      parsed?.text ??
      parsed?.reply ??
      parsed?.message ??
      '(응답 없음)'

  const currentEvent =
      parsed?.currentEvent ??
      parsed?.CurrentEvent ?? // 혹시 아직 백엔드 통일 전이면 방어
      null

  const event =
      parsed?.event ??
      currentEvent ??
      null

  const end = !!(parsed?.end)

  const stage =
      parsed?.['단계'] ??
      parsed?.stage ??
      null

  const eventLogs =
      parsed?.eventLogs ??
      {}

  return { text, currentEvent, event, end, stage, eventLogs }
}

function pickEventName(parsed) {
  const ev =
      parsed?.event ??
      parsed?.currentEvent ??
      parsed?.CurrentEvent ??
      null

  return (typeof ev === 'string' && ev.trim()) ? ev.trim() : null
}

function nextStepFromLogs(logs) {
  const steps = Object.keys(logs || {})
      .map(k => Number(String(k).split('_')[0]) || 0)
  return (steps.length ? Math.max(...steps) : 0) + 1
}

function mergeLogs(serverLogs = {}, localLogs = {}) {
  return { ...(serverLogs || {}), ...(localLogs || {}) }
}

function hasAnyEventLogs(logs) {
  return !!logs && Object.keys(logs).length > 0
}

/* =========================
 *  UI Focus / Scroll
 * ========================= */

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

let autoLoadLock = false
async function loadOlderAuto() {
  if (autoLoadLock) return
  autoLoadLock = true
  await loadOlder()
  setTimeout(() => { autoLoadLock = false }, 250)
}

/* 새메시지 watch */
watch(
    () => chats.value.length,
    async (newLen, oldLen) => {
      if (newLen <= (oldLen ?? 0)) return

      const added = Math.max(1, newLen - (oldLen ?? 0))

      if (atBottom.value) {
        await scrollToBottom(false)
        return
      }

      newMsgCount.value += added
      showNewMsgBtn.value = true
    }
)

/* 저장용 watch */
watch(
    [chats, localEventLogs, selectedScenario, serverSessionId],
    () => saveChatToStorage(),
    { deep: true }
)

/* 시나리오 바뀌면 persona도 새로 */
watch(selectedScenario, async () => {
  await loadPersona()
})

onMounted(async () => {
  ensureScenarioRandomEveryTime()

  // ✅ 시나리오 결정된 후 페르소나 로드
  await loadPersona()

  // sessionId가 이미 있으면 저장된 대화 복원
  loadChatFromStorage()

  await focusInput()
  await scrollToBottom(false)
})

/* =========================
 *   결과 페이지 이동 로직
 * ========================= */

function goResultIfNeeded(parsed, r) {
  const shouldEnd = !!(r?.end || parsed?.end)
  if (!shouldEnd) return false

  const serverLogs = (parsed?.eventLogs ?? r?.eventLogs ?? {}) || {}
  const mergedLogs = mergeLogs(serverLogs, localEventLogs.value)

  if (!hasAnyEventLogs(mergedLogs)) {
    pushBot('아직 기록된 위험 이벤트가 없어요. 대화를 조금 더 진행해볼까요?')
    return true
  }

  const userId = getUserId()
  const trackId = selectedTrack.value ?? getTrackId()
  const scenarioId = selectedScenario.value ?? 'romance'
  const sid = serverSessionId.value || parsed?.sessionId || 'no-session'

  const resultPayload = {
    userId,
    trackId,
    scenarioId,
    sessionId: sid,
    createdAt: Date.now(),
    currentEvent: (parsed?.currentEvent ?? parsed?.CurrentEvent ?? r?.currentEvent ?? null),
    eventLogs: mergedLogs,
    stage: (parsed?.['단계'] ?? parsed?.stage ?? r?.stage ?? null),
  }

  localStorage.setItem(`scam_result:${userId}:${trackId}:${scenarioId}`, JSON.stringify(resultPayload))
  localStorage.setItem(`scam_result_latest:${userId}`, JSON.stringify(resultPayload))
  localStorage.setItem('scam_result', JSON.stringify(resultPayload))
  localStorage.setItem(`scam_result:${sid}:${trackId}:${scenarioId}`, JSON.stringify(resultPayload))
  localStorage.setItem(`simscam_last_scenario:${userId}`, scenarioId)
  localStorage.setItem(`simscam_last_track:${userId}`, trackId)

  router.push({ path: '/result', state: { result: resultPayload } })
  return true
}

/* =========================
 *  메시지 전송: scenario만 전달 (백엔드 history 없음)
 * ========================= */

const send = async () => {
  const text = input.value.trim()
  if (!text) return

  chats.value.push({ role: 'user', text })
  input.value = ''
  focusInput()

  try {
    const scenarioId = selectedScenario.value ?? 'romance'
    const data = await sendChat(text, { scenario: scenarioId })

    const parsed = normalizeResponse(data)

    // (백엔드가 sessionId를 내려주는 구조가 아니라면 이 블록은 의미 없음 — 있어도 무해)
    if (parsed?.sessionId && typeof parsed.sessionId === 'string') {
      if (serverSessionId.value !== parsed.sessionId) {
        serverSessionId.value = parsed.sessionId
        localStorage.setItem('simscam_server_session_id', parsed.sessionId)
        loadChatFromStorage()
      }
    }

    const r = normalizeServerPayload(parsed)

    if (goResultIfNeeded(parsed, r)) return

    pushBot(r.text, { stage: r.stage, end: r.end })

    const ev = pickEventName(parsed)
    pendingEvent.value = ev ? { event: ev } : null

  } catch (e) {
    pushBot('연결 실패')
    console.error(e)
  } finally {
    focusInput()
  }
}

/* =========================
 *  선택 이벤트 전송: scenario만 전달 (백엔드 history 없음)
 * ========================= */

const decide = async (choice) => {
  const answer = choice === 'YES' ? 'yes' : 'no'

  const eventObj = pendingEvent.value
  const event = eventObj?.event
  pendingEvent.value = null

  if (!event) {
    pushBot('선택 전송 실패: event 없음')
    return
  }

  chats.value.push({
    role: 'system',
    text: eventToActionText(event, answer),
    meta: { kind: 'eventAction', event, answer }
  })

  const step = nextStepFromLogs(localEventLogs.value)
  localEventLogs.value[`${step}_${event}`] = answer

  try {
    const scenarioId = selectedScenario.value ?? 'romance'
    const data = await sendDecision(event, answer, { scenario: scenarioId })

    const parsed = normalizeResponse(data)

    if (parsed?.sessionId && typeof parsed.sessionId === 'string') {
      if (serverSessionId.value !== parsed.sessionId) {
        serverSessionId.value = parsed.sessionId
        localStorage.setItem('simscam_server_session_id', parsed.sessionId)
        loadChatFromStorage()
      }
    }

    const r = normalizeServerPayload(parsed)

    if (goResultIfNeeded(parsed, r)) return

    pushBot(r.text, { stage: r.stage, end: r.end })

    const ev2 = pickEventName(parsed)
    pendingEvent.value = ev2 ? { event: ev2 } : null

  } catch (e) {
    pushBot('선택 전송 실패')
    console.error(e)
  } finally {
    focusInput()
  }
}

/* =========================
 *  이벤트 카드 텍스트
 * ========================= */

function eventToQuestion(eventName) {
  switch (eventName) {
    case '금전요구': return '지금 돈을 보내 달라는 요청에 응할까요?'
    case '개인정보요구': return '개인정보를 보내 달라는 요청에 응할까요?'
    case '투자권유': return '투자를 하자는 제안을 받아들일까요?'
    case '앱설치유도': return '앱을 설치하라는 요청을 따를까요?'
    case '사이트가입유도': return '사이트에 가입하라는 요청을 따를까요?'
    default: return '요청을 따를까요?'
  }
}

function eventToActionText(eventName, answer) {
  const yes = answer === "yes"

  switch (eventName) {
    case "금전요구":
      return yes ? "요청대로 돈을 송금했습니다." : "송금 요청을 거절했습니다."
    case "개인정보요구":
      return yes ? "요청대로 개인정보를 전달했습니다." : "개인정보 제공을 거절했습니다."
    case "투자권유":
      return yes ? "요청대로 투자를 진행했습니다." : "투자 제안을 거절했습니다."
    case "앱설치유도":
      return yes ? "요청대로 앱을 설치했습니다." : "앱 설치를 거절했습니다."
    case "사이트가입유도":
      return yes ? "요청대로 사이트에 가입했습니다." : "사이트 가입을 거절했습니다."
    default:
      return yes ? "요청을 수락했습니다." : "요청을 거절했습니다."
  }
}
</script>

<style scoped>

/* 중앙 시스템 로그 */
.systemRow{
  width: 100%;
  display: flex;
  justify-content: center;
  margin: 10px 0;
}

.systemPill{
  display: inline-flex;
  align-items: center;
  justify-content: center;

  padding: 6px 10px;
  border-radius: 999px;

  font-size: 12px;
  font-weight: 700;
  color: #777;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid #eee;
  box-shadow: none;
}

.profile .roomAvatar {
  box-shadow:
      0 0 0 2px #fff,
      0 0 0 4px #6ee7b7;
}

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

/* 위 더보기 */
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
  display: inline-flex;
  flex-direction: column;
  align-items: flex-start;

  max-width: 68%;
  width: fit-content;
  min-width: 0;
  flex: 0 0 auto;

  padding: 6px 10px;
  border-radius: 16px;
  border: 1px solid #e9e9e9;
  background: #fff;

  white-space: pre-wrap;
  word-break: keep-all;
  overflow-wrap: break-word;
  line-break: auto;

  line-height: 1.25;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.04);
}


.actionBubble{
  background: #111 !important;
  color: #fff !important;
  border-color: #111 !important;
  font-weight: 800;
}


.text{
  white-space: pre-wrap;
  word-break: keep-all;
  overflow-wrap: break-word;
  line-break: auto;
  text-wrap: wrap;
  font-size: 14px;
  line-height: 1.45;
}
.row.me .bubble { background: #efe9ff; border-color: #e1d6ff; }

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
.clickable { cursor: pointer; }

.profileModal{
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.profileCard{
  width: min(420px, 92vw);
  background: #fff;
  border: 1px solid #eee;
  border-radius: 18px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.18);
  padding: 16px;
}

.pcTop{
  display: flex;
  gap: 12px;
  align-items: center;
  position: relative;
}

.pcAvatar{
  width: 64px;
  height: 64px;
  border-radius: 16px;
  object-fit: cover;
  border: 1px solid #eee;
}

.pcMeta{ flex: 1; min-width: 0; }
.pcNameRow{ display: flex; gap: 8px; align-items: center; }
.pcName{ font-size: 18px; font-weight: 800; }
.pcBadge{
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 999px;
  background: #eef6ff;
  color: #2b6cb0;
  border: 1px solid #d6eaff;
}
.pcSub{ font-size: 12px; opacity: .75; margin-top: 2px; }
.pcJob{ font-size: 13px; margin-top: 4px; opacity: .9; }

.pcClose{
  position: absolute;
  right: 0;
  top: 0;
  border: 0;
  background: transparent;
  font-size: 18px;
  cursor: pointer;
  opacity: .7;
}

.pcStats{
  display: flex;
  justify-content: space-around;
  padding: 12px 0;
  margin-top: 10px;
  border-top: 1px solid #f1f1f1;
  border-bottom: 1px solid #f1f1f1;
}
.pcStat{ text-align: center; }
.pcStat .num{ font-weight: 800; }
.pcStat .label{ font-size: 12px; opacity: .7; }

.pcBio{ padding: 12px 0; }
.pcBioLine{ font-size: 13px; line-height: 1.45; margin-top: 6px; }
.pcBioLine b{ margin-right: 6px; }

.pcActions{
  display: flex;
  gap: 10px;
  margin-top: 6px;
}
.pcBtn{
  flex: 1;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid #e7e7e7;
  background: #fff;
  cursor: pointer;
}
.pcBtn.primary{
  background: #111;
  color: #fff;
  border-color: #111;
}

</style>
