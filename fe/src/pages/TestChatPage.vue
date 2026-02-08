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

          <!-- ✅ NEW: 리셋 버튼 -->
          <button class="ghost danger" type="button" title="대화 초기화" :disabled="resetting" @click="onResetChat">
            <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <!-- refresh-cw 느낌 아이콘 -->
              <path d="M21 12a9 9 0 1 1-2.64-6.36" />
              <polyline points="21 3 21 9 15 9" />
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

        <!-- ✅ NEW: 리셋 결과 토스트 -->
        <div v-if="resetMsg" class="resetToast">{{ resetMsg }}</div>
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
import { useRouter, useRoute } from 'vue-router'
import { sendChat, sendDecision, getPersona, resetChat } from '../api/chatApi'

const router = useRouter()
const route = useRoute()

/* =========================
 *  기본 상태
 * ========================= */
const input = ref('')
const chats = ref([])
const boxRef = ref(null)
const inputRef = ref(null)

const showInfo = ref(false)

/* 프로필 모달 */
const showProfile = ref(false)
const profileStats = ref({ posts: 18, followers: '1.2만', following: 321 })

/* 페르소나 */
const personaLoading = ref(true)
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

/* 이벤트 */
const pendingEvent = ref(null)
const localEventLogs = ref({})

/* 스크롤 */
const atBottom = ref(true)
const showNewMsgBtn = ref(false)
const newMsgCount = ref(0)

/* 렌더링 제한 */
const MAX_RENDER = 50
const LOAD_STEP = 30
const renderCount = ref(MAX_RENDER)

const canLoadMore = computed(() => chats.value.length > renderCount.value)
const startIndex = computed(() => Math.max(0, chats.value.length - renderCount.value))
const visibleChats = computed(() => chats.value.slice(startIndex.value))

/* =========================
 *  sid (클라 고정 세션)
 * ========================= */
function getClientSid() {
  let sid = localStorage.getItem('simscam_client_sid')
  if (!sid) {
    sid = 'sid_' + Math.random().toString(36).slice(2) + Date.now().toString(36)
    localStorage.setItem('simscam_client_sid', sid)
  }
  return sid
}

/* =========================
 *  시나리오 선택
 * ========================= */
const SCENARIOS_BY_TRACK = {
  romance: ['romance'], // 필요하면 ['romance', 'invest']
  job: ['job'],
  invest: ['invest'],
}

const selectedTrack = ref(null)
const selectedScenario = ref(null)

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
  selectedScenario.value = pool.length
      ? pool[Math.floor(Math.random() * pool.length)]
      : 'romance'
}

/* =========================
 *  localStorage 저장/복원
 * ========================= */
function chatStorageKey(sid, scenario) {
  return `scam_chat:${sid}:${scenario || 'romance'}`
}

function saveChatToStorage() {
  const sid = getClientSid()
  const sc = selectedScenario.value || 'romance'

  const payload = {
    sid,
    scenario: sc,
    updatedAt: Date.now(),
    chats: chats.value,
    localEventLogs: localEventLogs.value,
    persona: persona.value,
  }

  try {
    localStorage.setItem(chatStorageKey(sid, sc), JSON.stringify(payload))
  } catch {
    // ignore
  }
}

function loadChatFromStorage() {
  const sid = getClientSid()
  const sc = selectedScenario.value || 'romance'
  const raw = localStorage.getItem(chatStorageKey(sid, sc))
  if (!raw) return false

  try {
    const saved = JSON.parse(raw)
    if (Array.isArray(saved?.chats)) chats.value = saved.chats
    if (saved?.localEventLogs && typeof saved.localEventLogs === 'object') localEventLogs.value = saved.localEventLogs
    if (saved?.persona && typeof saved.persona === 'object') persona.value = { ...persona.value, ...saved.persona }
    return true
  } catch {
    return false
  }
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
  const text = parsed?.text ?? parsed?.reply ?? parsed?.message ?? '(응답 없음)'
  const currentEvent = parsed?.currentEvent ?? parsed?.CurrentEvent ?? null
  const event = parsed?.event ?? currentEvent ?? null
  const end = !!parsed?.end
  const stage = parsed?.['단계'] ?? parsed?.stage ?? null
  const eventLogs = parsed?.eventLogs ?? {}
  const image = parsed?.image ?? null
  return { text, currentEvent, event, end, stage, eventLogs, image }
}

