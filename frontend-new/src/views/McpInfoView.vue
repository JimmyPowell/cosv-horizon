<template>
  <v-container class="my-8">
    <div class="d-flex align-center mb-4">
      <v-icon size="28" color="primary" class="mr-2">mdi-application-braces-outline</v-icon>
      <h1 class="text-h4">MCP 集成</h1>
    </div>

    <v-alert type="info" variant="tonal" class="mb-6">
      该页面用于介绍并准备即将上线的 MCP（Model Context Protocol）能力。面向所有登录用户开放。
    </v-alert>

    <v-card class="elevation-2 mb-6">
      <v-card-title>什么是 MCP？</v-card-title>
      <v-divider></v-divider>
      <v-card-text>
        <p class="text-body-2 mb-4">
          MCP（Model Context Protocol）用于在平台与外部工具、数据源之间建立标准化连接，使模型能够在安全、可控的前提下访问所需上下文与能力。
        </p>
        <p class="text-body-2 mb-1">后续在本页面你将能够：</p>
        <ul class="mb-4">
          <li>连接与管理你的 MCP 供应商与工具</li>
          <li>为个人或组织配置访问密钥与权限</li>
          <li>查看连接状态与调用日志</li>
        </ul>
        <p class="text-body-2 text-grey-darken-1">功能迭代中，欢迎反馈你的使用场景与需求。</p>
      </v-card-text>
    </v-card>

    <v-card class="elevation-1">
      <v-card-title>下一步</v-card-title>
      <v-divider></v-divider>
      <v-card-text>
        <p class="text-body-2 mb-2">你可以先前往「访问控制」创建 API 密钥，为后续的集成做准备。</p>
        <v-btn color="primary" variant="outlined" :to="{ name: 'access-control' }" prepend-icon="mdi-shield-key">
          前往访问控制
        </v-btn>
      </v-card-text>
    </v-card>
  </v-container>
  
</template>

<script setup>
// 说明页，无需额外逻辑
</script>

<style scoped>
ul {
  padding-left: 1.2rem;
}
li {
  margin-bottom: 6px;
}
</style>

