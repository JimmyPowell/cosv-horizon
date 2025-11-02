<template>
  <v-avatar
    :size="size"
    v-bind="$attrs"
    :style="{ backgroundColor: bgColor, color: '#fff' }"
  >
    <span :style="{ fontWeight: 600, fontSize: fontSize + 'px' }">{{ initial }}</span>
  </v-avatar>
</template>

<script setup>
import { computed } from 'vue';
import { getInitialFromName, stringToHslColor } from '@/utils/avatar';

const props = defineProps({
  name: { type: String, default: '' },
  size: { type: [Number, String], default: 40 }
});

const initial = computed(() => getInitialFromName(props.name));
const bgColor = computed(() => stringToHslColor(props.name));
const fontSize = computed(() => Math.max(12, Math.floor(Number(props.size || 40) * 0.5)));
</script>

<style scoped>
</style>

