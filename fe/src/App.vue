<template>
  <div class="app">
    <router-view v-slot="{ Component, route }">
      <!-- 풀블리드 페이지 -->
      <component
          :is="Component"
          v-if="route.path === '/' || route.path.startsWith('/chat')"
      />

      <!-- 그 외 페이지는 가운데 컨테이너 -->
      <div v-else class="container">
        <component :is="Component" />
      </div>
    </router-view>
  </div>
</template>

<style>
.app { min-height: 100vh; }
.container {
  max-width: 980px;
  margin: 0 auto;
  padding: 0 18px;
  color: #000;
}
</style>
