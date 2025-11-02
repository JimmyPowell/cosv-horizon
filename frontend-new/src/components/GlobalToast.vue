<template>
  <v-snackbar
    v-model="visible"
    :timeout="timeout"
    :color="color"
    location="top right"
    elevation="6"
  >
    <div class="d-flex align-center">
      <v-icon class="mr-2">{{ icon }}</v-icon>
      <span>{{ text }}</span>
      <v-spacer />
      <v-btn v-if="actionText" color="white" variant="text" @click="onActionClick">{{ actionText }}</v-btn>
      <v-btn icon variant="text" @click="onClose"><v-icon>mdi-close</v-icon></v-btn>
    </div>
  </v-snackbar>
</template>

<script setup>
import { computed, watch } from 'vue';
import { useToastStore } from '@/stores/toast';

const store = useToastStore();

const visible = computed({
  get: () => store.visible,
  set: (v) => { if (!v) store.hide(); },
});
const text = computed(() => store.current?.text || '');
const color = computed(() => store.currentColor);
const icon = computed(() => store.currentIcon);
const timeout = computed(() => store.current?.timeout ?? 2500);
const actionText = computed(() => store.current?.actionText);

function onActionClick() {
  const fn = store.current?.onAction;
  if (typeof fn === 'function') try { fn(); } catch (_) {}
  store.hide();
}
function onClose() { store.hide(); }

// Auto-hide fallback if timeout is 0 or invalid
watch(() => store.current, (cur) => {
  if (!cur) return;
  const t = typeof cur.timeout === 'number' ? cur.timeout : 2500;
  if (t <= 0) return;
  // Vuetify timeout handles auto-hide
});
</script>

<style scoped>
</style>

