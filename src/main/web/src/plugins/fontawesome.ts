// src/plugins/fontawesome.ts

import { library } from '@fortawesome/fontawesome-svg-core';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

// Import solid icons
import {
  faSun,
  faMoon,
  faCloud,
  faHardHat,
  faChartPie,
  faTable,
  faCubes,
  faListOl,
  faExchangeAlt,
  faExclamationCircle,
  faUserFriends,
  faSignInAlt,
  faSignOutAlt,
  faExclamationTriangle,
  faShieldAlt,
  faHdd,
  faNetworkWired,
  faProjectDiagram,
  faMask,
  faLayerGroup,
  faQuestionCircle
} from '@fortawesome/free-solid-svg-icons';

// Import regular icons
import { faClock } from '@fortawesome/free-regular-svg-icons';

// Import brand icons
import { faBitcoin } from '@fortawesome/free-brands-svg-icons';

// Add icons to the library
library.add(
  // Solid
  faSun,
  faMoon,
  faCloud,
  faHardHat,
  faChartPie,
  faTable,
  faCubes,
  faListOl,
  faExchangeAlt,
  faExclamationCircle,
  faUserFriends,
  faSignInAlt,
  faSignOutAlt,
  faExclamationTriangle,
  faShieldAlt,
  faHdd,
  faNetworkWired,
  faProjectDiagram,
  faMask,
  faLayerGroup,
  faQuestionCircle,

  // Regular
  faClock,

  // Brands
  faBitcoin
);

export { FontAwesomeIcon };
