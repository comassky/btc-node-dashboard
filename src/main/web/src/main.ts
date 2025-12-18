import { createApp } from 'vue';
import App from '@/App.vue';

import './assets/dashboard.css';
import './assets/animations.css';

import { library } from '@fortawesome/fontawesome-svg-core';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

import {
    faSun,
    faMoon,
    faUserFriends,
    faSignInAlt,
    faSignOutAlt,
    faCubes,
    faListOl,
    faExchangeAlt,
    faHardHat,
    faShieldAlt,
    faNetworkWired,
    faServer,
    faExclamationCircle,
    faSpinner,
    faChartPie,
    faTable,
    faArrowDown,
    faArrowUp,
    faHdd,
    faCoins,
    faProjectDiagram,
    faMask,
    faLayerGroup,
    faQuestionCircle,
    faUserSecret,
    faCloud
} from '@fortawesome/free-solid-svg-icons';

import {
    faClock,
} from '@fortawesome/free-regular-svg-icons';

import {
    faBitcoin,
    faGithub,
} from '@fortawesome/free-brands-svg-icons';

library.add(
    faSun, faMoon, faUserFriends, faSignInAlt, faSignOutAlt, faCubes, faListOl, 
    faExchangeAlt, faHardHat, faShieldAlt, faNetworkWired, faServer, 
    faExclamationCircle, faSpinner, faChartPie, faTable, faArrowDown, faArrowUp,
    faHdd, faCoins, faProjectDiagram, faMask,
    faQuestionCircle, faUserSecret, faCloud,
    faClock, faLayerGroup,
    faBitcoin, faGithub,
);

const app = createApp(App);
app.component('font-awesome-icon', FontAwesomeIcon);
app.mount('#app');

//registerServiceWorker();
