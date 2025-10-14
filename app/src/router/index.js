import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import i18n from '@/plugins/i18n';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/recover-password',
      name: 'recover-password',
      component: () => import('@/views/auth/RecoverPasswordView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: { name: 'dashboard/nutrition' },
        },
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { titleKey: 'routes.dashboard' },
        },
        {
          path: 'dashboard/nutrition',
          name: 'dashboard-nutrition',
          component: () => import('@/views/dashboard/NutritionDashboard.vue'),
          meta: { titleKey: 'routes.nutritionDashboard' },
        },
        {
          path: 'users',
          name: 'users',
          component: () => import('@/views/users/UsersListView.vue'),
          meta: { titleKey: 'routes.users' },
        },
        {
          path: 'anamnesis',
          name: 'anamnesis',
          component: () => import('@/views/anamnesis/AnamnesisWizard.vue'),
          meta: { titleKey: 'routes.anamnesis' },
        },
        {
          path: 'diet',
          name: 'diet',
          component: () => import('@/views/diet/DietList.vue'),
          meta: { titleKey: 'routes.diet' },
        },
        {
          path: 'diet/new',
          name: 'diet-new',
          component: () => import('@/views/diet/DietWizard.vue'),
          meta: { titleKey: 'routes.dietNew' },
        },
        {
          path: 'diet/:id/edit',
          name: 'diet-edit',
          component: () => import('@/views/diet/DietWizard.vue'),
          meta: { titleKey: 'routes.dietEdit' },
        },
        {
          path: 'goals',
          name: 'goals',
          component: () => import('@/views/goals/GoalList.vue'),
          meta: { titleKey: 'routes.goals' },
        },
        {
          path: 'photo-evolution',
          name: 'photo-evolution',
          component: () => import('@/views/photoEvolution/PhotoEvolutionView.vue'),
          meta: { titleKey: 'routes.photoEvolution' },
        },
        {
          path: 'photo-evolution/comparison',
          name: 'photo-evolution-comparison',
          component: () => import('@/views/photoEvolution/PhotoEvolutionCompareView.vue'),
          meta: { titleKey: 'routes.photoEvolution' },
        },
        {
          path: 'goals/new',
          name: 'goals-new',
          component: () => import('@/views/goals/GoalWizard.vue'),
          meta: { titleKey: 'routes.goalsNew' },
        },
        {
          path: 'goals/:id/edit',
          name: 'goals-edit',
          component: () => import('@/views/goals/GoalWizard.vue'),
          meta: { titleKey: 'routes.goalsEdit' },
        },
        {
          path: 'whatsapp',
          name: 'whatsapp-nutrition',
          component: () => import('@/views/whatsapp/WhatsAppNutritionView.vue'),
          meta: { titleKey: 'routes.whatsappNutrition' },
        },
        {
          path: 'payments',
          name: 'payments',
          component: () => import('@/views/payments/PaymentsView.vue'),
          meta: { titleKey: 'routes.payments' },
        },
        {
          path: 'settings',
          name: 'settings',
          component: () => import('@/views/settings/SettingsView.vue'),
          meta: { titleKey: 'routes.settings' },
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/profile/ProfileView.vue'),
          meta: { titleKey: 'routes.profile' },
        },
        {
          path: 'references',
          component: () => import('@/views/reference/ReferenceLayout.vue'),
          meta: { titleKey: 'routes.references', requiresAdmin: true },
          children: [
            { path: '', redirect: { name: 'reference-countries' } },
            {
              path: 'countries',
              name: 'reference-countries',
              component: () => import('@/views/reference/ReferenceCountriesView.vue'),
              meta: { titleKey: 'routes.referenceCountries', requiresAdmin: true },
            },
            {
              path: 'ai-prompts',
              name: 'reference-ai-prompts',
              component: () => import('@/views/reference/ReferenceAiPromptsView.vue'),
              meta: { titleKey: 'routes.referenceAiPrompts', requiresAdmin: true },
            },
            {
              path: 'cities',
              name: 'reference-cities',
              component: () => import('@/views/reference/ReferenceCitiesView.vue'),
              meta: { titleKey: 'routes.referenceCities', requiresAdmin: true },
            },
            {
              path: 'education-levels',
              name: 'reference-education-levels',
              component: () => import('@/views/reference/ReferenceEducationLevelsView.vue'),
              meta: { titleKey: 'routes.referenceEducationLevels', requiresAdmin: true },
            },
            {
              path: 'meals',
              name: 'reference-meals',
              component: () => import('@/views/reference/ReferenceMealsView.vue'),
              meta: { titleKey: 'routes.referenceMeals', requiresAdmin: true },
            },
            {
              path: 'professions',
              name: 'reference-professions',
              component: () => import('@/views/reference/ReferenceProfessionsView.vue'),
              meta: { titleKey: 'routes.referenceProfessions', requiresAdmin: true },
            },
          ],
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
    },
  ],
});

router.beforeEach(async (to) => {
  const auth = useAuthStore();

  if (auth.token && !auth.user) {
    try {
      await auth.loadProfile();
    } catch (error) {
      auth.reset();
    }
  }

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } };
  }

  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { name: 'dashboard' };
  }

  if (to.matched.some((record) => record.meta.requiresAdmin)) {
    const isAdmin = (auth.user?.type ?? '').toUpperCase() === 'ADMIN';
    if (!isAdmin) {
      return { name: 'dashboard' };
    }
  }

  if (to.meta.titleKey) {
    const title = i18n.global.t(to.meta.titleKey);
    document.title = `${title} - JM Admin`;
  } else if (to.meta.title) {
    document.title = `${to.meta.title} - JM Admin`;
  } else {
    document.title = 'JM Admin';
  }

  return true;
});

export default router;
