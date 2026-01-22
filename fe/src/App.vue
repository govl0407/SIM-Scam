<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const message = ref('로딩중...')

onMounted(async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/hello')
    message.value = res.data
  } catch (e) {
    message.value = '백엔드 연결 실패'
    console.error(e)
  }
})
</script>

<template>
  <main>
    <h1>SIM-Scam</h1>
    <p> 안녕하시렵니까</p>
  </main>
</template>

<style scoped>
main {
  padding: 40px;
  font-family: Arial, sans-serif;
}
</style>
