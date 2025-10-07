import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import i18n from './plugins/i18n';
import { usePreferencesStore } from '@/stores/preferences';
import './index.css';

const pinia = createPinia();

const app = createApp(App);
app.use(pinia);
app.use(i18n);
app.use(router);

const preferences = usePreferencesStore(pinia);
preferences.initialize();

app.mount('#app');