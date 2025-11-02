<template>
  <div class="text-center py-10">
    <v-icon :color="color" size="64" class="mb-3">{{ icon }}</v-icon>
    <div class="text-subtitle-1 mb-1">{{ title }}</div>
    <div class="text-body-2 text-grey-darken-1">{{ description }}</div>
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, default: 'mdi-database-off-outline' },
  title: { type: String, default: '暂无数据' },
  description: { type: String, default: '' },
  color: { type: String, default: 'grey' },
});
</script>

<style scoped>
</style>

