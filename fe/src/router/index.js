import { createRouter, createWebHistory } from 'vue-router'

import StartView from '../pages/StartPage.vue'
import TestChatPage from '../pages/TestChatPage.vue'
import AboutPage from '../pages/AboutPage.vue'

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
        {
            path: '/about',
            name: 'About',
            component: AboutPage        // ABOUT 설명 페이지
        },
    ],
})

export default router
