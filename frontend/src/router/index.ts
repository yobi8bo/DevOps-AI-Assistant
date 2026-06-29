import { createRouter, createWebHistory } from 'vue-router';
import AiCallLogView from '@/views/ai-call-log/AiCallLogView.vue';
import AppLayout from '@/layouts/AppLayout.vue';
import DiagnosisView from '@/views/diagnosis/DiagnosisView.vue';
import DashboardView from '@/views/dashboard/DashboardView.vue';
import HistoryView from '@/views/history/HistoryView.vue';
import LoginView from '@/views/login/LoginView.vue';
import ModelConfigView from '@/views/model-config/ModelConfigView.vue';
import PromptTemplateView from '@/views/prompt-template/PromptTemplateView.vue';
import { useUserStore } from '@/stores/user';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true },
    },
    {
      path: '/',
      component: AppLayout,
      children: [
        {
          path: '',
          name: 'dashboard',
          component: DashboardView,
        },
        {
          path: 'diagnosis',
          name: 'diagnosis',
          component: DiagnosisView,
        },
        {
          path: 'history',
          name: 'history',
          component: HistoryView,
        },
        {
          path: 'model-configs',
          name: 'model-configs',
          component: ModelConfigView,
        },
        {
          path: 'prompt-templates',
          name: 'prompt-templates',
          component: PromptTemplateView,
        },
        {
          path: 'ai-call-logs',
          name: 'ai-call-logs',
          component: AiCallLogView,
        },
      ],
    },
  ],
});

router.beforeEach(async (to) => {
  const userStore = useUserStore();
  if (to.meta.public) {
    if (to.path === '/login' && userStore.isAuthenticated) {
      return typeof to.query.redirect === 'string' ? to.query.redirect : '/';
    }
    return true;
  }

  if (!userStore.isAuthenticated) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    };
  }

  if (!userStore.userInfo) {
    try {
      await userStore.loadCurrentUser();
    } catch {
      userStore.clearSession();
      return {
        path: '/login',
        query: { redirect: to.fullPath },
      };
    }
  }

  return true;
});

export default router;
