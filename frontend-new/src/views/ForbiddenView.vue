<template>
  <v-container class="my-12 text-center">
    <v-icon color="error" size="72" class="mb-4">mdi-block-helper</v-icon>
    <h1 class="text-h4 mb-2">403 无权限访问</h1>
    <p class="text-body-1 text-grey-darken-1 mb-6">
      您没有访问此页面的权限。如需访问，请联系系统管理员。
    </p>
    <v-btn color="primary" to="/dashboard" variant="flat">返回仪表盘</v-btn>
  </v-container>
</template>

<script setup>
// 简单403占位页
</script>

<style scoped>
</style>

