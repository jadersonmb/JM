<template>
  <div class="space-y-8">
    <section class="rounded-3xl bg-white p-6 shadow-sm">
      <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <h1 class="text-2xl font-semibold text-slate-900">{{ t('photoEvolution.title') }}</h1>
          <p class="mt-1 max-w-2xl text-sm text-slate-500">{{ t('photoEvolution.subtitle') }}</p>
        </div>
        <div class="flex items-center gap-3 text-sm text-slate-500">
          <PhotoIcon class="h-10 w-10 text-primary-500" />
          <div class="hidden md:block">
            <p class="font-semibold text-slate-700">{{ totalEntriesLabel }}</p>
            <p v-if="filterBodyPart" class="text-xs text-slate-400">{{ bodyPartLabel(filterBodyPart) }}</p>
          </div>
        </div>
      </div>

      <div class="mt-6 grid gap-6 md:grid-cols-2">
        <div v-if="isAdmin" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-600">{{ t('photoEvolution.filters.searchOwner') }}</label>
            <div class="relative mt-1">
              <input
                v-model="ownerSearch"
                type="text"
                :placeholder="t('photoEvolution.filters.searchOwner')"
                class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
              />
              <ArrowPathIcon
                v-if="ownersLoading"
                class="absolute right-3 top-1/2 h-5 w-5 -translate-y-1/2 animate-spin text-primary-500"
              />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-600">{{ t('photoEvolution.filters.user') }}</label>
            <select
              v-model="selectedUserId"
              class="mt-1 w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
            >
              <option :value="null">{{ t('photoEvolution.filters.noUserSelected') }}</option>
              <option
                v-for="owner in owners"
                :key="owner.id"
                :value="owner.id"
              >
                {{ owner.displayName }}
                <span v-if="owner.email" class="text-slate-400">— {{ owner.email }}</span>
              </option>
            </select>
            <p v-if="!owners.length && !ownersLoading" class="mt-1 text-xs text-slate-400">{{ t('photoEvolution.filters.noOwners') }}</p>
          </div>
        </div>

        <div v-else class="space-y-2">
          <p class="text-sm font-medium text-slate-600">{{ t('photoEvolution.filters.user') }}</p>
          <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-600">
            <p class="font-semibold text-slate-700">{{ auth.user?.name }}</p>
            <p class="text-xs text-slate-400">{{ auth.user?.email }}</p>
          </div>
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-600">{{ t('photoEvolution.filters.bodyPart') }}</label>
            <select
              v-model="filterBodyPart"
              class="mt-1 w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
            >
              <option :value="null">{{ t('photoEvolution.filters.allBodyParts') }}</option>
              <option v-for="option in bodyPartOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>
          <div class="flex items-center gap-3">
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-2xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-600 transition hover:border-primary-200 hover:text-primary-600"
              @click="loadEntries"
            >
              <ArrowPathIcon class="h-4 w-4" />
              {{ t('photoEvolution.actions.refresh') }}
            </button>
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-2xl border border-transparent bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-600 transition hover:bg-slate-200"
              @click="clearFilters"
            >
              <XMarkIcon class="h-4 w-4" />
              {{ t('photoEvolution.actions.clearFilters') }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <section class="rounded-3xl bg-white p-6 shadow-sm">
      <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <h2 class="text-xl font-semibold text-slate-900">
            {{ editingEntry ? t('photoEvolution.form.editTitle') : t('photoEvolution.form.title') }}
          </h2>
          <p class="text-sm text-slate-500">
            {{ editingEntry ? t('photoEvolution.form.editHelper') : t('photoEvolution.form.helper') }}
          </p>
        </div>
        <div class="flex items-center gap-3">
          <button
            v-if="editingEntry"
            type="button"
            class="inline-flex items-center gap-2 rounded-2xl border border-transparent bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-600 transition hover:bg-slate-200"
            @click="resetEditing"
          >
            <XMarkIcon class="h-4 w-4" />
            {{ t('photoEvolution.form.actions.cancel') }}
          </button>
        </div>
      </div>

      <template v-if="editingEntry">
        <form class="mt-6 space-y-6" @submit.prevent="saveEditedEntry">
          <div class="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
            <div>
              <label class="block text-sm font-medium text-slate-600">{{ t('photoEvolution.form.fields.capturedAt.label') }}</label>
              <input
                v-model="editingEntry.capturedAt"
                type="date"
                class="mt-1 w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-600">{{ t('photoEvolution.form.fields.bodyPart.label') }}</label>
              <select
                v-model="editingEntry.bodyPart"
                class="mt-1 w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
              >
                <option v-for="option in bodyPartOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </div>
            <div v-for="metric in numericFields" :key="metric.key">
              <label class="block text-sm font-medium text-slate-600">
                {{ metric.label }}
                <span v-if="metric.unit" class="font-normal text-slate-400">({{ metric.unit }})</span>
              </label>
              <input
                v-model="editingEntry[metric.key]"
                type="number"
                step="0.01"
                class="mt-1 w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
              />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-slate-600">{{ t('photoEvolution.form.fields.notes.label') }}</label>
            <textarea
              v-model="editingEntry.notes"
              rows="3"
              class="mt-1 w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
            ></textarea>
          </div>

          <div class="grid gap-6 md:grid-cols-2">
            <div>
              <label class="block text-sm font-medium text-slate-600">{{ t('photoEvolution.form.fields.image.label') }}</label>
              <div class="mt-1 flex items-center gap-4">
                <div
                  class="flex h-32 w-32 items-center justify-center overflow-hidden rounded-2xl border border-dashed border-slate-300 bg-slate-50"
                >
                  <img
                    v-if="editingEntry.imagePreview"
                    :src="editingEntry.imagePreview"
                    alt="preview"
                    class="h-full w-full object-cover"
                  />
                  <PhotoIcon v-else class="h-12 w-12 text-slate-300" />
                </div>
                <div class="space-y-2 text-sm text-slate-500">
                  <p>{{ t('photoEvolution.form.fields.image.helper') }}</p>
                  <label
                    class="inline-flex cursor-pointer items-center gap-2 rounded-2xl border border-primary-200 bg-primary-50 px-4 py-2 font-semibold text-primary-600 transition hover:bg-primary-100"
                  >
                    <PhotoIcon class="h-4 w-4" />
                    {{ editingEntry.imagePreview ? t('photoEvolution.form.fields.image.change') : t('photoEvolution.form.fields.image.select') }}
                    <input type="file" accept="image/*" class="hidden" @change="handleEditFileChange" />
                  </label>
                </div>
              </div>
            </div>
            <div class="flex items-end">
              <button
                type="submit"
                :disabled="saving"
                class="inline-flex w-full items-center justify-center gap-2 rounded-2xl border border-transparent bg-primary-600 px-4 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-primary-500 disabled:cursor-not-allowed disabled:opacity-60"
              >
                <ArrowPathIcon v-if="saving" class="h-5 w-5 animate-spin" />
                <PlusIcon v-else class="h-5 w-5" />
                {{ t('photoEvolution.form.actions.update') }}
              </button>
            </div>
          </div>
        </form>
      </template>
      <template v-else>
        <form class="mt-6 space-y-6" @submit.prevent="saveDraftEntries">
          <div class="space-y-6">
            <div
              v-for="(entryForm, index) in draftEntries"
              :key="entryForm.uid"
              class="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm"
            >
              <div class="flex items-center justify-between">
                <h3 class="text-lg font-semibold text-slate-800">
                  {{ t('photoEvolution.form.batchEntryLabel', { index: index + 1 }) }}
                </h3>
                <button
                  v-if="draftEntries.length > 1"
                  type="button"
                  class="rounded-full border border-slate-200 p-2 text-slate-400 transition hover:border-red-200 hover:text-red-500"
                  @click="removeDraftEntry(index)"
                >
                  <TrashIcon class="h-4 w-4" />
                </button>
              </div>

              <div class="mt-4 grid gap-6 md:grid-cols-2 xl:grid-cols-3">
                <div>
                  <label class="block text-sm font-medium text-slate-600">{{ t('photoEvolution.form.fields.capturedAt.label') }}</label>
                  <input
                    v-model="entryForm.capturedAt"
                    type="date"
                    class="mt-1 w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-slate-600">{{ t('photoEvolution.form.fields.bodyPart.label') }}</label>
                  <select
                    v-model="entryForm.bodyPart"
                    class="mt-1 w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
                  >
                    <option v-for="option in bodyPartOptions" :key="option.value" :value="option.value">
                      {{ option.label }}
                    </option>
                  </select>
                </div>
                <div v-for="metric in numericFields" :key="metric.key">
                  <label class="block text-sm font-medium text-slate-600">
                    {{ metric.label }}
                    <span v-if="metric.unit" class="font-normal text-slate-400">({{ metric.unit }})</span>
                  </label>
                  <input
                    v-model="entryForm[metric.key]"
                    type="number"
                    step="0.01"
                    class="mt-1 w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
                  />
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-slate-600">{{ t('photoEvolution.form.fields.notes.label') }}</label>
                <textarea
                  v-model="entryForm.notes"
                  rows="3"
                  class="mt-1 w-full rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 shadow-sm focus:border-primary-300 focus:outline-none focus:ring-2 focus:ring-primary-100"
                ></textarea>
              </div>

              <div class="grid gap-6 md:grid-cols-[auto,1fr] md:items-center">
                <div
                  class="flex h-32 w-32 items-center justify-center overflow-hidden rounded-2xl border border-dashed border-slate-300 bg-slate-50"
                >
                  <img
                    v-if="entryForm.imagePreview"
                    :src="entryForm.imagePreview"
                    alt="preview"
                    class="h-full w-full object-cover"
                  />
                  <PhotoIcon v-else class="h-12 w-12 text-slate-300" />
                </div>
                <div class="space-y-2 text-sm text-slate-500">
                  <p>{{ t('photoEvolution.form.fields.image.batchHelper') }}</p>
                  <label
                    class="inline-flex cursor-pointer items-center gap-2 rounded-2xl border border-primary-200 bg-primary-50 px-4 py-2 font-semibold text-primary-600 transition hover:bg-primary-100"
                  >
                    <PhotoIcon class="h-4 w-4" />
                    {{ entryForm.imagePreview ? t('photoEvolution.form.fields.image.change') : t('photoEvolution.form.fields.image.selectMultiple') }}
                    <input
                      type="file"
                      accept="image/*"
                      multiple
                      class="hidden"
                      @change="(event) => handleDraftFileChange(event, index)"
                    />
                  </label>
                </div>
              </div>
            </div>
          </div>

          <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-2xl border border-dashed border-primary-200 bg-white px-4 py-2 text-sm font-semibold text-primary-600 transition hover:border-primary-300"
              @click="addDraftEntry"
            >
              <PlusIcon class="h-4 w-4" />
              {{ t('photoEvolution.form.actions.addPhoto') }}
            </button>
            <button
              type="submit"
              :disabled="saving"
              class="inline-flex items-center justify-center gap-2 rounded-2xl border border-transparent bg-primary-600 px-4 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-primary-500 disabled:cursor-not-allowed disabled:opacity-60"
            >
              <ArrowPathIcon v-if="saving" class="h-5 w-5 animate-spin" />
              <PlusIcon v-else class="h-5 w-5" />
              {{ t('photoEvolution.form.actions.saveBatch') }}
            </button>
          </div>
        </form>
      </template>
    </section>

    <section v-if="comparisonEntries.length" class="rounded-3xl border border-primary-100 bg-primary-50/60 p-6 shadow-sm">
      <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h2 class="text-lg font-semibold text-primary-700">{{ t('photoEvolution.comparison.title') }}</h2>
          <p class="text-sm text-primary-600">{{ t('photoEvolution.comparison.subtitle') }}</p>
          <p class="mt-2 text-xs font-semibold uppercase tracking-wide text-primary-500">
            {{ t('photoEvolution.comparison.selectedCount', { count: comparisonIds.length }) }}
          </p>
        </div>
        <div class="flex flex-col gap-2 sm:flex-row sm:items-center">
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-2xl border border-transparent bg-primary-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-primary-500 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="comparisonIds.length < 2"
            @click="openComparison"
          >
            <ArrowsPointingOutIcon class="h-4 w-4" />
            {{ t('photoEvolution.comparison.open') }}
          </button>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-2xl border border-transparent bg-primary-100 px-4 py-2 text-sm font-semibold text-primary-700 transition hover:bg-primary-200"
            @click="clearComparison"
          >
            <XMarkIcon class="h-4 w-4" />
            {{ t('photoEvolution.comparison.reset') }}
          </button>
        </div>
      </div>

      <div class="mt-4 grid gap-4 sm:grid-cols-2">
        <article
          v-for="(entry, index) in comparisonEntries"
          :key="entry.id"
          class="rounded-2xl bg-white p-4 shadow-sm"
        >
          <div class="flex items-start justify-between gap-3">
            <div>
              <p class="text-xs font-semibold uppercase tracking-wide text-slate-400">
                {{ index === 0 ? t('photoEvolution.comparison.first') : t('photoEvolution.comparison.second') }}
              </p>
              <p class="mt-1 text-base font-semibold text-slate-800">
                {{ formatDate(entry.capturedAt || entry.createdAt) }}
              </p>
              <p class="text-xs text-slate-400">{{ bodyPartLabel(entry.bodyPart) }}</p>
            </div>
            <button
              type="button"
              class="rounded-full border border-slate-200 p-2 text-slate-400 transition hover:border-primary-200 hover:text-primary-600"
              @click="toggleComparison(entry)"
            >
              <XMarkIcon class="h-4 w-4" />
            </button>
          </div>
          <div class="mt-4 overflow-hidden rounded-xl bg-slate-50">
            <img
              v-if="entry.imageUrl"
              :src="entry.imageUrl"
              alt="comparison preview"
              class="h-40 w-full object-cover"
            />
            <div v-else class="flex h-40 items-center justify-center text-slate-400">
              <PhotoIcon class="h-8 w-8" />
            </div>
          </div>
        </article>
      </div>

      <p v-if="comparisonIds.length < 2" class="mt-4 text-sm text-primary-600">
        {{ t('photoEvolution.comparison.missingSelection') }}
      </p>
    </section>

    <section class="space-y-6">
      <div v-if="loadingEntries" class="flex items-center justify-center rounded-3xl bg-white p-12 shadow-sm">
        <ArrowPathIcon class="h-8 w-8 animate-spin text-primary-500" />
      </div>
      <template v-else>
        <div v-if="!entries.length" class="rounded-3xl bg-white p-12 text-center shadow-sm">
          <p class="text-lg font-semibold text-slate-700">{{ t('photoEvolution.empty.title') }}</p>
          <p class="mt-2 text-sm text-slate-500">{{ t('photoEvolution.empty.subtitle') }}</p>
        </div>
        <div v-else class="space-y-8">
          <div
            v-for="group in groupedEntries"
            :key="group.bodyPart"
            class="rounded-3xl bg-white p-6 shadow-sm"
          >
            <div class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
              <div>
                <h3 class="text-lg font-semibold text-slate-900">
                  {{ t('photoEvolution.table.sectionTitle', { part: group.label }) }}
                </h3>
                <p class="text-xs text-slate-400">
                  {{ t('photoEvolution.table.count', { count: group.items.length }) }}
                </p>
              </div>
            </div>

            <div class="mt-4 grid gap-6 lg:grid-cols-2 xl:grid-cols-3">
              <article
                v-for="entry in group.items"
                :key="entry.id"
                class="flex flex-col rounded-2xl border border-slate-100 bg-white p-4 shadow-sm transition hover:-translate-y-1 hover:shadow-md"
              >
                <div class="flex items-start justify-between gap-4">
                  <div>
                    <p class="text-sm font-semibold text-slate-800">
                      {{ formatDate(entry.capturedAt || entry.createdAt) }}
                    </p>
                    <p class="text-xs text-slate-400">
                      {{ entry.userDisplayName }}
                    </p>
                  </div>
                  <div class="flex gap-2">
                    <button
                      type="button"
                      class="rounded-full border border-slate-200 p-2 text-slate-400 transition hover:border-primary-200 hover:text-primary-600"
                      @click="startEdit(entry)"
                    >
                      <PencilSquareIcon class="h-4 w-4" />
                    </button>
                    <button
                      type="button"
                      class="rounded-full border border-slate-200 p-2 text-slate-400 transition hover:border-red-200 hover:text-red-500"
                      @click="deleteEntry(entry)"
                    >
                      <TrashIcon class="h-4 w-4" />
                    </button>
                    <button
                      type="button"
                      class="rounded-full border border-slate-200 p-2 text-slate-400 transition hover:border-emerald-200 hover:text-emerald-500"
                      @click="toggleComparison(entry)"
                    >
                      <ArrowsRightLeftIcon class="h-4 w-4" />
                    </button>
                  </div>
                </div>

                <div class="mt-4 overflow-hidden rounded-xl bg-slate-50">
                  <img
                    v-if="entry.imageUrl"
                    :src="entry.imageUrl"
                    alt="evolution"
                    class="h-56 w-full object-cover"
                  />
                  <div v-else class="flex h-56 items-center justify-center text-slate-400">
                    <PhotoIcon class="h-10 w-10" />
                  </div>
                </div>

                <dl class="mt-4 grid grid-cols-2 gap-3 text-xs text-slate-600">
                  <template v-for="metric in cardMetrics(entry)" :key="metric.key">
                    <div class="rounded-xl bg-slate-50 px-3 py-2">
                      <dt class="text-[10px] uppercase tracking-wide text-slate-400">{{ metric.label }}</dt>
                      <dd class="mt-1 text-sm font-semibold text-slate-700">
                        {{ metric.value }}
                        <span v-if="metric.unit" class="text-[10px] font-normal text-slate-400">{{ metric.unit }}</span>
                      </dd>
                    </div>
                  </template>
                </dl>

                <p v-if="entry.notes" class="mt-3 rounded-xl bg-slate-50 px-3 py-2 text-sm text-slate-600">
                  {{ entry.notes }}
                </p>
              </article>
            </div>
          </div>
        </div>
      </template>
    </section>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notifications';
import photoEvolutionService from '@/services/photoEvolution';
import {
  ArrowsPointingOutIcon,
  ArrowsRightLeftIcon,
  ArrowPathIcon,
  PencilSquareIcon,
  PhotoIcon,
  PlusIcon,
  TrashIcon,
  XMarkIcon,
} from '@heroicons/vue/24/outline';

const { t, locale } = useI18n();
const auth = useAuthStore();
const notifications = useNotificationStore();
const router = useRouter();
const route = useRoute();

const isAdmin = computed(() => (auth.user?.type ?? '').toUpperCase() === 'ADMIN');
const selectedUserId = ref(isAdmin.value ? null : auth.user?.id ?? null);
const owners = ref([]);
const ownerSearch = ref('');
const ownersLoading = ref(false);
let ownerSearchTimer;

const entries = ref([]);
const loadingEntries = ref(false);
const filterBodyPart = ref(null);
const comparisonIds = ref([]);
const comparisonBodyPart = ref(null);

const numberFormatter = computed(() => new Intl.NumberFormat(locale.value, {
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
}));
const dateFormatter = computed(() => new Intl.DateTimeFormat(locale.value, { dateStyle: 'medium' }));

const bodyPartOptions = computed(() => [
  { value: 'FRONT', label: t('photoEvolution.bodyParts.FRONT') },
  { value: 'BACK', label: t('photoEvolution.bodyParts.BACK') },
  { value: 'LEFT_SIDE', label: t('photoEvolution.bodyParts.LEFT_SIDE') },
  { value: 'RIGHT_SIDE', label: t('photoEvolution.bodyParts.RIGHT_SIDE') },
  { value: 'ABDOMEN', label: t('photoEvolution.bodyParts.ABDOMEN') },
  { value: 'ARMS', label: t('photoEvolution.bodyParts.ARMS') },
  { value: 'LEGS', label: t('photoEvolution.bodyParts.LEGS') },
  { value: 'SHOULDERS', label: t('photoEvolution.bodyParts.SHOULDERS') },
  { value: 'FULL_BODY', label: t('photoEvolution.bodyParts.FULL_BODY') },
]);

const numericFields = computed(() => [
  { key: 'weight', label: t('photoEvolution.form.fields.weight.label'), unit: t('photoEvolution.form.fields.weight.unit') },
  { key: 'bodyFatPercentage', label: t('photoEvolution.form.fields.bodyFatPercentage.label'), unit: t('photoEvolution.form.fields.bodyFatPercentage.unit') },
  { key: 'muscleMass', label: t('photoEvolution.form.fields.muscleMass.label'), unit: t('photoEvolution.form.fields.muscleMass.unit') },
  { key: 'visceralFat', label: t('photoEvolution.form.fields.visceralFat.label'), unit: t('photoEvolution.form.fields.visceralFat.unit') },
  { key: 'waistCircumference', label: t('photoEvolution.form.fields.waistCircumference.label'), unit: t('photoEvolution.form.fields.waistCircumference.unit') },
  { key: 'hipCircumference', label: t('photoEvolution.form.fields.hipCircumference.label'), unit: t('photoEvolution.form.fields.hipCircumference.unit') },
  { key: 'chestCircumference', label: t('photoEvolution.form.fields.chestCircumference.label'), unit: t('photoEvolution.form.fields.chestCircumference.unit') },
  { key: 'leftArmCircumference', label: t('photoEvolution.form.fields.leftArmCircumference.label'), unit: t('photoEvolution.form.fields.leftArmCircumference.unit') },
  { key: 'rightArmCircumference', label: t('photoEvolution.form.fields.rightArmCircumference.label'), unit: t('photoEvolution.form.fields.rightArmCircumference.unit') },
  { key: 'leftThighCircumference', label: t('photoEvolution.form.fields.leftThighCircumference.label'), unit: t('photoEvolution.form.fields.leftThighCircumference.unit') },
  { key: 'rightThighCircumference', label: t('photoEvolution.form.fields.rightThighCircumference.label'), unit: t('photoEvolution.form.fields.rightThighCircumference.unit') },
  { key: 'caloricIntake', label: t('photoEvolution.form.fields.caloricIntake.label'), unit: t('photoEvolution.form.fields.caloricIntake.unit') },
  { key: 'proteinIntake', label: t('photoEvolution.form.fields.proteinIntake.label'), unit: t('photoEvolution.form.fields.proteinIntake.unit') },
  { key: 'carbohydrateIntake', label: t('photoEvolution.form.fields.carbohydrateIntake.label'), unit: t('photoEvolution.form.fields.carbohydrateIntake.unit') },
  { key: 'fatIntake', label: t('photoEvolution.form.fields.fatIntake.label'), unit: t('photoEvolution.form.fields.fatIntake.unit') },
]);

const prefillData = ref({});
let draftIdCounter = 0;
const draftEntries = ref([]);
const editingEntry = ref(null);

const saving = ref(false);

const comparisonEntries = computed(() => comparisonIds.value
  .map((id) => entries.value.find((entry) => entry.id === id))
  .filter((entry) => Boolean(entry)));

function generateDraftId() {
  draftIdCounter += 1;
  return `draft-${Date.now()}-${draftIdCounter}`;
}

function formatPrefillValue(value) {
  if (value === null || value === undefined || value === '') {
    return '';
  }
  return String(value);
}

function createDraftEntry() {
  const entry = {
    uid: generateDraftId(),
    capturedAt: '',
    bodyPart: bodyPartOptions.value[0]?.value ?? 'FRONT',
    notes: '',
    imageFile: null,
    imagePreview: '',
    temporaryPreview: false,
  };
  numericFields.value.forEach((metric) => {
    entry[metric.key] = formatPrefillValue(prefillData.value?.[metric.key]);
  });
  return entry;
}

function cleanupEntryPreview(entry) {
  if (entry?.temporaryPreview && entry.imagePreview) {
    URL.revokeObjectURL(entry.imagePreview);
    entry.temporaryPreview = false;
  }
}

function resetDraftEntries() {
  draftEntries.value.forEach((entry) => cleanupEntryPreview(entry));
  draftEntries.value = [createDraftEntry()];
}

function addDraftEntry() {
  draftEntries.value.push(createDraftEntry());
}

function removeDraftEntry(index) {
  if (index < 0 || index >= draftEntries.value.length) {
    return;
  }
  const [removed] = draftEntries.value.splice(index, 1);
  cleanupEntryPreview(removed);
  if (!draftEntries.value.length) {
    resetDraftEntries();
  }
}

function setEntryFile(entry, file) {
  cleanupEntryPreview(entry);
  if (!file) {
    entry.imageFile = null;
    entry.imagePreview = '';
    return;
  }
  entry.imageFile = file;
  entry.imagePreview = URL.createObjectURL(file);
  entry.temporaryPreview = true;
}

function handleDraftFileChange(event, index) {
  const files = Array.from(event?.target?.files ?? []);
  if (!files.length) {
    return;
  }
  const target = draftEntries.value[index];
  if (target) {
    const [first, ...rest] = files;
    setEntryFile(target, first);
    rest.forEach((file) => {
      const extra = createDraftEntry();
      setEntryFile(extra, file);
      draftEntries.value.push(extra);
    });
  }
  if (event?.target) {
    event.target.value = '';
  }
}

function handleEditFileChange(event) {
  const file = event?.target?.files?.[0];
  if (!editingEntry.value) {
    return;
  }
  if (file) {
    setEntryFile(editingEntry.value, file);
  }
  if (event?.target) {
    event.target.value = '';
  }
}

function resetEditing() {
  if (editingEntry.value) {
    cleanupEntryPreview(editingEntry.value);
  }
  editingEntry.value = null;
}

const groupedEntries = computed(() => {
  const groups = new Map();
  entries.value.forEach((entry) => {
    const key = entry.bodyPart ?? 'UNKNOWN';
    if (!groups.has(key)) {
      groups.set(key, []);
    }
    groups.get(key).push(entry);
  });
  return Array.from(groups.entries()).map(([bodyPart, items]) => ({
    bodyPart,
    label: bodyPartLabel(bodyPart),
    items: items.slice().sort((a, b) => compareDates(b, a)),
  }));
});

const totalEntriesLabel = computed(() => {
  if (!entries.value.length) {
    return t('photoEvolution.summary.empty');
  }
  return t('photoEvolution.summary.count', { count: entries.value.length });
});

function compareDates(a, b) {
  const dateA = new Date((a.capturedAt ?? a.createdAt ?? '') + '');
  const dateB = new Date((b.capturedAt ?? b.createdAt ?? '') + '');
  return dateA - dateB;
}

function formatDate(value) {
  if (!value) return '—';
  try {
    const date = value.length === 10 ? new Date(`${value}T00:00:00`) : new Date(value);
    return dateFormatter.value.format(date);
  } catch (error) {
    return value;
  }
}

function formatNumber(value) {
  if (value === null || value === undefined || value === '') {
    return '—';
  }
  const numeric = Number(value);
  if (Number.isNaN(numeric)) {
    return '—';
  }
  return numberFormatter.value.format(numeric);
}

function toNumber(value) {
  if (value === null || value === undefined || value === '') {
    return null;
  }
  const parsed = Number(value);
  return Number.isNaN(parsed) ? null : parsed;
}

function bodyPartLabel(value) {
  if (!value) {
    return t('photoEvolution.bodyParts.UNKNOWN');
  }
  const key = `photoEvolution.bodyParts.${value}`;
  const translation = t(key);
  return translation === key ? value : translation;
}

async function loadOwners(query = '') {
  if (!isAdmin.value) {
    return;
  }
  ownersLoading.value = true;
  try {
    const { data } = await photoEvolutionService.listOwners({ query });
    owners.value = data ?? [];
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('photoEvolution.notifications.errorTitle'),
      message: t('photoEvolution.notifications.ownersLoadFailed'),
    });
  } finally {
    ownersLoading.value = false;
  }
}

