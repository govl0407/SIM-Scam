import { createRouter, createWebHistory } from 'vue-router'

import StartView from '../pages/StartView.vue'
import TestChatPage from '../pages/TestChatPage.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'Start',
            component: StartView,   // 시작페이지
        },
        {
            path: '/chat',
            name: 'Chat',
            component: TestChatPage // DM 채팅 페이지
        },
    ],
})

export default router
