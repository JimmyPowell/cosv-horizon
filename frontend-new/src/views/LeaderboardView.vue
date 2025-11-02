<template>
  <v-container class="my-12">
    <div class="text-center mb-12">
      <h1 class="text-h2 font-weight-bold text-grey-darken-4 mb-4">贡献排行榜</h1>
      <p class="text-h6 font-weight-light text-grey-darken-1">查看您或您组织的贡献积分</p>
    </div>

    <v-tabs v-model="tab" centered class="mb-8">
      <v-tab value="users">用户排行</v-tab>
      <v-tab value="organizations">组织排行</v-tab>
    </v-tabs>

    <v-window v-model="tab">
      <v-window-item value="users">
        <UserLeaderboard />
      </v-window-item>
      <v-window-item value="organizations">
        <OrganizationLeaderboard />
      </v-window-item>
    </v-window>
  </v-container>
</template>

<script setup>
import { ref } from 'vue';
import UserLeaderboard from '@/components/UserLeaderboard.vue';
import OrganizationLeaderboard from '@/components/OrganizationLeaderboard.vue';

const tab = ref('users');
</script>
