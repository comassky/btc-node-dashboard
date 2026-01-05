import { createApp } from 'vue';
import { createPinia } from 'pinia';
import { MotionPlugin } from '@vueuse/motion';
import { Icon } from '@iconify/vue';
import App from '@/App.vue';

import './assets/dashboard.css';
import './assets/animations.css';

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(MotionPlugin);
app.component('Icon', Icon);
app.mount('#app');

//registerServiceWorker();
