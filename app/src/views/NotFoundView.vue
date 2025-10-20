<template>
  <div class="flex min-h-screen flex-col items-center justify-center bg-slate-100 px-6 py-16 text-center">
    <AppLogo />
    <p class="mt-8 text-6xl font-bold text-primary-500">404</p>
    <h1 class="mt-4 text-2xl font-semibold text-slate-900">We couldn't find that page.</h1>
    <p class="mt-2 max-w-md text-sm text-slate-500">
      The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.
    </p>
    <RouterLink :to="{ name: 'dashboard-nutrition' }" class="btn-primary mt-8">Return to dashboard</RouterLink>
  </div>
</template>

<script setup>
import { RouterLink } from 'vue-router';
import AppLogo from '@/components/AppLogo.vue';
</script>