async function loadEntries() {
  if (!isAdmin.value && !selectedUserId.value) {
    selectedUserId.value = auth.user?.id ?? null;
  }
  if (isAdmin.value && !selectedUserId.value) {
    entries.value = [];
    return;
  }
  const params = {};
  if (selectedUserId.value) {
    params.userId = selectedUserId.value;
  }
  if (filterBodyPart.value) {
    params.bodyPart = filterBodyPart.value;
  }
  loadingEntries.value = true;
  try {
    const { data } = await photoEvolutionService.list(params);
    entries.value = data ?? [];
    comparisonIds.value = comparisonIds.value.filter((id) => entries.value.some((entry) => entry.id === id));
    if (comparisonIds.value.length === 0) {
      comparisonBodyPart.value = null;
    }
    applyComparisonFromRoute();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('photoEvolution.notifications.errorTitle'),
      message: t('photoEvolution.notifications.loadFailed'),
    });
  } finally {
    loadingEntries.value = false;
  }
}

async function loadPrefill() {
  if (!selectedUserId.value) {
    prefillData.value = {};
    return;
  }
  try {
    const { data } = await photoEvolutionService.prefill({ userId: selectedUserId.value });
    prefillData.value = data ?? {};
  } catch (error) {
    prefillData.value = {};
  }
}

