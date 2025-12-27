<template>
  <footer class="mt-12 py-6 border-t border-border-strong bg-bg-card/50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex flex-col sm:flex-row justify-between items-center gap-4 text-sm text-text-secondary">
        <div class="flex items-center gap-2">
          <span>© {{ currentYear }} Bitcoin Node Dashboard</span>
          <span class="hidden sm:inline">•</span>
          <a 
            href="https://github.com/comassky/btc-node-dashboard/blob/main/LICENSE" 
            target="_blank"
            rel="noopener noreferrer"
            class="hover:text-accent"
          >
            GPL-3.0 License
          </a>
        </div>
        <div class="flex items-center gap-2">
          <span class="font-mono text-xs bg-bg-app px-2 py-1 rounded">v{{ version }}</span>
          <a 
            href="https://github.com/comassky/btc-node-dashboard" 
            target="_blank"
            rel="noopener noreferrer"
            class="hover:text-accent"
          >
            <font-awesome-icon :icon="['fab', 'github']" class="text-lg" />
          </a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { library } from '@fortawesome/fontawesome-svg-core';
import { faGithub } from '@fortawesome/free-brands-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

library.add(faGithub);

const version = __APP_VERSION__;
const currentYear = new Date().getFullYear();
</script>
