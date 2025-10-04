<template>
  <div class="flex flex-col gap-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-semibold text-slate-900">Nutrition Assistant</h2>
        <p class="text-sm text-slate-500">Monitor incoming WhatsApp meals and AI-powered nutrition breakdowns.</p>
      </div>
      <button class="btn-secondary" @click="refreshData" :disabled="loading">
        <span v-if="loading">Updating...</span>
        <span v-else>Refresh</span>
      </button>
    </div>

    <div class="grid gap-6 lg:grid-cols-2">
      <section class="rounded-2xl bg-white p-6 shadow-sm">
        <header class="mb-4 flex items-center justify-between">
          <div>
            <h3 class="text-lg font-semibold text-slate-900">WhatsApp Feed</h3>
            <p class="text-xs text-slate-500">Latest images and messages captured from your WhatsApp bot.</p>
          </div>
          <span class="rounded-full bg-primary-50 px-3 py-1 text-xs font-semibold text-primary-600">{{ feed.length }} items</span>
        </header>

        <div v-if="loading" class="flex h-64 items-center justify-center text-sm text-slate-500">Loading feed...</div>
        <div v-else-if="!feed.length" class="flex h-64 items-center justify-center text-sm text-slate-500">No WhatsApp messages processed yet.</div>
        <ul v-else class="space-y-4 overflow-y-auto pr-2" style="max-height: 750px;">
          <li v-for="item in feed" :key="item.id" class="flex gap-4 rounded-xl border border-slate-200 p-4">
            <div class="h-20 w-20 flex-shrink-0 overflow-hidden rounded-xl bg-slate-100">
              <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.nutrition?.foodName ?? 'Food image'" class="h-full w-full object-cover" />
              <div v-else class="flex h-full w-full items-center justify-center text-xs font-semibold text-slate-400">Text</div>
            </div>
            <div class="flex-1 space-y-2">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm font-semibold text-slate-900">{{ item.nutrition?.foodName ?? 'Unknown meal' }}</p>
                  <p class="text-xs text-slate-500">From {{ item.fromPhone }} - {{ formatDate(item.receivedAt) }}</p>
                </div>
                <span v-if="item.nutrition?.calories" class="text-sm font-semibold text-primary-600">{{ item.nutrition.calories.toFixed(0) }} kcal</span>
              </div>
              <p v-if="item.textContent" class="text-sm text-slate-600">{{ item.textContent }}</p>
              <div v-if="item.nutrition" class="grid gap-2 sm:grid-cols-3">
                <div class="nutrition-chip">P {{ item.nutrition.protein.toFixed(1) }} g</div>
                <div class="nutrition-chip">C {{ item.nutrition.carbs.toFixed(1) }} g</div>
                <div class="nutrition-chip">F {{ item.nutrition.fat.toFixed(1) }} g</div>
              </div>
              <p v-if="item.nutrition?.summary" class="text-xs text-slate-500">{{ item.nutrition.summary }}</p>
            </div>
          </li>
        </ul>
      </section>

      <section class="flex flex-col gap-4 rounded-2xl bg-white p-6 shadow-sm">
        <header>
          <h3 class="text-lg font-semibold text-slate-900">Nutrition Dashboard</h3>
          <p class="text-xs text-slate-500">Aggregated insights from the last 20 analysed meals.</p>
        </header>

        <div v-if="loading" class="flex h-64 items-center justify-center text-sm text-slate-500">Calculating insights...</div>
        <div v-else class="flex flex-col gap-4">
          <div class="grid gap-4 sm:grid-cols-2">
            <StatCard label="Total Calories" :value="formatNumber(dashboard.totalCalories) + ' kcal'" />
            <StatCard label="Meals Analysed" :value="dashboard.mealsAnalyzed" />
          </div>

          <section class="rounded-xl border border-slate-200 p-4">
            <h4 class="text-sm font-semibold text-slate-900">Macronutrients</h4>
            <div class="mt-3 space-y-2">
              <MacroBar label="Protein" color="bg-emerald-500" :percentage="macroPercentages.protein" :amount="dashboard.totalProtein" />
              <MacroBar label="Carbs" color="bg-amber-500" :percentage="macroPercentages.carbs" :amount="dashboard.totalCarbs" />
              <MacroBar label="Fat" color="bg-rose-500" :percentage="macroPercentages.fat" :amount="dashboard.totalFat" />
            </div>
          </section>

          <section class="rounded-xl border border-slate-200 p-4">
            <h4 class="text-sm font-semibold text-slate-900">Food Categories</h4>
            <ul class="mt-3 space-y-2">
              <li v-for="category in categoryBreakdown" :key="category.name" class="flex items-center justify-between text-sm">
                <span class="font-medium text-slate-700">{{ category.name }}</span>
                <span class="text-slate-500">{{ category.calories.toFixed(0) }} kcal</span>
              </li>
            </ul>
          </section>

          <section class="rounded-xl border border-slate-200 p-4">
            <h4 class="text-sm font-semibold text-slate-900">Recent History</h4>
            <ul class="mt-3 space-y-3">
              <li v-for="item in history" :key="item.messageId" class="space-y-1 border-b border-slate-100 pb-3 last:border-b-0 last:pb-0">
                <div class="flex items-center justify-between text-sm">
                  <span class="font-semibold text-slate-800">{{ item.foodName ?? 'Meal' }}</span>
                  <span class="text-primary-600">{{ item.calories.toFixed(0) }} kcal</span>
                </div>
                <div class="text-xs text-slate-500">
                  {{ formatDate(item.analyzedAt) }} - {{ item.primaryCategory ?? 'Uncategorised' }}
                </div>
                <p v-if="item.summary" class="text-xs text-slate-500">{{ item.summary }}</p>
              </li>
            </ul>
          </section>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { fetchNutritionDashboard, fetchWhatsAppMessages } from '@/services/whatsapp';
import StatCard from '@/views/whatsapp/components/StatCard.vue';
import MacroBar from '@/views/whatsapp/components/MacroBar.vue';
import { useNotificationStore } from '@/stores/notifications';

const notifications = useNotificationStore();
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

const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      phoneNumber: '351911566336',
    };   
    const { data: messagesRes } =  await fetchWhatsAppMessages(params);
    const [dashboardRes] = await Promise.all([
      fetchNutritionDashboard(),
    ]);
    feed.value = messagesRes?? [];
    dashboard.value = dashboardRes.data ?? dashboard.value;
  } catch (error) {
    notifications.push({
      type: 'error',
      title: 'Unable to load nutrition data',
      message: error.response?.data?.message ?? 'Check your connection and try again.',
    });
  } finally {
    loading.value = false;
  }
};

const refreshData = () => loadData();

const formatNumber = (value) => new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 }).format(value ?? 0);

const formatDate = (value) => {
  if (!value) return 'Unknown';
  try {
    return new Intl.DateTimeFormat('en-US', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value));
  } catch (error) {
    return value;
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.nutrition-chip {
  @apply rounded-lg bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600;
}
</style>
