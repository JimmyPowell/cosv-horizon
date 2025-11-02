<template>
  <div class="d-inline-flex align-center uuid-cell">
    <v-tooltip :text="uuid" location="top">
      <template #activator="{ props }">
        <span v-bind="props" class="uuid-text" @click="copy" :title="uuid">
          {{ shortUuid }}
        </span>
      </template>
    </v-tooltip>
    <v-btn size="x-small" variant="text" class="ml-1" @click="copy">
      <v-icon size="14">mdi-content-copy</v-icon>
    </v-btn>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  uuid: { type: String, required: true },
  length: { type: Number, default: 10 },
});

const shortUuid = computed(() => props.uuid ? props.uuid.slice(0, props.length) : '');

function copy() {
  if (!props.uuid) return;
  navigator?.clipboard?.writeText?.(props.uuid).catch(() => {});
}
</script>

<style scoped>
.uuid-text {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  color: rgba(0,0,0,0.74);
}
.uuid-text:hover { text-decoration: underline; cursor: pointer; }
</style>