function clearFilters() {
  filterBodyPart.value = null;
  loadEntries();
}

function parsePayloadNumber(value) {
  const numeric = toNumber(value);
  return numeric === null ? null : numeric;
}

function buildCreatePayload(entry) {
  const payload = {
    userId: selectedUserId.value,
    bodyPart: entry.bodyPart,
    capturedAt: entry.capturedAt || null,
    notes: entry.notes || null,
  };
  numericFields.value.forEach((metric) => {
    payload[metric.key] = parsePayloadNumber(entry[metric.key]);
  });
  return payload;
}

function buildUpdatePayload(entry) {
  const payload = {
    bodyPart: entry.bodyPart,
    capturedAt: entry.capturedAt || null,
    notes: entry.notes || null,
  };
  numericFields.value.forEach((metric) => {
    payload[metric.key] = parsePayloadNumber(entry[metric.key]);
  });
  return payload;
}

async function saveDraftEntries() {
  if (!selectedUserId.value) {
    notifications.push({
      type: 'warning',
      title: t('photoEvolution.notifications.warningTitle'),
      message: t('photoEvolution.notifications.formMissingUser'),
    });
    return;
  }
  const validEntries = draftEntries.value.filter((entry) => entry.imageFile);
  if (!validEntries.length) {
    notifications.push({
      type: 'warning',
      title: t('photoEvolution.notifications.warningTitle'),
      message: t('photoEvolution.notifications.invalidImage'),
    });
    return;
  }
  saving.value = true;
  try {
    const formData = new FormData();
    const payload = validEntries.map((entry) => buildCreatePayload(entry));
    formData.append('entries', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
    validEntries.forEach((entry) => {
      formData.append('images', entry.imageFile);
    });
    await photoEvolutionService.create(formData);
    notifications.push({
      type: 'success',
      title: t('photoEvolution.notifications.successTitle'),
      message: validEntries.length > 1
        ? t('photoEvolution.notifications.bulkCreated', { count: validEntries.length })
        : t('photoEvolution.notifications.created'),
    });
    validEntries.forEach((entry) => cleanupEntryPreview(entry));
    await loadEntries();
    resetDraftEntries();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('photoEvolution.notifications.errorTitle'),
      message: t('photoEvolution.notifications.saveFailed'),
    });
  } finally {
    saving.value = false;
  }
}

