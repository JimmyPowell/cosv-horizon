<template>
  <v-container class="my-8">
    <h1 class="text-h4 mb-6">管理员中心</h1>

    <v-alert type="info" variant="tonal" class="mb-6">
      这是最小可用的管理员页面占位。可在此接入后端管理员端点。
    </v-alert>

    <v-row>
      <v-col cols="12" md="6">
        <v-card class="mb-4">
          <v-card-title>系统概览</v-card-title>
          <v-card-text>
            <div class="text-body-2 text-grey">后续可展示统计、队列、健康检查等。</div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" md="6">
        <v-card>
          <v-card-title>快速入口</v-card-title>
          <v-card-text>
            <v-chip class="mr-2 mb-2" color="primary" variant="tonal">用户管理</v-chip>
            <v-chip class="mr-2 mb-2" color="primary" variant="tonal">角色与权限</v-chip>
            <v-chip class="mr-2 mb-2" color="primary" variant="tonal">审核与审计</v-chip>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
  
</template>

<script setup>
// 最小占位，无逻辑依赖
</script>

<style scoped>
</style>

