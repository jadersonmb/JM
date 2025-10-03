<template>
  <RouterView />
  <NotificationToaster />
</template>

<script setup>
import { RouterView } from 'vue-router';
import NotificationToaster from '@/components/NotificationToaster.vue';
</script>