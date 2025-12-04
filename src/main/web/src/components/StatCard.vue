<template>
    <div class="stat-card bg-bg-card p-6 rounded-xl shadow-lg border-l-4 hover:shadow-2xl hover:border-accent transition duration-300 transform hover:-translate-y-0.5 flex flex-col gap-3"
         :class="`border-${color}`">

        <div class="flex justify-between items-center">
            <div class="icon text-3xl" :class="`text-${color}`">
                <font-awesome-icon :icon="icon" />
            </div>
            <div class="label text-xs uppercase text-text-secondary font-medium">{{ title }}</div>
        </div>

        <div v-if="value" class="value text-5xl font-light text-text-primary">{{ value }}</div>

        <div class="detail mt-2 pt-2 border-t border-border-strong text-sm text-text-secondary font-light">
            <slot></slot>
        </div>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    title: string;
    value?: string | number;
    icon: [string, string];
    color: 'status-success' | 'status-warning' | 'accent';
}>();
</script>