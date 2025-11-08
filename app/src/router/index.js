import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { can } from '@/utils/permissions';
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
      name: 'landing',
      component: () => import('@/views/LandingPage.vue'),
      meta: { guestOnly: true, title: 'Macro AI' },
    },
    {
      path: '/app',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: { name: 'dashboard-nutrition' },
        },
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { titleKey: 'routes.dashboard', permission: 'ROLE_ANALYTICS_READ' },
        },
        {
          path: 'dashboard/nutrition',
          name: 'dashboard-nutrition',
          component: () => import('@/views/dashboard/NutritionDashboard.vue'),
          meta: { titleKey: 'routes.nutritionDashboard', permission: 'ROLE_ANALYTICS_READ' },
        },
        {
          path: 'dashboard/body-evolution',
          name: 'dashboard-body-evolution',
          component: () => import('@/views/dashboard/BodyEvolutionDashboard.vue'),
          meta: { titleKey: 'routes.dashboardBodyEvolution', permission: 'ROLE_PHOTO_EVOLUTION_READ' },
        },
        {
          path: 'users',
          name: 'users',
          component: () => import('@/views/users/UsersListView.vue'),
          meta: { titleKey: 'routes.users', permission: 'ROLE_USERS_READ' },
        },
        {
          path: 'users/roles',
          name: 'user-roles',
          component: () => import('@/views/users/UserRolesView.vue'),
          meta: { titleKey: 'routes.userRoles', permission: 'ROLE_ADMIN_MANAGE_ROLES' },
        },
        {
          path: 'access-control',
          name: 'access-control',
          component: () => import('@/views/security/AccessControlView.vue'),
          meta: { titleKey: 'routes.accessControl', permission: 'ROLE_ADMIN_MANAGE_ROLES' },
        },
        {
          path: 'exercises',
          name: 'exercises',
          component: () => import('@/views/exercises/ExercisesListView.vue'),
          meta: { titleKey: 'routes.exercises', permission: 'ROLE_EXERCISES_READ' },
        },
        {
          path: 'anamnesis',
          name: 'anamnesis',
          component: () => import('@/views/anamnesis/AnamnesisWizard.vue'),
          meta: { titleKey: 'routes.anamnesis', permission: 'ROLE_ANAMNESIS_READ' },
        },
        {
          path: 'diet',
          name: 'diet',
          component: () => import('@/views/diet/DietList.vue'),
          meta: { titleKey: 'routes.diet', permission: 'ROLE_DIETS_READ' },
        },
        {
          path: 'diet/new',
          name: 'diet-new',
          component: () => import('@/views/diet/DietWizard.vue'),
          meta: { titleKey: 'routes.dietNew', permission: 'ROLE_DIETS_CREATE' },
        },
        {
          path: 'diet/:id/edit',
          name: 'diet-edit',
          component: () => import('@/views/diet/DietWizard.vue'),
          meta: { titleKey: 'routes.dietEdit', permission: 'ROLE_DIETS_UPDATE' },
        },
        {
          path: 'goals',
          name: 'goals',
          component: () => import('@/views/goals/GoalList.vue'),
          meta: { titleKey: 'routes.goals', permission: 'ROLE_GOALS_READ' },
        },
        {
          path: 'photo-evolution',
          name: 'photo-evolution',
          component: () => import('@/views/photoEvolution/PhotoEvolutionView.vue'),
          meta: { titleKey: 'routes.photoEvolution', permission: 'ROLE_PHOTO_EVOLUTION_READ' },
        },
        {
          path: 'photo-evolution/comparison',
          name: 'photo-evolution-comparison',
          component: () => import('@/views/photoEvolution/PhotoEvolutionCompareView.vue'),
          meta: { titleKey: 'routes.photoEvolution', permission: 'ROLE_PHOTO_EVOLUTION_READ' },
        },
        {
          path: 'goals/new',
          name: 'goals-new',
          component: () => import('@/views/goals/GoalWizard.vue'),
          meta: { titleKey: 'routes.goalsNew', permission: 'ROLE_GOALS_CREATE' },
        },
        {
          path: 'goals/:id/edit',
          name: 'goals-edit',
          component: () => import('@/views/goals/GoalWizard.vue'),
          meta: { titleKey: 'routes.goalsEdit', permission: 'ROLE_GOALS_UPDATE' },
        },
        {
          path: 'whatsapp',
          name: 'whatsapp-nutrition',
          component: () => import('@/views/whatsapp/WhatsAppNutritionView.vue'),
          meta: { titleKey: 'routes.whatsappNutrition', permission: 'ROLE_WHATSAPP_MANAGEMENT_READ' },
        },
        {
          path: 'reminders',
          name: 'reminders',
          component: () => import('@/views/reminders/RemindersView.vue'),
          meta: { titleKey: 'routes.reminders', permission: 'ROLE_REMINDERS_READ' },
        },
        {
          path: 'payments',
          name: 'payments',
          component: () => import('@/views/payments/PaymentsView.vue'),
          meta: { titleKey: 'routes.payments', permission: 'ROLE_PAYMENTS_READ' },
        },
        {
          path: 'settings',
          name: 'settings',
          component: () => import('@/views/settings/SettingsView.vue'),
          meta: { titleKey: 'routes.settings', permission: 'ROLE_USER_SETTINGS_READ' },
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
          meta: { titleKey: 'routes.references', permission: 'ROLE_REFERENCE_MANAGEMENT_READ' },
          children: [
            { path: '', redirect: { name: 'reference-countries' } },
            {
              path: 'countries',
              name: 'reference-countries',
              component: () => import('@/views/reference/ReferenceCountriesView.vue'),
              meta: { titleKey: 'routes.referenceCountries', permission: 'ROLE_REFERENCE_MANAGEMENT_READ' },
            },
            {
              path: 'ai-prompts',
              name: 'reference-ai-prompts',
              component: () => import('@/views/reference/ReferenceAiPromptsView.vue'),
              meta: { titleKey: 'routes.referenceAiPrompts', permission: 'ROLE_REFERENCE_MANAGEMENT_READ' },
            },
            {
              path: 'exercise-references',
              name: 'reference-exercise-references',
              component: () => import('@/views/reference/ReferenceExerciseReferencesView.vue'),
              meta: { titleKey: 'routes.referenceExerciseReferences', permission: 'ROLE_REFERENCE_MANAGEMENT_READ' },
            },
            {
              path: 'cities',
              name: 'reference-cities',
              component: () => import('@/views/reference/ReferenceCitiesView.vue'),
              meta: { titleKey: 'routes.referenceCities', permission: 'ROLE_REFERENCE_MANAGEMENT_READ' },
            },
            {
              path: 'education-levels',
              name: 'reference-education-levels',
              component: () => import('@/views/reference/ReferenceEducationLevelsView.vue'),
              meta: { titleKey: 'routes.referenceEducationLevels', permission: 'ROLE_REFERENCE_MANAGEMENT_READ' },
            },
            {
              path: 'meals',
              name: 'reference-meals',
              component: () => import('@/views/reference/ReferenceMealsView.vue'),
              meta: { titleKey: 'routes.referenceMeals', permission: 'ROLE_REFERENCE_MANAGEMENT_READ' },
            },
            {
              path: 'professions',
              name: 'reference-professions',
              component: () => import('@/views/reference/ReferenceProfessionsView.vue'),
              meta: { titleKey: 'routes.referenceProfessions', permission: 'ROLE_REFERENCE_MANAGEMENT_READ' },
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
    {
      path: '/unauthorized',
      name: 'unauthorized',
      component: () => import('@/views/UnauthorizedView.vue'),
      meta: { titleKey: 'routes.unauthorized' },
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

  const requiredPermission = to.matched
    .filter((record) => record.meta?.permission)
    .map((record) => record.meta.permission)
    .find(Boolean);

  if (requiredPermission && !can(requiredPermission)) {
    return { name: 'unauthorized' };
  }

  const baseTitle = '🌿 Macro AI';

  if (to.meta.titleKey) {
    const title = i18n.global.t(to.meta.titleKey);
    document.title = `${title} · ${baseTitle}`;
  } else if (to.meta.title) {
    document.title = `${to.meta.title} · ${baseTitle}`;
  } else {
    document.title = baseTitle;
  }

  return true;
});

export default router;