function pickEventName(parsed) {
  const ev = parsed?.event ?? parsed?.currentEvent ?? parsed?.CurrentEvent ?? null
  return (typeof ev === 'string' && ev.trim()) ? ev.trim() : null
}

function mergeLogs(serverLogs = {}, localLogs = {}) {
  return { ...(serverLogs || {}), ...(localLogs || {}) }
}

function hasAnyEventLogs(logs) {
  return !!logs && Object.keys(logs).length > 0
}

function nextStepFromLogs(logs) {
  const steps = Object.keys(logs || {}).map(k => Number(String(k).split('_')[0]) || 0)
  return (steps.length ? Math.max(...steps) : 0) + 1
}

function pushBot(text, extra = {}) {
  const norm = (text ?? '').toString().trim()
  if (!norm) {
    chats.value.push({ role: 'bot', text: '(응답 없음)', ...extra })
    return
  }

  // 중복 방지
  const lastBot = [...chats.value].reverse().find(m => m.role === 'bot')
  const lastNorm = (lastBot?.text ?? '').toString().trim()
  if (lastNorm && lastNorm === norm) return

  chats.value.push({ role: 'bot', text: norm, ...extra })
}

/* =========================
 *  Persona + avatar
 * ========================= */
function defaultAvatarByScenario(scenarioId) {
  if (scenarioId === 'romance') return '/img/일본인 여자.png'
  if (scenarioId === 'invest') return '/img/김부자.png'
  return '/img/씹덕1.jpeg'
}

function pickAvatarByPersona(raw = {}, scenarioId = 'romance') {
  const job = (raw['직업'] ?? raw.job ?? '').toString()
  const traits = (raw['특징'] ?? raw.traits ?? '').toString()

  if (job.includes('자산') || job.includes('애널') || traits.includes('외제차') || traits.includes('성공')) {
    return '/img/김부자.png'
  }
  if (job.includes('유학생') || traits.includes('일본')) {
    return '/img/일본인 여자.png'
  }
  return defaultAvatarByScenario(scenarioId)
}

function mapPersona(raw = {}, scenarioId = 'romance') {
  const name = raw['이름'] ?? raw['닉네임'] ?? raw['Name'] ?? '상대'
  const age = raw['나이'] ?? raw['age'] ?? ''
  const job = raw['직업'] ?? raw['job'] ?? ''
  const location = raw['주소지'] ?? raw['거주지'] ?? raw['location'] ?? ''
  const personality = raw['성격'] ?? raw['personality'] ?? ''
  const traits = raw['특징'] ?? raw['traits'] ?? ''

  const subtitle = [job, location].filter(Boolean).join(' · ')
  const avatarUrl = pickAvatarByPersona(raw, scenarioId)

  return { name, age, job, location, personality, traits, subtitle, avatarUrl }
}

async function loadPersona() {
  personaLoading.value = true
  const scenarioId = selectedScenario.value ?? 'romance'

  try {
    const raw = await getPersona(scenarioId)
    const mapped = mapPersona(raw, scenarioId)
    persona.value = { ...persona.value, ...mapped }
    profileStats.value = buildProfileStats(persona.value)
  } catch (e) {
    console.error('[persona] load failed', e)
    persona.value = { ...persona.value, name: '상대', avatarUrl: defaultAvatarByScenario(scenarioId) }
    profileStats.value = buildProfileStats(persona.value)
  } finally {
    personaLoading.value = false
  }
}

