<script setup>
import {ref, nextTick} from 'vue'
import { sendChat } from '../api/chatApi'


const input = ref('')
const chats = ref([])
const boxRef = ref(null)

const send = async () => {
  const text = input.value.trim()
  if (!text) return

  chats.value.push({ role: 'user', text })
  input.value = ''

  await nextTick()
  boxRef.value.scrollTop = boxRef.value.scrollHeight

  try {
    const reply = await sendChat(text)
    chats.value.push({ role: 'bot', text: reply ?? '(응답 없음)' })
  } catch (e) {
    chats.value.push({ role: 'bot', text: '연결 실패' })
    console.error(e)
  }

  await nextTick()
  boxRef.value.scrollTop = boxRef.value.scrollHeight
}
</script>

<template>
  <main class="wrap">
    <!-- 상단 -->
    <header class="header">
      <img class="avatar" src="https://placekitten.com/80/80"/>
      <div class="info">
        <div class="name">최정민(로맨스스캠범)</div>
        <div class="status">online 🟢</div>
      </div>
    </header>

    <!-- 채팅 -->
    <section class="chat" ref="boxRef">
      <div v-for="(c, i) in chats" :key="i" :class="['msg', c.role]">
        {{ c.text }}
      </div>
    </section>

    <!-- 입력 -->
    <footer class="input">
      <input
          v-model="input"
          @keyup.enter="send"
          placeholder="메시지 입력"
      />
      <button @click="send">전송</button>

    </footer>
  </main>
</template>

<style scoped>
.wrap {
  max-width: 420px;
  height: 640px;
  margin: 30px auto;
  border: 1px solid #ddd;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  font-family: system-ui, -apple-system, sans-serif;
}

/* 상단 */
.header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-bottom: 1px solid #eee;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.name {
  font-weight: 600;
}

.status {
  font-size: 12px;
  color: #777;
}

/* 채팅 */
.chat {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
  background: #fafafa;
}

.msg {
  max-width: 70%;
  padding: 8px 12px;
  margin: 6px 0;
  border-radius: 12px;
  font-size: 14px;
}

.msg.bot {
  background: white;
  border: 1px solid #ddd;
}

.msg.user {
  background: #e9e9e9;
  margin-left: auto;
}

/* 입력 */
.input {
  display: flex;
  gap: 8px;
  padding: 10px;
  border-top: 1px solid #eee;
}

.input input {
  flex: 1;
  padding: 8px;
}

.input button {
  padding: 8px 12px;
}
</style>
