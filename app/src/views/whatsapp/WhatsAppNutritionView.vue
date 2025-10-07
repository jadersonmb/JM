<template>
  <div class="flex flex-col gap-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-semibold text-slate-900 text-blue-600 font-bold">{{ t('whatsappNutrition.title') }}</h2>
        <p class="text-sm text-slate-500">{{ t('whatsappNutrition.subtitle') }}</p>
      </div>
      <button class="btn-secondary" @click="refreshData" :disabled="loading">
        <span v-if="loading">{{ t('whatsappNutrition.actions.refreshing') }}</span>
        <span v-else>{{ t('whatsappNutrition.actions.refresh') }}</span>
      </button>
    </div>

    <div class="grid gap-6 lg:grid-cols-2">
      <section class="rounded-2xl bg-white p-6 shadow-sm">
        <header class="mb-4 flex items-center justify-between">
          <div>
            <h3 class="text-lg font-semibold text-slate-900 font-bold">{{ t('whatsappNutrition.feed.title') }}</h3>
            <p class="text-xs text-slate-500">{{ t('whatsappNutrition.feed.description') }}</p>
          </div>
          <span class="rounded-full bg-primary-50 px-3 py-1 text-xs font-semibold text-primary-600">
            {{ t('whatsappNutrition.feed.count', { count: feed.length }) }}
          </span>
        </header>

        <div v-if="loading" class="flex h-64 items-center justify-center text-sm text-slate-500">
          {{ t('whatsappNutrition.feed.loading') }}
        </div>
        <div v-else-if="!feed.length" class="flex h-64 items-center justify-center text-sm text-slate-500">
          {{ t('whatsappNutrition.feed.empty') }}
        </div>
        <ul v-else class="space-y-4 overflow-y-auto pr-2 max-h-auto">
          <li v-for="item in feed" :key="item.id" class="flex gap-4 rounded-xl border border-slate-200 p-4">
            <div class="h-20 w-20 flex-shrink-0 overflow-hidden rounded-xl bg-slate-100">
              <img
                v-if="item.imageUrl"
                :src="item.imageUrl"
                :alt="item.nutrition?.foodName ?? t('whatsappNutrition.feed.alt')"
                class="h-full w-full object-cover"
              />
              <div v-else class="flex h-full w-full items-center justify-center text-xs font-semibold text-slate-400">
                {{ t('whatsappNutrition.feed.textPlaceholder') }}
              </div>
            </div>
            <div class="flex-1 space-y-2">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm font-semibold text-slate-900">{{ item.nutrition?.foodName ?? t('whatsappNutrition.feed.unknownMeal') }}</p>
                  <p class="text-xs text-slate-500">
                    {{ t('whatsappNutrition.feed.source', { phone: item.fromPhone, date: formatDate(item.receivedAt) }) }}
                  </p>
                </div>
                <span v-if="item.nutrition?.calories" class="text-sm font-semibold text-primary-600">
                  {{ formatNumber(item.nutrition.calories) }} kcal
                </span>
              </div>
              <p v-if="item.textContent" class="text-sm text-slate-600">{{ item.textContent }}</p>
              <div v-if="item.nutrition" class="grid gap-2 sm:grid-cols-3">
                <div class="nutrition-chip">
                  {{ t('whatsappNutrition.feed.macros.protein', { amount: formatMacro(item.nutrition.protein) }) }}
                </div>
                <div class="nutrition-chip">
                  {{ t('whatsappNutrition.feed.macros.carbs', { amount: formatMacro(item.nutrition.carbs) }) }}
                </div>
                <div class="nutrition-chip">
                  {{ t('whatsappNutrition.feed.macros.fat', { amount: formatMacro(item.nutrition.fat) }) }}
                </div>
              </div>
              <p v-if="item.nutrition?.summary" class="text-xs text-slate-500">{{ item.nutrition.summary }}</p>
            </div>
          </li>
        </ul>
      </section>

      <section class="flex flex-col gap-4 rounded-2xl bg-white p-6 shadow-sm">
        <header>
          <h3 class="text-lg font-semibold text-slate-900">{{ t('whatsappNutrition.dashboard.title') }}</h3>
          <p class="text-xs text-slate-500">{{ t('whatsappNutrition.dashboard.description') }}</p>
        </header>

        <div v-if="loading" class="flex h-64 items-center justify-center text-sm text-slate-500">
          {{ t('whatsappNutrition.dashboard.loading') }}
        </div>
        <div v-else class="flex flex-col gap-4">
          <div class="grid gap-4 sm:grid-cols-2">
            <StatCard
              :label="t('whatsappNutrition.dashboard.metrics.calories')"
              :value="`${formatNumber(dashboard.totalCalories)} kcal`"
            />
            <StatCard
              :label="t('whatsappNutrition.dashboard.metrics.meals')"
              :value="formatNumber(dashboard.mealsAnalyzed)"
            />
          </div>

          <section class="rounded-xl border border-slate-200 p-4">
            <h4 class="text-sm font-semibold text-slate-900">{{ t('whatsappNutrition.dashboard.macros.title') }}</h4>
            <div class="mt-3 space-y-2">
              <MacroBar
                :label="t('whatsappNutrition.dashboard.macros.protein')"
                color="bg-emerald-500"
                :percentage="macroPercentages.protein"
                :amount="formatMacro(dashboard.totalProtein, true)"
              />
              <MacroBar
                :label="t('whatsappNutrition.dashboard.macros.carbs')"
                color="bg-amber-500"
                :percentage="macroPercentages.carbs"
                :amount="formatMacro(dashboard.totalCarbs, true)"
              />
              <MacroBar
                :label="t('whatsappNutrition.dashboard.macros.fat')"
                color="bg-rose-500"
                :percentage="macroPercentages.fat"
                :amount="formatMacro(dashboard.totalFat, true)"
              />
            </div>
          </section>

          <section class="rounded-xl border border-slate-200 p-4">
            <h4 class="text-sm font-semibold text-slate-900">{{ t('whatsappNutrition.dashboard.categories.title') }}</h4>
            <ul class="mt-3 space-y-2">
              <li v-for="category in categoryBreakdown" :key="category.name" class="flex items-center justify-between text-sm">
                <span class="font-medium text-slate-700">{{ category.name }}</span>
                <span class="text-slate-500">{{ formatNumber(category.calories) }} kcal</span>
              </li>
              <li v-if="!categoryBreakdown.length" class="text-xs text-slate-500">{{ t('whatsappNutrition.dashboard.categories.empty') }}</li>
            </ul>
          </section>

          <section class="rounded-xl border border-slate-200 p-4">
            <h4 class="text-sm font-semibold text-slate-900">{{ t('whatsappNutrition.dashboard.history.title') }}</h4>
            <ul class="mt-3 space-y-3">
              <li v-for="item in history" :key="item.messageId" class="space-y-1 border-b border-slate-100 pb-3 last:border-b-0 last:pb-0">
                <div class="flex items-center justify-between text-sm">
                  <span class="font-semibold text-slate-800">{{ item.foodName ?? t('whatsappNutrition.dashboard.history.defaultMeal') }}</span>
                  <span class="text-primary-600">{{ formatNumber(item.calories) }} kcal</span>
                </div>
                <div class="text-xs text-slate-500">
                  {{ t('whatsappNutrition.dashboard.history.meta', {
                    date: formatDate(item.analyzedAt),
                    category: item.primaryCategory ?? t('whatsappNutrition.dashboard.history.uncategorised'),
                  }) }}
                </div>
                <p v-if="item.summary" class="text-xs text-slate-500">{{ item.summary }}</p>
              </li>
              <li v-if="!history.length" class="text-xs text-slate-500">{{ t('whatsappNutrition.dashboard.history.empty') }}</li>
            </ul>
          </section>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { fetchNutritionDashboard, fetchWhatsAppMessages } from '@/services/whatsapp';
