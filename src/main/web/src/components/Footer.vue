<template>
  <footer class="bg-bg-card/50 mt-12 border-t border-border-strong py-6">
    <div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
      <div
        class="flex flex-col items-center justify-between gap-4 text-sm text-text-secondary sm:flex-row"
      >
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
          <span class="rounded bg-bg-app px-2 py-1 font-mono text-xs">v{{ props.version }}</span>
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
import { computed } from 'vue';
import { library } from '@fortawesome/fontawesome-svg-core';
import { faGithub } from '@fortawesome/free-brands-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

library.add(faGithub);

const props = withDefaults(defineProps<{ version?: string }>(), {
  version: typeof __APP_VERSION__ !== 'undefined' ? __APP_VERSION__ : 'dev',
});

const currentYear = computed(() => new Date().getFullYear());
</script>
