<template>
  <v-dialog v-model="model" max-width="420">
    <v-card>
      <v-card-title class="text-h6">{{ title }}</v-card-title>
      <v-card-text>{{ message }}</v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn variant="text" @click="onCancel">取消</v-btn>
        <v-btn color="primary" @click="onConfirm">确定</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '确认操作' },
  message: { type: String, default: '确定要执行该操作吗？' },
});
const emit = defineEmits(['update:modelValue', 'confirm', 'cancel']);

const model = computed({
  get: () => props.modelValue,
  set: v => emit('update:modelValue', v)
});

function onCancel() { emit('cancel'); emit('update:modelValue', false); }
function onConfirm() { emit('confirm'); emit('update:modelValue', false); }
</script>

<style scoped>
</style>