import StatCard from '@/views/whatsapp/components/StatCard.vue';
import MacroBar from '@/views/whatsapp/components/MacroBar.vue';
import { useNotificationStore } from '@/stores/notifications';
import { useAuthStore } from '@/stores/auth';

const { t, locale } = useI18n();
const notifications = useNotificationStore();
const auth = useAuthStore();

const loading = ref(true);
const feed = ref([]);
const dashboard = ref({
  totalCalories: 0,
  totalProtein: 0,
  totalCarbs: 0,
  totalFat: 0,
  mealsAnalyzed: 0,
  categoryCalories: {},
  history: [],
});

const macroTotals = computed(() => ({
  protein: dashboard.value.totalProtein,
  carbs: dashboard.value.totalCarbs,
  fat: dashboard.value.totalFat,
}));

const macroPercentages = computed(() => {
  const totals = macroTotals.value;
  const sum = totals.protein + totals.carbs + totals.fat;
  if (!sum) {
    return { protein: 0, carbs: 0, fat: 0 };
  }
  return {
    protein: Math.round((totals.protein / sum) * 100),
    carbs: Math.round((totals.carbs / sum) * 100),
    fat: Math.round((totals.fat / sum) * 100),
  };
});

const categoryBreakdown = computed(() => {
  const entries = Object.entries(dashboard.value.categoryCalories ?? {});
  return entries
    .map(([name, calories]) => ({ name, calories }))
    .sort((a, b) => b.calories - a.calories);
});

const history = computed(() => dashboard.value.history ?? []);

const currentLocaleTag = () => (locale.value === 'pt' ? 'pt-BR' : 'en-US');

const numberFormatter = computed(() => new Intl.NumberFormat(currentLocaleTag(), { maximumFractionDigits: 0 }));
const macroFormatter = computed(() => new Intl.NumberFormat(currentLocaleTag(), { maximumFractionDigits: 1 }));

const formatNumber = (value) => numberFormatter.value.format(value ?? 0);
const formatMacro = (value, includeUnit = false) => {
  const formatted = macroFormatter.value.format(value ?? 0);
  return includeUnit ? `${formatted} g` : formatted;
};

const formatDate = (value) => {
  if (!value) return t('whatsappNutrition.common.unknownDate');
  try {
    return new Intl.DateTimeFormat(currentLocaleTag(), {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value));
  } catch (error) {
    return value;
  }
};

const loadData = async () => {
  loading.value = true;
  try {
    const params = auth.user?.id ? { userId: auth.user.id } : {};
    const [messagesResponse, dashboardResponse] = await Promise.all([
      fetchWhatsAppMessages(params),
      fetchNutritionDashboard(params),
    ]);
    feed.value = messagesResponse.data ?? [];
    dashboard.value = { ...dashboard.value, ...(dashboardResponse.data ?? {}) };
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('whatsappNutrition.notifications.title'),
      message: error.response?.data?.message ?? t('whatsappNutrition.notifications.message'),
    });
  } finally {
    loading.value = false;
  }
};

const refreshData = () => {
  void loadData();
};

onMounted(() => {
  void loadData();
});

watch(
  () => auth.user?.id,
  () => {
    void loadData();
  },
);
</script>

<style scoped>
.nutrition-chip {
  @apply rounded-lg bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600;
}
</style>
