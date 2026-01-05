<template>
  <footer class="bg-bg-card/50 border-border-strong mt-12 border-t py-6">
    <div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
      <div
        class="text-text-secondary flex flex-col items-center justify-between gap-4 text-sm sm:flex-row"
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
          <span class="bg-bg-app rounded px-2 py-1 font-mono text-xs">v{{ props.version }}</span>
          <a
            href="https://github.com/comassky/btc-node-dashboard"
            target="_blank"
            rel="noopener noreferrer"
            class="hover:text-accent"
          >
            <IconGithub class="text-lg" />
          </a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { IconGithub } from '@/icons';

const props = withDefaults(defineProps<{ version?: string }>(), {
  version: typeof __APP_VERSION__ !== 'undefined' ? __APP_VERSION__ : 'dev',
});

const currentYear = computed(() => new Date().getFullYear());
</script>
