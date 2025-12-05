<template>
  <footer class="mt-12 py-6 border-t border-border-strong bg-bg-card/50 backdrop-blur-sm">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex flex-col sm:flex-row justify-between items-center gap-4 text-sm text-text-secondary">
        <div class="flex items-center gap-2">
          <span>© {{ currentYear }} Bitcoin Node Dashboard</span>
          <span class="hidden sm:inline">•</span>
          <a 
            href="https://github.com/comassky/btc-node-dashboard/blob/main/LICENSE" 
            target="_blank"
            rel="noopener noreferrer"
            class="hover:text-accent transition-colors"
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
            class="hover:text-accent transition-colors"
          >
            <font-awesome-icon :icon="['fab', 'github']" class="text-lg" />
          </a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const version = __APP_VERSION__;
const currentYear = computed(() => new Date().getFullYear());
</script>
