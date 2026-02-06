import { createRouter, createWebHistory } from 'vue-router'

import StartView from '../pages/StartPage.vue'
import TestChatPage from '../pages/TestChatPage.vue'
import AboutPage from '../pages/AboutPage.vue'
import ResultPage from '../pages/ResultPage.vue'

const routes = [
    {
        path: '/',
        name: 'Start',
        component: StartView,
    },
    {
        path: '/chat',
        name: 'Chat',
        component: TestChatPage,
    },
    {
        path: '/about',
        name: 'About',
        component: AboutPage,
    },
    {
        path: '/result',
        name: 'Result',
        component: ResultPage,
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

export default router