function buildProfileStats(p) {
  const job = (p.job ?? '').toString()
  const traits = (p.traits ?? '').toString()
  const location = (p.location ?? '').toString()
  const personality = (p.personality ?? '').toString()

  const rich =
      job.includes('자산') ||
      job.includes('애널') ||
      job.includes('투자') ||
      traits.includes('성공') ||
      traits.includes('외제차')

  const japaneseStudent =
      job.includes('유학생') ||
      job.includes('학생') ||
      location.includes('일본') ||
      traits.includes('일본') ||
      personality.includes('일본')

  if (rich) return { posts: 73, followers: '33.2만', following: 123 }
  if (japaneseStudent) return { posts: 24, followers: '4.8천', following: 612 }
  return { posts: 18, followers: '1.2만', following: 321 }
}

function openProfile() {
  profileStats.value = buildProfileStats(persona.value)
  showProfile.value = true
}
function closeProfile() {
  showProfile.value = false
}

/* =========================
 *  Focus / Scroll
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

  if (el.scrollTop < 30) loadOlderAuto()
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
  if (!el || !canLoadMore.value || loadingOlder) return

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

/* 새 메시지 카운트 */
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

/* 저장 */
watch(
    [chats, localEventLogs, selectedScenario, persona],
    () => saveChatToStorage(),
    { deep: true }
)

/* =========================
 *  결과 페이지 이동
 * ========================= */
function getUserId() {
  return localStorage.getItem('simscam_user_id') || 'guest'
}

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
  const sid = getClientSid()

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
  localStorage.setItem(`scam_result:${sid}:${trackId}:${scenarioId}`, JSON.stringify(resultPayload)) // 레거시/호환
  localStorage.setItem(`scam_result_latest:${userId}`, JSON.stringify(resultPayload))
  localStorage.setItem('scam_result', JSON.stringify(resultPayload))

  localStorage.setItem(`simscam_last_scenario:${userId}`, scenarioId)
  localStorage.setItem(`simscam_last_track:${userId}`, trackId)

  // scenario를 query로도 넘기면 결과페이지에서 시나리오 리셋이 안정적
  router.push({ path: '/result', query: { scenario: scenarioId }, state: { result: resultPayload } })
  return true
}

/* =========================
 *  전송/선택
 * ========================= */
const send = async () => {
  const text = input.value.trim()
  if (!text) return

  chats.value.push({ role: 'user', text })
  input.value = ''
  focusInput()

  try {
    const scenarioId = selectedScenario.value ?? 'romance'
    const parsed = normalizeResponse(await sendChat(text, { scenario: scenarioId }))
    const r = normalizeServerPayload(parsed)

    if (goResultIfNeeded(parsed, r)) return

    pushBot(r.text, { stage: r.stage, end: r.end, image: r.image })

    const ev = pickEventName(parsed)
    pendingEvent.value = ev ? { event: ev } : null
  } catch (e) {
    console.error(e)
    pushBot('연결 실패')
  } finally {
    focusInput()
  }
}

const decide = async (choice) => {
  const answer = choice === 'YES' ? 'yes' : 'no'
  const event = pendingEvent.value?.event
  pendingEvent.value = null

  if (!event) {
    pushBot('선택 전송 실패: event 없음')
    return
  }

  chats.value.push({
    role: 'system',
    text: eventToActionText(event, answer),
    meta: { kind: 'eventAction', event, answer },
  })

  const step = nextStepFromLogs(localEventLogs.value)
  localEventLogs.value[`${step}_${event}`] = answer

  try {
    const scenarioId = selectedScenario.value ?? 'romance'
    const parsed = normalizeResponse(await sendDecision(event, answer, { scenario: scenarioId }))
    const r = normalizeServerPayload(parsed)

    if (r.eventLogs && typeof r.eventLogs === 'object') {
      localEventLogs.value = mergeLogs(r.eventLogs, localEventLogs.value)
    }

    if (goResultIfNeeded(parsed, r)) return

    pushBot(r.text, { stage: r.stage, end: r.end, image: r.image })

    const ev2 = pickEventName(parsed)
    pendingEvent.value = ev2 ? { event: ev2 } : null
  } catch (e) {
    console.error(e)
    pushBot('선택 전송 실패')
  } finally {
    focusInput()
  }
}

