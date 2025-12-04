// main.ts

import { createApp } from 'vue';
import App from './App.vue';


import './assets/dashboard.css';

// 1. Font Awesome core imports
import { library } from '@fortawesome/fontawesome-svg-core';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

// 2. SOLID icons (fas) imports
import {
    faSun,              // App.vue (Toggle Dark Mode)
    faMoon,             // App.vue (Toggle Dark Mode)
    faUserFriends,      // App.vue & StatCardGrid.vue (Total Peers)
    faSignInAlt,        // App.vue & StatCardGrid.vue (Inbound)
    faSignOutAlt,       // App.vue & StatCardGrid.vue (Outbound)
    faCubes,            // App.vue & StatCardGrid.vue (Current Block)
    faListOl,           // App.vue & StatCardGrid.vue (Headers)
    faExchangeAlt,      // App.vue & StatCardGrid.vue (Tx Count)
    faHardHat,          // App.vue & StatCardGrid.vue (Node Details)
    faShieldAlt,        // App.vue & StatCardGrid.vue (Verification Progress)
    faNetworkWired,     // Status.vue (WebSocket Status)
    faServer,           // Status.vue (Node RPC Status)
    faExclamationCircle, // Status.vue (Error Message)
    faSpinner,          // App.vue (Connecting RPC)
    faChartPie,         // App.vue (Peer Software Distribution)
    faTable,            // App.vue (Connection Details)
    faArrowDown,        // PeerTable.vue (Received)
    faArrowUp,          // PeerTable.vue (Sent)
} from '@fortawesome/free-solid-svg-icons';

// 3. REGULAR icons (far) imports
import {
    faClock,            // App.vue & StatCardGrid.vue (Time)
} from '@fortawesome/free-regular-svg-icons';

// 4. BRAND icons (fab) imports
import {
    faBitcoin,          // App.vue (Title)
} from '@fortawesome/free-brands-svg-icons';


// 5. Add all necessary icons to the global library
library.add(
    // Solid (fas)
    faSun, faMoon, faUserFriends, faSignInAlt, faSignOutAlt, faCubes, faListOl, 
    faExchangeAlt, faHardHat, faShieldAlt, faNetworkWired, faServer, 
    faExclamationCircle, faSpinner, faChartPie, faTable, faArrowDown, faArrowUp,

    // Regular (far)
    faClock,

    // Brands (fab)
    faBitcoin,
);


// 6. Create Vue application
const app = createApp(App);

// 7. Register Font Awesome component globally
app.component('font-awesome-icon', FontAwesomeIcon);

app.mount('#app');


