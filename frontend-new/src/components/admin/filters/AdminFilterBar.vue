<template>
  <div class="d-flex align-center mb-4">
    <v-text-field
      v-model="innerQuery"
      :placeholder="placeholder"
      density="comfortable"
      clearable
      hide-details
      prepend-inner-icon="mdi-magnify"
      class="mr-2"
      style="max-width: 360px"
    />
    <slot name="extra"></slot>
    <v-spacer></v-spacer>
    <slot name="actions"></slot>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '搜索...' },
});
const emit = defineEmits(['update:modelValue']);

const innerQuery = ref(props.modelValue);

watch(() => props.modelValue, v => { if (v !== innerQuery.value) innerQuery.value = v; });
watch(innerQuery, v => emit('update:modelValue', v));
</script>

<style scoped>
</style>

