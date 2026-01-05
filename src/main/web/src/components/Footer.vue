<template>
  <footer class="bg-bg-card/30 border-border-strong mt-16 border-t backdrop-blur-sm">
    <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <div class="flex flex-col gap-6">
        <!-- Main footer content -->
        <div class="flex flex-col items-center justify-between gap-4 sm:flex-row">
          <div class="flex flex-col items-center gap-2 sm:items-start">
            <div class="flex items-center gap-2 text-sm font-medium">
              <IconBitcoin class="text-accent text-lg" />
              <span class="text-text-primary">Bitcoin Node Dashboard</span>
            </div>
            <div class="text-text-secondary flex items-center gap-2 text-xs">
              <span>© {{ currentYear }}</span>
              <span>•</span>
              <a
                href="https://github.com/comassky/btc-node-dashboard/blob/main/LICENSE"
                target="_blank"
                rel="noopener noreferrer"
                class="hover:text-accent transition-colors"
              >
                GPL-3.0 License
              </a>
            </div>
          </div>

          <div class="flex items-center gap-3">
            <span class="bg-bg-app border-border-strong rounded-md border px-2.5 py-1 font-mono text-xs font-medium">
              v{{ props.version }}
            </span>
            <a
              href="https://github.com/comassky/btc-node-dashboard"
              target="_blank"
              rel="noopener noreferrer"
              class="hover:text-accent text-text-secondary flex items-center gap-1.5 transition-colors"
              title="View on GitHub"
            >
              <IconGithub class="text-xl" />
              <span class="text-xs font-medium">GitHub</span>
            </a>
          </div>
        </div>

        <!-- Separator -->
        <div class="border-border-strong border-t"></div>

        <!-- Additional info -->
        <div class="text-text-secondary flex flex-wrap items-center justify-center gap-x-4 gap-y-2 text-xs">
          <span class="flex items-center gap-1">
            <IconServer class="text-sm" />
            Built with Vue 3 & Quarkus
          </span>
          <span class="hidden sm:inline">•</span>
          <span class="flex items-center gap-1">
            <IconShieldHalved class="text-sm" />
            Open Source & Self-Hosted
          </span>
          <span class="hidden sm:inline">•</span>
          <a
            href="https://github.com/comassky/btc-node-dashboard/issues"
            target="_blank"
            rel="noopener noreferrer"
            class="hover:text-accent transition-colors"
          >
            Report an Issue
          </a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { IconGithub, IconBitcoin, IconServer, IconShieldHalved } from '@/icons';

const props = withDefaults(defineProps<{ version?: string }>(), {
  version: typeof __APP_VERSION__ !== 'undefined' ? __APP_VERSION__ : 'dev',
});

const currentYear = computed(() => new Date().getFullYear());
</script>
