<template>
  <div class="flex min-h-screen flex-col items-center justify-center bg-slate-900 text-white">
    <div class="rounded-xl bg-slate-800/80 p-10 text-center shadow-2xl">
      <h1 class="text-4xl font-semibold">{{ $t('auth.accessDeniedTitle') }}</h1>
      <p class="mt-4 max-w-lg text-slate-300">{{ $t('auth.accessDeniedMessage') }}</p>
      <RouterLink
        to="/app/dashboard"
        class="mt-8 inline-flex items-center rounded-lg bg-emerald-500 px-6 py-3 font-semibold text-slate-900 hover:bg-emerald-400"
      >
        {{ $t('common.goBackHome') }}
      </RouterLink>
    </div>
  </div>
</template>

<script setup>
import { RouterLink } from 'vue-router';
</script>