async function saveEditedEntry() {
  if (!editingEntry.value) {
    return;
  }
  saving.value = true;
  try {
    const formData = new FormData();
    formData.append('payload', new Blob([JSON.stringify(buildUpdatePayload(editingEntry.value))], {
      type: 'application/json',
    }));
    if (editingEntry.value.imageFile) {
      formData.append('image', editingEntry.value.imageFile);
    }
    await photoEvolutionService.update(editingEntry.value.id, formData);
    notifications.push({
      type: 'success',
      title: t('photoEvolution.notifications.successTitle'),
      message: t('photoEvolution.notifications.updated'),
    });
    cleanupEntryPreview(editingEntry.value);
    await loadEntries();
    await loadPrefill();
    resetEditing();
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('photoEvolution.notifications.errorTitle'),
      message: t('photoEvolution.notifications.saveFailed'),
    });
  } finally {
    saving.value = false;
  }
}

function startEdit(entry) {
  resetEditing();
  editingEntry.value = {
    id: entry.id,
    capturedAt: entry.capturedAt ?? '',
    bodyPart: entry.bodyPart ?? bodyPartOptions.value[0]?.value ?? 'FRONT',
    notes: entry.notes ?? '',
    imageFile: null,
    imagePreview: entry.imageUrl ?? '',
    temporaryPreview: false,
  };
  numericFields.value.forEach((metric) => {
    editingEntry.value[metric.key] = formatPrefillValue(entry[metric.key]);
  });
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

async function deleteEntry(entry) {
  const confirmed = window.confirm(t('photoEvolution.notifications.confirmDelete'));
  if (!confirmed) {
    return;
  }
  if (editingEntry.value?.id === entry.id) {
    resetEditing();
  }
  await photoEvolutionService.remove(entry.id);
  notifications.push({
    type: 'success',
    title: t('photoEvolution.notifications.successTitle'),
    message: t('photoEvolution.notifications.deleted'),
  });
  comparisonIds.value = comparisonIds.value.filter((id) => id !== entry.id);
  if (comparisonIds.value.length === 0) {
    comparisonBodyPart.value = null;
  }
  await loadEntries();
}

function toggleComparison(entry) {
  if (!entry?.id) {
    return;
  }
  const ids = [...comparisonIds.value];
  const existingIndex = ids.indexOf(entry.id);

  if (existingIndex >= 0) {
    ids.splice(existingIndex, 1);
    comparisonIds.value = ids;
    if (!ids.length) {
      comparisonBodyPart.value = null;
    }
    return;
  }

  const entryBodyPart = entry.bodyPart ?? null;
  const expectedBodyPart = comparisonBodyPart.value ?? entryBodyPart;

  if (ids.length && expectedBodyPart && expectedBodyPart !== entryBodyPart) {
    notifications.push({
      type: 'warning',
      title: t('photoEvolution.notifications.warningTitle'),
      message: t('photoEvolution.notifications.comparisonMismatch'),
    });
    comparisonIds.value = [entry.id];
    comparisonBodyPart.value = entryBodyPart;
    return;
  }

  if (ids.length >= 2) {
    ids.shift();
  }

  ids.push(entry.id);
  comparisonIds.value = ids;
  comparisonBodyPart.value = entryBodyPart;
}

function clearComparison() {
  comparisonIds.value = [];
  comparisonBodyPart.value = null;
}

function arraysEqual(first, second) {
  if (first.length !== second.length) {
    return false;
  }
  return first.every((value, index) => value === second[index]);
}

function extractComparisonIdsFromRoute() {
  const ids = [];
  const collect = (value) => {
    if (!value) {
      return;
    }
    if (Array.isArray(value)) {
      value.forEach(collect);
      return;
    }
    ids.push(String(value));
  };
  collect(route.query.first);
  collect(route.query.second);
  return [...new Set(ids)].slice(0, 2);
}

function applyComparisonFromRoute() {
  const ids = extractComparisonIdsFromRoute();
  if (!ids.length) {
    if (comparisonIds.value.length) {
      comparisonIds.value = [];
      comparisonBodyPart.value = null;
    }
    return;
  }
  const matched = ids
    .map((id) => entries.value.find((entry) => entry.id === id))
    .filter((entry) => Boolean(entry));

  if (!matched.length) {
    return;
  }

  const firstEntry = matched[0];
  const sameBodyPart = matched.every((entry) => entry.bodyPart === firstEntry.bodyPart);
  const nextIds = sameBodyPart ? matched.map((entry) => entry.id).slice(0, 2) : [firstEntry.id];

  if (!arraysEqual(comparisonIds.value, nextIds)) {
    comparisonIds.value = nextIds;
  }
  comparisonBodyPart.value = firstEntry.bodyPart ?? null;
}

function serializeQuery(query) {
  return Object.keys(query)
    .sort()
    .map((key) => {
      const value = query[key];
      if (Array.isArray(value)) {
        return `${key}=[${value.map(String).join(',')}]`;
      }
      if (value === undefined || value === null) {
        return `${key}=`;
      }
      return `${key}=${value}`;
    })
    .join('&');
}

function buildComparisonQuery(ids = [], baseQuery = {}) {
  const [first, second] = ids;
  const query = { ...baseQuery };
  if (first) {
    query.first = first;
  } else {
    delete query.first;
  }
  if (second) {
    query.second = second;
  } else {
    delete query.second;
  }
  return query;
}

function updateRouteComparisonQuery(ids) {
  if (route.name !== 'photo-evolution') {
    return;
  }
  const base = { ...route.query };
  delete base.first;
  delete base.second;
  const nextQuery = buildComparisonQuery(ids, base);
  const currentSerialized = serializeQuery(route.query);
  const nextSerialized = serializeQuery(nextQuery);
  if (currentSerialized === nextSerialized) {
    return;
  }
  router.replace({ query: nextQuery }).catch(() => {});
}

function openComparison() {
  if (comparisonIds.value.length < 2) {
    notifications.push({
      type: 'warning',
      title: t('photoEvolution.notifications.warningTitle'),
      message: t('photoEvolution.comparison.missingSelection'),
    });
    return;
  }
  router.push({
    name: 'photo-evolution-comparison',
    query: buildComparisonQuery(comparisonIds.value),
  });
}

function comparisonMetrics(entry) {
  return numericFields.value
    .map((metric) => ({
      key: metric.key,
      label: metric.label,
      value: formatNumber(entry[metric.key]),
      unit: metric.unit,
    }))
    .filter((metric) => metric.value !== '—');
}

function cardMetrics(entry) {
  const metrics = comparisonMetrics(entry);
  return metrics.length ? metrics : [{
    key: 'empty',
    label: t('photoEvolution.card.noMetrics'),
    value: '—',
    unit: '',
  }];
}

watch(comparisonIds, (ids) => {
  updateRouteComparisonQuery(ids);
  if (!ids.length) {
    comparisonBodyPart.value = null;
  }
});

watch(
  () => [route.query.first, route.query.second],
  () => {
    if (entries.value.length) {
      applyComparisonFromRoute();
    }
  },
);

watch(() => auth.user?.id, (newId) => {
  if (!isAdmin.value) {
    selectedUserId.value = newId ?? null;
  }
});

watch(ownerSearch, (value) => {
  if (!isAdmin.value) {
    return;
  }
  clearTimeout(ownerSearchTimer);
  ownerSearchTimer = setTimeout(() => loadOwners(value), 300);
});

watch(filterBodyPart, () => {
  loadEntries();
});

watch(selectedUserId, async () => {
  comparisonIds.value = [];
  comparisonBodyPart.value = null;
  resetEditing();
  await loadEntries();
  await loadPrefill();
  resetDraftEntries();
});

onMounted(async () => {
  if (isAdmin.value) {
    loadOwners();
  }
  await loadEntries();
  await loadPrefill();
  resetDraftEntries();
});
</script>