/* 이벤트 문구 */
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
  const yes = answer === 'yes'
  switch (eventName) {
    case '금전요구': return yes ? '요청대로 돈을 송금했습니다.' : '송금 요청을 거절했습니다.'
    case '개인정보요구': return yes ? '요청대로 개인정보를 전달했습니다.' : '개인정보 제공을 거절했습니다.'
    case '투자권유': return yes ? '요청대로 투자를 진행했습니다.' : '투자 제안을 거절했습니다.'
    case '앱설치유도': return yes ? '요청대로 앱을 설치했습니다.' : '앱 설치를 거절했습니다.'
    case '사이트가입유도': return yes ? '요청대로 사이트에 가입했습니다.' : '사이트 가입을 거절했습니다.'
    default: return yes ? '요청을 수락했습니다.' : '요청을 거절했습니다.'
  }
}

// reset
const resetting = ref(false)
const resetMsg = ref('')

async function onResetChat() {
  const sc = selectedScenario.value ?? 'romance'
  const ok = confirm(`'${sc}' 대화를 초기화할까요?\n(대화/이벤트 기록이 모두 리셋됩니다)`)
  if (!ok) return

  resetting.value = true
  resetMsg.value = ''

  try {
    // 1) 백엔드 메모리 초기화
    const data = await resetChat({ scenario: sc })

    // 2) 로컬 스토리지(현재 시나리오 채팅 저장본) 삭제
    const sid = getClientSid()
    try { localStorage.removeItem(chatStorageKey(sid, sc)) } catch {}

    // 3) 화면 상태 초기화
    chats.value = []
    pendingEvent.value = null
    localEventLogs.value = {}
    input.value = ''

    renderCount.value = MAX_RENDER
    atBottom.value = true
    showNewMsgBtn.value = false
    newMsgCount.value = 0

    // 4) 페르소나 다시 로드(선택)
    await loadPersona()

    resetMsg.value = data?.message || '초기화 완료'

    await nextTick()
    await scrollToBottom(false)
    await focusInput()
  } catch (e) {
    resetMsg.value = e?.message || '초기화 실패'
  } finally {
    resetting.value = false
    // 토스트 자동 숨김(선택)
    setTimeout(() => { resetMsg.value = '' }, 1600)
  }
}

// 마운트
onMounted(async () => {
  ensureScenarioRandomEveryTime()

  // 저장된 대화 먼저 복원
  const hasSavedChat = loadChatFromStorage()

  // 페르소나 로드
  await loadPersona()

  // 3) 기존 대화가 없다면 빈 문자열을 보내 첫 응답 유도
  if (!hasSavedChat || chats.value.length === 0) {
    await sendFirstGreeting()
  }

  await focusInput()
  await scrollToBottom(false)
})

/* ✅ 새로운 함수: 첫 인사 유도 */
async function sendFirstGreeting() {
  try {
    const scenarioId = selectedScenario.value ?? 'romance'
    // 빈 문자열을 보내 서버가 "안녕? 반가워" 같은 첫 메시지를 주도록 함
    const parsed = normalizeResponse(await sendChat("", { scenario: scenarioId }))
    const r = normalizeServerPayload(parsed)

    // 응답이 있을 경우 화면에 표시
    if (r.text && r.text !== '(응답 없음)') {
      pushBot(r.text, { stage: r.stage, end: r.end, image: r.image })

      // 혹시 첫 마디부터 이벤트를 준다면 처리
      const ev = pickEventName(parsed)
      pendingEvent.value = ev ? { event: ev } : null
    }
  } catch (e) {
    console.error('[Initial Greeting Error]', e)
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


.ghost.danger{
  border-color: rgba(255, 120, 120, 0.35);
  background: rgba(255, 80, 80, 0.06);
}
.ghost:disabled{ opacity: 0.6; cursor: not-allowed; }

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

.resetToast{
  position: sticky;
  bottom: 64px;
  left: 50%;
  transform: translateX(-50%);
  width: fit-content;
  margin: 0 auto;
  z-index: 5;
  padding: 10px 12px;
  border-radius: 999px;
  border: 1px solid #e7e7e7;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(6px);
  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.10);
  font-weight: 800;
  font-size: 12px;
  color: #444;
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
