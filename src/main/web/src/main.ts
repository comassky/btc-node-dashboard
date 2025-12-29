import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from '@/App.vue';

import './assets/dashboard.css';
import './assets/animations.css';

// Import and initialize FontAwesome
import { FontAwesomeIcon } from './plugins/fontawesome';

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.component('font-awesome-icon', FontAwesomeIcon);
app.mount('#app');

//registerServiceWorker();
