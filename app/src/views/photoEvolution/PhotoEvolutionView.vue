<template>
  <div class="min-h-screen bg-gray-50">
    <div class="mx-auto max-w-7xl space-y-6 px-4 py-6 lg:px-8">
      <header class="space-y-6">
        <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
          <div class="space-y-1">
            <h1 class="text-2xl font-semibold text-gray-800">{{ t('photoEvolution.title') }}</h1>
            <p class="text-sm text-gray-500">{{ t('photoEvolution.subtitle') }}</p>
          </div>
          <div class="flex flex-col items-stretch gap-4 text-sm text-gray-500 md:flex-row md:items-center">
            <div class="flex items-center gap-3">
              <PhotoIcon class="h-10 w-10 text-primary-500" />
              <div>
                <p class="font-semibold text-gray-700">{{ totalEntriesLabel }}</p>
                <p v-if="filterBodyPart" class="text-xs text-gray-400">
                  {{ bodyPartLabel(filterBodyPart) }}
                </p>
              </div>
            </div>
            <div
              v-if="isAdmin"
              class="flex items-center rounded-full border border-gray-200 bg-white p-1 shadow-sm"
            >
              <button
                type="button"
                class="rounded-full px-4 py-1.5 text-sm font-medium transition"
                :class="adminViewMode === 'user' ? 'bg-blue-500 text-white shadow' : 'text-gray-500 hover:text-gray-700'"
                @click="setAdminViewMode('user')"
              >
                {{ t('photoEvolution.filters.userTab') }}
              </button>
              <button
                type="button"
                class="rounded-full px-4 py-1.5 text-sm font-medium transition"
                :class="adminViewMode === 'all' ? 'bg-blue-500 text-white shadow' : 'text-gray-500 hover:text-gray-700'"
                @click="setAdminViewMode('all')"
              >
                {{ t('photoEvolution.filters.allUsersTab') }}
              </button>
            </div>
          </div>
        </div>
        <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div class="flex flex-col gap-4 md:flex-row md:flex-wrap md:items-center">
            <div v-if="showAllUsers" class="w-full md:w-64">
              <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.filters.searchOwner') }}</label>
              <div class="relative mt-1">
                <input
                  v-model="ownerSearch"
                  type="text"
                  :placeholder="t('photoEvolution.filters.searchOwner')"
                  class="w-full rounded-lg border border-gray-200 bg-white px-4 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
                />
                <ArrowPathIcon
                  v-if="ownersLoading"
                  class="absolute right-3 top-1/2 h-5 w-5 -translate-y-1/2 animate-spin text-primary-500"
                />
              </div>
            </div>
            <div v-if="showAllUsers" class="w-full md:w-72">
              <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.filters.user') }}</label>
              <select
                v-model="selectedUserId"
                class="mt-1 w-full rounded-lg border border-gray-200 bg-white px-4 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
              >
                <option :value="null">{{ t('photoEvolution.filters.noUserSelected') }}</option>
                <option
                  v-for="owner in owners"
                  :key="owner.id"
                  :value="owner.id"
                >
                  {{ owner.displayName }}
                  <span v-if="owner.email" class="text-gray-400">— {{ owner.email }}</span>
                </option>
              </select>
              <p v-if="!owners.length && !ownersLoading" class="mt-1 text-xs text-gray-400">
                {{ t('photoEvolution.filters.noOwners') }}
              </p>
            </div>
            <div
              v-else
              class="w-full max-w-xs rounded-xl border border-gray-200 bg-white px-4 py-3 text-sm text-gray-600 shadow-sm"
            >
              <p class="font-semibold text-gray-700">{{ auth.user?.name }}</p>
              <p class="text-xs text-gray-400">{{ auth.user?.email }}</p>
            </div>
          </div>
          <div class="flex flex-wrap items-center gap-3">
            <div>
              <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.filters.bodyPart') }}</label>
              <select
                v-model="filterBodyPart"
                class="mt-1 w-44 rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
              >
                <option :value="null">{{ t('photoEvolution.filters.allBodyParts') }}</option>
                <option
                  v-for="option in bodyPartOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </option>
              </select>
            </div>
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-4 py-2 text-sm font-semibold text-gray-600 transition hover:border-blue-200 hover:text-blue-600"
              @click="loadEntries"
            >
              <ArrowPathIcon class="h-4 w-4" />
              {{ t('photoEvolution.actions.refresh') }}
            </button>
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-lg border border-transparent bg-gray-100 px-4 py-2 text-sm font-semibold text-gray-600 transition hover:bg-gray-200"
              @click="clearFilters"
            >
              <XMarkIcon class="h-4 w-4" />
              {{ t('photoEvolution.actions.clearFilters') }}
            </button>
          </div>
        </div>
      </header>

      <div class="flex items-center gap-2 border-b border-gray-200 pb-2">
        <button
          type="button"
          class="rounded-full px-4 py-2 text-sm font-semibold transition"
          :class="activeTab === 'gallery' ? 'bg-blue-500 text-white shadow' : 'text-gray-500 hover:text-gray-700'"
          @click="activeTab = 'gallery'"
        >
          {{ t('photoEvolution.tabs.gallery') }}
        </button>
        <button
          type="button"
          class="rounded-full px-4 py-2 text-sm font-semibold transition"
          :class="activeTab === 'compare' ? 'bg-blue-500 text-white shadow' : 'text-gray-500 hover:text-gray-700'"
          @click="activeTab = 'compare'"
        >
          {{ t('photoEvolution.tabs.compare') }}
        </button>
      </div>

      <div class="grid grid-cols-1 gap-6 md:grid-cols-[380px_1fr]">
        <aside v-if="activeTab === 'gallery'" class="space-y-4">
          <div
            v-if="editingEntry"
            class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm space-y-4"
          >
            <div class="flex items-start justify-between gap-3">
              <div>
                <h2 class="text-lg font-semibold text-gray-800">{{ t('photoEvolution.form.editTitle') }}</h2>
                <p class="text-sm text-gray-500">{{ t('photoEvolution.form.editHelper') }}</p>
              </div>
              <button
                type="button"
                class="rounded-full border border-gray-200 px-3 py-1.5 text-sm font-medium text-gray-500 transition hover:border-blue-200 hover:text-blue-600"
                @click="resetEditing"
              >
                {{ t('photoEvolution.form.actions.cancel') }}
              </button>
            </div>
            <form class="space-y-4" @submit.prevent="saveEditedEntry">
              <div class="grid grid-cols-1 gap-4">
                <div class="grid grid-cols-1 gap-3">
                  <div>
                    <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.form.fields.capturedAt.label') }}</label>
                    <input
                      v-model="editingEntry.capturedAt"
                      type="date"
                      class="mt-1 w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
                    />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.form.fields.bodyPart.label') }}</label>
                    <select
                      v-model="editingEntry.bodyPart"
                      class="mt-1 w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
                    >
                      <option
                        v-for="option in bodyPartOptions"
                        :key="option.value"
                        :value="option.value"
                      >
                        {{ option.label }}
                      </option>
                    </select>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.form.fields.notes.label') }}</label>
                    <textarea
                      v-model="editingEntry.notes"
                      rows="3"
                      class="mt-1 w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
                    ></textarea>
                  </div>
                </div>
                <div class="grid grid-cols-2 gap-3">
                  <div
                    v-for="metric in highlightedMetrics"
                    :key="`edit-${metric.key}`"
                  >
                    <label class="text-sm font-medium text-gray-600">
                      {{ metric.label }}
                      <span v-if="metric.unit" class="text-xs font-normal text-gray-400">({{ metric.unit }})</span>
                    </label>
                    <input
                      v-model="editingEntry[metric.key]"
                      type="number"
                      step="0.01"
                      class="mt-1 w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
                    />
                  </div>
                </div>
              </div>

              <div class="space-y-3">
                <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.form.fields.image.label') }}</label>
                <div class="flex items-center gap-3">
                  <div class="flex h-28 w-24 items-center justify-center overflow-hidden rounded-lg border border-dashed border-gray-300 bg-gray-50">
                    <img
                      v-if="editingEntry.imagePreview"
                      :src="editingEntry.imagePreview"
                      alt="preview"
                      class="h-full w-full object-cover"
                    />
                    <PhotoIcon v-else class="h-8 w-8 text-gray-300" />
                  </div>
                  <label class="inline-flex cursor-pointer items-center gap-2 rounded-lg border border-blue-200 bg-blue-50 px-4 py-2 text-sm font-semibold text-blue-600 transition hover:bg-blue-100">
                    <PhotoIcon class="h-4 w-4" />
                    {{ editingEntry.imagePreview ? t('photoEvolution.form.fields.image.change') : t('photoEvolution.form.fields.image.select') }}
                    <input type="file" accept="image/*" class="hidden" @change="handleEditFileChange" />
                  </label>
                </div>
              </div>

              <button
                type="submit"
                :disabled="saving"
                class="flex w-full items-center justify-center gap-2 rounded-lg bg-blue-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-600 disabled:cursor-not-allowed disabled:opacity-60"
              >
                <ArrowPathIcon v-if="saving" class="h-5 w-5 animate-spin" />
                <PlusIcon v-else class="h-5 w-5" />
                {{ t('photoEvolution.form.actions.update') }}
              </button>
            </form>
          </div>

          <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm space-y-4">
            <div>
              <h2 class="text-lg font-semibold text-gray-800">{{ t('photoEvolution.form.title') }}</h2>
              <p class="text-sm text-gray-500">{{ t('photoEvolution.form.helper') }}</p>
            </div>
            <form class="space-y-5" @submit.prevent="saveDraftEntries">
              <div
                v-for="(entryForm, index) in draftEntries"
                :key="entryForm.uid"
                class="rounded-lg border border-gray-200 bg-gray-50/80 p-4"
              >
                <div class="flex items-start justify-between gap-3">
                  <div>
                    <p class="text-sm font-semibold text-gray-700">
                      {{ t('photoEvolution.form.batchEntryLabel', { index: index + 1 }) }}
                    </p>
                    <p class="text-xs text-gray-400">
                      {{ t('photoEvolution.gallery.entryHelper') }}
                    </p>
                  </div>
                  <button
                    v-if="draftEntries.length > 1"
                    type="button"
                    class="rounded-full border border-gray-200 p-2 text-gray-400 transition hover:border-red-200 hover:text-red-500"
                    @click="removeDraftEntry(index)"
                  >
                    <TrashIcon class="h-4 w-4" />
                  </button>
                </div>

                <div class="mt-4 space-y-4">
                  <div class="grid grid-cols-1 gap-3">
                    <div>
                      <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.form.fields.capturedAt.label') }}</label>
                      <input
                        v-model="entryForm.capturedAt"
                        type="date"
                        class="mt-1 w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
                      />
                    </div>
                    <div>
                      <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.form.fields.bodyPart.label') }}</label>
                      <select
                        v-model="entryForm.bodyPart"
                        class="mt-1 w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
                      >
                        <option
                          v-for="option in bodyPartOptions"
                          :key="option.value"
                          :value="option.value"
                        >
                          {{ option.label }}
                        </option>
                      </select>
                    </div>
                    <div>
                      <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.form.fields.notes.label') }}</label>
                      <textarea
                        v-model="entryForm.notes"
                        rows="2"
                        class="mt-1 w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
                      ></textarea>
                    </div>
                  </div>

                  <div class="grid grid-cols-2 gap-3">
                    <div
                      v-for="metric in highlightedMetrics"
                      :key="`${entryForm.uid}-${metric.key}`"
                    >
                      <label class="text-sm font-medium text-gray-600">
                        {{ metric.label }}
                        <span v-if="metric.unit" class="text-xs font-normal text-gray-400">({{ metric.unit }})</span>
                      </label>
                      <input
                        v-model="entryForm[metric.key]"
                        type="number"
                        step="0.01"
                        class="mt-1 w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
                      />
                    </div>
                  </div>

                  <div class="space-y-3">
                    <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.form.fields.image.label') }}</label>
                    <div class="flex items-center gap-3">
                      <div class="flex h-24 w-24 items-center justify-center overflow-hidden rounded-lg border border-dashed border-gray-300 bg-white">
                        <img
                          v-if="entryForm.imagePreview"
                          :src="entryForm.imagePreview"
                          alt="preview"
                          class="h-full w-full object-cover"
                        />
                        <PhotoIcon v-else class="h-8 w-8 text-gray-300" />
                      </div>
                      <label class="inline-flex cursor-pointer items-center gap-2 rounded-lg border border-blue-200 bg-blue-50 px-4 py-2 text-sm font-semibold text-blue-600 transition hover:bg-blue-100">
                        <PhotoIcon class="h-4 w-4" />
                        {{ entryForm.imagePreview ? t('photoEvolution.form.fields.image.change') : t('photoEvolution.gallery.uploadLabel') }}
                        <input
                          type="file"
                          accept="image/*"
                          multiple
                          class="hidden"
                          @change="(event) => handleDraftFileChange(event, index)"
                        />
                      </label>
                    </div>
                    <p class="text-xs text-gray-400">{{ t('photoEvolution.gallery.uploadHelper') }}</p>
                  </div>
                </div>
              </div>

              <div class="space-y-3">
                <div class="flex items-center justify-between text-xs text-gray-500">
                  <span>{{ uploadStatus }}</span>
                  <button
                    type="button"
                    class="text-blue-500 hover:text-blue-600"
                    @click="resetDraftEntries"
                  >
                    {{ t('photoEvolution.gallery.clearForm') }}
                  </button>
                </div>
                <div class="flex flex-col gap-3 sm:flex-row">
                  <button
                    type="submit"
                    :disabled="saving"
                    class="flex-1 rounded-lg bg-blue-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-600 disabled:cursor-not-allowed disabled:opacity-60"
                  >
                    {{ t('photoEvolution.gallery.saveEvolution') }}
                  </button>
                  <button
                    type="button"
                    class="flex-1 rounded-lg border border-gray-200 bg-white px-4 py-2 text-sm font-semibold text-gray-600 transition hover:border-blue-200 hover:text-blue-600"
                    @click="addDraftEntry"
                  >
                    {{ t('photoEvolution.gallery.addNew') }}
                  </button>
                </div>
              </div>
            </form>
          </div>
        </aside>

        <aside v-else class="space-y-4">
          <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm space-y-4">
            <div>
              <h2 class="text-lg font-semibold text-gray-800">{{ t('photoEvolution.compare.formTitle') }}</h2>
              <p class="text-sm text-gray-500">{{ t('photoEvolution.compare.formSubtitle') }}</p>
            </div>
            <form class="space-y-4" @submit.prevent="applyComparisonForm">
              <div class="space-y-3">
                <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.form.fields.bodyPart.label') }}</label>
                <select
                  v-model="comparisonForm.bodyPart"
                  class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
                >
                  <option :value="null">{{ t('photoEvolution.compare.selectBodyPart') }}</option>
                  <option
                    v-for="option in comparisonBodyParts"
                    :key="option.value"
                    :value="option.value"
                  >
                    {{ option.label }}
                  </option>
                </select>
              </div>
              <div class="space-y-3">
                <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.compare.angle') }}</label>
                <select
                  v-model="comparisonForm.angle"
                  class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
                >
                  <option :value="null">{{ t('photoEvolution.compare.selectAngle') }}</option>
                  <option
                    v-for="option in comparisonAngles"
                    :key="option.value"
                    :value="option.value"
                  >
                    {{ option.label }}
                  </option>
                </select>
              </div>
              <div class="space-y-3">
                <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.compare.mode') }}</label>
                <select
                  v-model="comparisonForm.mode"
                  class="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
                >
                  <option value="side-by-side">{{ t('photoEvolution.compare.modes.sideBySide') }}</option>
                  <option value="slider">{{ t('photoEvolution.compare.modes.slider') }}</option>
                  <option value="overlay">{{ t('photoEvolution.compare.modes.overlay') }}</option>
                </select>
              </div>
              <div class="grid grid-cols-1 gap-3">
                <div>
                  <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.compare.before') }}</label>
                  <select
                    v-model="comparisonForm.beforeId"
                    class="mt-1 w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
                  >
                    <option :value="null">{{ t('photoEvolution.compare.selectBefore') }}</option>
                    <option
                      v-for="option in comparisonBeforeOptions"
                      :key="option.value"
                      :value="option.value"
                    >
                      {{ option.label }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.compare.after') }}</label>
                  <select
                    v-model="comparisonForm.afterId"
                    class="mt-1 w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
                  >
                    <option :value="null">{{ t('photoEvolution.compare.selectAfter') }}</option>
                    <option
                      v-for="option in comparisonAfterOptions"
                      :key="option.value"
                      :value="option.value"
                    >
                      {{ option.label }}
                    </option>
                  </select>
                </div>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.compare.beforeLabel') }}</label>
                  <input
                    v-model="comparisonForm.beforeLabel"
                    type="text"
                    class="mt-1 w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
                  />
                </div>
                <div>
                  <label class="text-sm font-medium text-gray-600">{{ t('photoEvolution.compare.afterLabel') }}</label>
                  <input
                    v-model="comparisonForm.afterLabel"
                    type="text"
                    class="mt-1 w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 shadow-sm focus:border-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-100"
                  />
                </div>
              </div>
              <div class="flex flex-col gap-3 sm:flex-row">
                <button
                  type="submit"
                  class="flex-1 rounded-lg bg-blue-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-600"
                >
                  {{ t('photoEvolution.compare.update') }}
                </button>
                <button
                  type="button"
                  class="flex-1 rounded-lg border border-gray-200 bg-white px-4 py-2 text-sm font-semibold text-gray-600 transition hover:border-blue-200 hover:text-blue-600"
                  @click="resetComparisonForm"
                >
                  {{ t('photoEvolution.compare.reset') }}
                </button>
              </div>
            </form>
          </div>

          <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm space-y-3">
            <h3 class="text-sm font-semibold text-gray-700">{{ t('photoEvolution.compare.recentComparisons') }}</h3>
            <div class="flex flex-col gap-3">
              <button
                v-for="recent in recentComparisons"
                :key="recent.key"
                type="button"
                class="flex items-center justify-between rounded-lg border border-gray-200 px-3 py-2 text-sm text-gray-600 transition hover:border-blue-200 hover:text-blue-600"
                @click="applyRecentComparison(recent)"
              >
                <span>{{ recent.label }}</span>
                <span class="text-xs text-gray-400">{{ recent.modeLabel }}</span>
              </button>
              <p v-if="!recentComparisons.length" class="text-xs text-gray-400">
                {{ t('photoEvolution.compare.noRecent') }}
              </p>
            </div>
          </div>
        </aside>

        <section v-if="activeTab === 'gallery'" class="space-y-4">
          <div v-if="loadingEntries" class="flex items-center justify-center rounded-xl bg-white p-12 shadow-sm">
            <ArrowPathIcon class="h-8 w-8 animate-spin text-primary-500" />
          </div>
          <div v-else>
            <div v-if="!entries.length" class="rounded-xl bg-white p-12 text-center shadow-sm">
              <p class="text-lg font-semibold text-gray-700">{{ t('photoEvolution.empty.title') }}</p>
              <p class="mt-2 text-sm text-gray-500">{{ t('photoEvolution.empty.subtitle') }}</p>
            </div>
            <div v-else class="space-y-4">
              <div
                v-for="group in timelineGroups"
                :key="group.key"
                class="bg-white rounded-xl border border-gray-200 p-4 shadow-sm space-y-3"
              >
                <div class="flex flex-wrap items-center justify-between gap-2">
                  <div>
                    <h3 class="text-base font-semibold text-gray-800">{{ group.label }}</h3>
                    <p class="text-sm text-gray-500">
                      {{ group.countLabel }}
                    </p>
                  </div>
                  <div class="flex flex-wrap gap-2 text-xs text-gray-500">
                    <span class="rounded-full bg-gray-100 px-3 py-1">{{ t('photoEvolution.gallery.bodyPartChip', { part: group.bodyPartLabel }) }}</span>
                    <span class="rounded-full bg-gray-100 px-3 py-1">{{ t('photoEvolution.gallery.angleChip', { angle: group.angleLabel }) }}</span>
                  </div>
                </div>
                <div class="flex gap-3 overflow-x-auto pb-1">
                  <div
                    v-for="entry in group.items"
                    :key="entry.id"
                    class="group relative h-32 w-24 flex-shrink-0 overflow-hidden rounded-lg border border-transparent bg-gray-100 transition-all duration-300"
                    :class="comparisonIds.includes(entry.id) ? 'ring-2 ring-blue-500 ring-offset-2' : 'hover:border-blue-200 hover:shadow'"
                  >
                    <img
                      v-if="entry.imageUrl"
                      :src="entry.imageUrl"
                      :alt="formatDate(entry.capturedAt || entry.createdAt)"
                      class="h-full w-full object-cover transition duration-300 group-hover:scale-105"
                    />
                    <div v-else class="flex h-full w-full items-center justify-center text-gray-400">
                      <PhotoIcon class="h-6 w-6" />
                    </div>
                    <div class="pointer-events-none absolute inset-0 flex flex-col justify-end bg-gradient-to-t from-black/60 via-black/10 to-transparent p-2 text-xs text-white opacity-0 transition group-hover:opacity-100">
                      <span class="font-semibold">{{ formatDate(entry.capturedAt || entry.createdAt) }}</span>
                      <span v-if="entry.weight" class="text-[10px] text-white/80">{{ t('photoEvolution.gallery.weightChip', { weight: formatNumber(entry.weight) }) }}</span>
                    </div>
                    <div class="absolute inset-0 flex items-center justify-center gap-2 bg-black/60 opacity-0 transition group-hover:opacity-100">
                      <button
                        type="button"
                        class="pointer-events-auto rounded-full bg-white px-3 py-1 text-xs font-semibold text-gray-700 shadow"
                        @click="viewEntry(entry)"
                      >
                        {{ t('photoEvolution.gallery.view') }}
                      </button>
                      <button
                        type="button"
                        class="pointer-events-auto rounded-full bg-blue-500 px-3 py-1 text-xs font-semibold text-white shadow"
                        @click="toggleComparison(entry)"
                      >
                        {{ t('photoEvolution.gallery.compare') }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section v-else class="space-y-4">
          <div class="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
            <div class="flex flex-wrap gap-2 text-sm text-gray-600">
              <span class="rounded-full bg-gray-100 px-3 py-1">{{ t('photoEvolution.gallery.bodyPartChip', { part: comparisonSummary.bodyPart }) }}</span>
              <span class="rounded-full bg-gray-100 px-3 py-1">{{ t('photoEvolution.gallery.angleChip', { angle: comparisonSummary.angle }) }}</span>
              <span class="rounded-full bg-gray-100 px-3 py-1">{{ t('photoEvolution.compare.beforeChip', { date: comparisonSummary.beforeDate }) }}</span>
              <span class="rounded-full bg-gray-100 px-3 py-1">{{ t('photoEvolution.compare.afterChip', { date: comparisonSummary.afterDate }) }}</span>
              <span class="rounded-full bg-gray-100 px-3 py-1">{{ t('photoEvolution.compare.modeChip', { mode: comparisonSummary.modeLabel }) }}</span>
            </div>
            <div class="mt-4 grid grid-cols-1 gap-4 lg:grid-cols-2">
              <div v-for="(entry, index) in comparisonEntries" :key="entry?.id || index" class="relative h-full rounded-lg bg-gray-100">
                <div class="absolute inset-0 flex flex-col justify-between p-3 text-xs">
                  <div class="flex justify-between text-gray-700">
                    <span class="rounded bg-white/80 px-2 py-1">{{ index === 0 ? comparisonForm.beforeLabel : comparisonForm.afterLabel }}</span>
                    <span class="rounded bg-white/80 px-2 py-1" v-if="entry?.weight">
                      {{ formatNumber(entry.weight) }} {{ t('photoEvolution.form.fields.weight.unit') }}
                    </span>
                  </div>
                  <div class="self-end rounded bg-white/80 px-2 py-1 text-gray-600">
                    {{ formatDate(entry?.capturedAt || entry?.createdAt) }}
                  </div>
                </div>
                <img
                  v-if="entry?.imageUrl"
                  :src="entry.imageUrl"
                  class="h-full w-full rounded-lg object-cover"
                  :style="{ transform: `scale(${comparisonZoomScale})` }"
                  alt=""
                />
                <div v-else class="flex h-96 items-center justify-center text-gray-300">
                  <PhotoIcon class="h-12 w-12" />
                </div>
              </div>
            </div>

            <div class="mt-4 space-y-3">
              <input
                type="range"
                min="1"
                max="100"
                v-model="comparisonForm.zoom"
                class="w-full accent-blue-500"
              />
              <div class="flex flex-wrap items-center justify-between gap-3 text-sm text-gray-500">
                <div class="flex gap-3">
                  <button type="button" class="hover:text-gray-700" @click="swapComparison">
                    ⇄ {{ t('photoEvolution.compare.swap') }}
                  </button>
                  <button type="button" class="hover:text-gray-700" @click="duplicateComparison">
                    {{ t('photoEvolution.compare.duplicate') }}
                  </button>
                </div>
                <div class="flex items-center gap-3">
                  <span>• {{ comparisonForm.afterLabel }}</span>
                  <span>• {{ comparisonForm.beforeLabel }}</span>
                  <button
                    type="button"
                    class="rounded-md bg-blue-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-600"
                    @click="exportComparison"
                  >
                    {{ t('photoEvolution.compare.export') }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
            <h3 class="text-sm font-semibold text-gray-700">{{ t('photoEvolution.compare.suggestedPairs') }}</h3>
            <div class="mt-3 flex flex-wrap gap-2">
              <button
                v-for="suggestion in suggestedPairs"
                :key="suggestion.key"
                type="button"
                class="rounded-full border border-gray-200 px-3 py-1 text-xs text-gray-600 transition hover:border-blue-200 hover:text-blue-600"
                @click="applySuggestion(suggestion)"
              >
                {{ suggestion.label }}
              </button>
              <p v-if="!suggestedPairs.length" class="text-xs text-gray-400">
                {{ t('photoEvolution.compare.noSuggestions') }}
              </p>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notifications';
import photoEvolutionService from '@/services/photoEvolution';
import {
  ArrowPathIcon,
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
const adminViewMode = ref(isAdmin.value ? 'user' : 'user');
const activeTab = ref('gallery');
const selectedUserId = ref(auth.user?.id ?? null);
const owners = ref([]);
const ownerSearch = ref('');
const ownersLoading = ref(false);
let ownerSearchTimer;

const showAllUsers = computed(() => isAdmin.value && adminViewMode.value === 'all');

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

const highlightedMetricKeys = [
  'weight',
  'waistCircumference',
  'hipCircumference',
  'chestCircumference',
  'leftArmCircumference',
  'leftThighCircumference',
];

const highlightedMetrics = computed(() => numericFields.value
  .filter((metric) => highlightedMetricKeys.includes(metric.key)));

const prefillData = ref({});
let draftIdCounter = 0;
const draftEntries = ref([]);
const editingEntry = ref(null);

const saving = ref(false);

const uploadStatus = computed(() => {
  const pending = draftEntries.value.filter((entry) => entry.imageFile).length;
  if (!pending) {
    return t('photoEvolution.gallery.uploadIdle');
  }
  if (saving.value) {
    return t('photoEvolution.gallery.uploading', { count: pending });
  }
  return t('photoEvolution.gallery.uploadReady', { count: pending });
});

const comparisonEntries = computed(() => comparisonIds.value
  .map((id) => entries.value.find((entry) => entry.id === id))
  .filter((entry) => Boolean(entry)));

const timelineGroups = computed(() => {
  const groups = new Map();
  entries.value.forEach((entry) => {
    const key = timelineKey(entry);
    if (!groups.has(key)) {
      groups.set(key, []);
    }
    groups.get(key).push(entry);
  });
  return Array.from(groups.entries()).map(([key, items]) => {
    const sorted = items.slice().sort((a, b) => compareDates(b, a));
    const primary = sorted[0] ?? {};
    const bodyPart = primary.bodyPart ?? null;
    const angle = extractAngle(primary);
    return {
      key,
      label: key === 'unknown' ? t('photoEvolution.gallery.unknownDate') : formatDate(key),
      countLabel: t('photoEvolution.gallery.countLabel', { count: sorted.length }),
      bodyPartLabel: bodyPartLabel(bodyPart),
      angleLabel: angleLabel(angle),
      items: sorted,
    };
  }).sort((a, b) => compareGroupKeys(b.key, a.key));
});

const beforeLabelDefault = computed(() => t('photoEvolution.compare.beforeDefault'));
const afterLabelDefault = computed(() => t('photoEvolution.compare.afterDefault'));

const comparisonForm = reactive({
  bodyPart: null,
  angle: null,
  mode: 'side-by-side',
  beforeId: null,
  afterId: null,
  beforeLabel: '',
  afterLabel: '',
  zoom: 50,
});

comparisonForm.beforeLabel = beforeLabelDefault.value;
comparisonForm.afterLabel = afterLabelDefault.value;

const recentComparisons = ref([]);

const comparisonBodyParts = computed(() => {
  const map = new Map();
  entries.value.forEach((entry) => {
    if (entry.bodyPart) {
      map.set(entry.bodyPart, bodyPartLabel(entry.bodyPart));
    }
  });
  return Array.from(map.entries()).map(([value, label]) => ({ value, label }));
});

const comparisonAngles = computed(() => {
  const map = new Map();
  entries.value.forEach((entry) => {
    if (comparisonForm.bodyPart && entry.bodyPart !== comparisonForm.bodyPart) {
      return;
    }
    const angle = extractAngle(entry);
    if (angle) {
      map.set(angle, angleLabel(angle));
    }
  });
  return Array.from(map.entries()).map(([value, label]) => ({ value, label }));
});

const comparisonCandidates = computed(() => entries.value
  .filter((entry) => (!comparisonForm.bodyPart || entry.bodyPart === comparisonForm.bodyPart)
    && (!comparisonForm.angle || extractAngle(entry) === comparisonForm.angle))
  .slice()
  .sort((a, b) => compareDates(a, b)));

const comparisonBeforeOptions = computed(() => comparisonCandidates.value.map((entry) => ({
  value: entry.id,
  label: buildComparisonOptionLabel(entry),
})));

const comparisonAfterOptions = computed(() => comparisonBeforeOptions.value);

const comparisonModeLabel = computed(() => modeLabel(comparisonForm.mode));

const comparisonSummary = computed(() => {
  const [before, after] = comparisonEntries.value;
  return {
    bodyPart: before ? bodyPartLabel(before.bodyPart) : t('photoEvolution.bodyParts.UNKNOWN'),
    angle: angleLabel(extractAngle(before) ?? extractAngle(after)),
    beforeDate: before ? formatDate(before.capturedAt || before.createdAt) : '—',
    afterDate: after ? formatDate(after.capturedAt || after.createdAt) : '—',
    modeLabel: comparisonModeLabel.value,
  };
});

const comparisonZoomScale = computed(() => 1 + (Number(comparisonForm.zoom || 50) - 50) / 100);

const suggestedPairs = computed(() => {
  const map = new Map();
  entries.value.forEach((entry) => {
    const key = `${entry.bodyPart ?? 'UNKNOWN'}::${extractAngle(entry) ?? 'UNKNOWN'}`;
    if (!map.has(key)) {
      map.set(key, []);
    }
    map.get(key).push(entry);
  });
  const suggestions = [];
  map.forEach((items, key) => {
    if (items.length < 2) {
      return;
    }
    const sorted = items.slice().sort((a, b) => compareDates(a, b));
    const first = sorted[0];
    const last = sorted[sorted.length - 1];
    if (!first || !last || first.id === last.id) {
      return;
    }
    const bodyPart = first.bodyPart ?? last.bodyPart ?? null;
    const angle = extractAngle(first) ?? extractAngle(last);
    suggestions.push({
      key: `${key}-${first.id}-${last.id}`,
      beforeId: first.id,
      afterId: last.id,
      bodyPart,
      angle,
      label: t('photoEvolution.compare.suggestionLabel', {
        bodyPart: bodyPartLabel(bodyPart),
        before: formatDate(first.capturedAt || first.createdAt),
        after: formatDate(last.capturedAt || last.createdAt),
      }),
    });
  });
  return suggestions.slice(0, 6);
});

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

function setAdminViewMode(mode) {
  if (!isAdmin.value) {
    return;
  }
  if (adminViewMode.value === mode) {
    return;
  }
  adminViewMode.value = mode;
  if (mode === 'user') {
    selectedUserId.value = auth.user?.id ?? null;
  } else {
    selectedUserId.value = null;
    owners.value = [];
    ownerSearch.value = '';
    loadOwners();
  }
}

function viewEntry(entry) {
  if (!entry) {
    return;
  }
  startEdit(entry);
  activeTab.value = 'gallery';
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

const totalEntriesLabel = computed(() => {
  if (!entries.value.length) {
    return t('photoEvolution.summary.empty');
  }
  return t('photoEvolution.summary.count', { count: entries.value.length });
});

function timelineKey(entry) {
  const raw = entry?.capturedAt ?? entry?.createdAt ?? '';
  if (!raw) {
    return 'unknown';
  }
  return raw.slice(0, 10);
}

function compareGroupKeys(a, b) {
  if (a === b) {
    return 0;
  }
  if (a === 'unknown') {
    return -1;
  }
  if (b === 'unknown') {
    return 1;
  }
  const dateA = new Date(`${a}T00:00:00`);
  const dateB = new Date(`${b}T00:00:00`);
  return dateA - dateB;
}

function extractAngle(entry) {
  return entry?.angle ?? entry?.position ?? null;
}

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

function angleLabel(value) {
  if (!value) {
    return t('photoEvolution.angles.UNKNOWN');
  }
  const key = `photoEvolution.angles.${value}`;
  const translation = t(key);
  return translation === key ? value : translation;
}

function buildComparisonOptionLabel(entry) {
  const parts = [formatDate(entry.capturedAt || entry.createdAt)];
  if (entry.weight) {
    parts.push(`${formatNumber(entry.weight)} ${t('photoEvolution.form.fields.weight.unit')}`);
  }
  if (entry.userDisplayName) {
    parts.push(entry.userDisplayName);
  }
  return parts.filter(Boolean).join(' • ');
}

function modeLabel(mode) {
  switch (mode) {
    case 'slider':
      return t('photoEvolution.compare.modes.slider');
    case 'overlay':
      return t('photoEvolution.compare.modes.overlay');
    default:
      return t('photoEvolution.compare.modes.sideBySide');
  }
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

function applyComparisonForm() {
  if (!comparisonForm.beforeId || !comparisonForm.afterId) {
    notifications.push({
      type: 'warning',
      title: t('photoEvolution.notifications.warningTitle'),
      message: t('photoEvolution.compare.missingSelection'),
    });
    return;
  }
  if (comparisonForm.beforeId === comparisonForm.afterId) {
    notifications.push({
      type: 'warning',
      title: t('photoEvolution.notifications.warningTitle'),
      message: t('photoEvolution.compare.sameSelection'),
    });
    return;
  }
  comparisonIds.value = [comparisonForm.beforeId, comparisonForm.afterId];
  if (comparisonForm.bodyPart) {
    comparisonBodyPart.value = comparisonForm.bodyPart;
  }
  registerRecentComparison();
}

function resetComparisonForm() {
  comparisonForm.bodyPart = null;
  comparisonForm.angle = null;
  comparisonForm.mode = 'side-by-side';
  comparisonForm.beforeId = null;
  comparisonForm.afterId = null;
  comparisonForm.beforeLabel = beforeLabelDefault.value;
  comparisonForm.afterLabel = afterLabelDefault.value;
  comparisonForm.zoom = 50;
}

function registerRecentComparison() {
  const before = entries.value.find((entry) => entry.id === comparisonForm.beforeId);
  const after = entries.value.find((entry) => entry.id === comparisonForm.afterId);
  if (!before || !after) {
    return;
  }
  const bodyPart = before.bodyPart ?? after.bodyPart ?? null;
  const angle = extractAngle(before) ?? extractAngle(after);
  const label = `${formatDate(before.capturedAt || before.createdAt)} → ${formatDate(after.capturedAt || after.createdAt)}`;
  const item = {
    key: `${before.id}-${after.id}-${comparisonForm.mode}`,
    beforeId: before.id,
    afterId: after.id,
    bodyPart,
    angle,
    mode: comparisonForm.mode,
    label: `${bodyPartLabel(bodyPart)} • ${label}`,
    modeLabel: modeLabel(comparisonForm.mode),
  };
  recentComparisons.value = [item, ...recentComparisons.value.filter((existing) => existing.key !== item.key)].slice(0, 3);
}

function applyRecentComparison(recent) {
  if (!recent) {
    return;
  }
  comparisonForm.bodyPart = recent.bodyPart ?? comparisonForm.bodyPart;
  comparisonForm.angle = recent.angle ?? comparisonForm.angle;
  comparisonForm.mode = recent.mode ?? comparisonForm.mode;
  comparisonForm.beforeId = recent.beforeId;
  comparisonForm.afterId = recent.afterId;
  applyComparisonForm();
}

function swapComparison() {
  const before = comparisonForm.beforeId;
  const after = comparisonForm.afterId;
  comparisonForm.beforeId = after;
  comparisonForm.afterId = before;
  const beforeLabel = comparisonForm.beforeLabel;
  comparisonForm.beforeLabel = comparisonForm.afterLabel;
  comparisonForm.afterLabel = beforeLabel;
  applyComparisonForm();
}

function duplicateComparison() {
  registerRecentComparison();
}

function exportComparison() {
  notifications.push({
    type: 'info',
    title: t('photoEvolution.notifications.successTitle'),
    message: t('photoEvolution.compare.exportMessage'),
  });
}

function applySuggestion(suggestion) {
  if (!suggestion) {
    return;
  }
  comparisonForm.bodyPart = suggestion.bodyPart ?? comparisonForm.bodyPart;
  comparisonForm.angle = suggestion.angle ?? comparisonForm.angle;
  comparisonForm.beforeId = suggestion.beforeId;
  comparisonForm.afterId = suggestion.afterId;
  applyComparisonForm();
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
  if (!isAdmin.value || adminViewMode.value === 'user') {
    selectedUserId.value = newId ?? null;
  }
});

watch(ownerSearch, (value) => {
  if (!showAllUsers.value) {
    return;
  }
  clearTimeout(ownerSearchTimer);
  ownerSearchTimer = setTimeout(() => loadOwners(value), 300);
});

watch(beforeLabelDefault, (value, oldValue) => {
  if (!comparisonForm.beforeLabel || comparisonForm.beforeLabel === oldValue) {
    comparisonForm.beforeLabel = value;
  }
});

watch(afterLabelDefault, (value, oldValue) => {
  if (!comparisonForm.afterLabel || comparisonForm.afterLabel === oldValue) {
    comparisonForm.afterLabel = value;
  }
});

watch(comparisonEntries, (entries) => {
  if (!entries.length) {
    comparisonForm.beforeId = null;
    comparisonForm.afterId = null;
    return;
  }
  const [before, after] = entries;
  if (before) {
    comparisonForm.beforeId = before.id;
    comparisonForm.bodyPart = before.bodyPart ?? comparisonForm.bodyPart;
    comparisonForm.angle = extractAngle(before) ?? comparisonForm.angle;
    if (before.bodyPart) {
      comparisonBodyPart.value = before.bodyPart;
    }
  }
  if (after) {
    comparisonForm.afterId = after.id;
  }
});

watch(() => comparisonForm.bodyPart, () => {
  if (comparisonForm.angle && !comparisonAngles.value.some((option) => option.value === comparisonForm.angle)) {
    comparisonForm.angle = null;
  }
});

watch(filterBodyPart, () => {
  loadEntries();
});

watch(selectedUserId, async () => {
  comparisonIds.value = [];
  comparisonBodyPart.value = null;
  resetEditing();
  resetComparisonForm();
  await loadEntries();
  await loadPrefill();
  resetDraftEntries();
});

onMounted(async () => {
  if (showAllUsers.value) {
    loadOwners();
  }
  await loadEntries();
  await loadPrefill();
  resetDraftEntries();
});
</script>
