<template>
  <div>
    <v-row>
      <v-col v-for="(user, index) in topContributors" :key="user.uuid" :cols="12" :md="index === 0 ? 12 : 6">
        <v-card :class="['elevation-4', 'pa-4', 'd-flex', 'align-center', 'contributor-card', 'rank-' + (index + 1)]">
          <div class="rank-badge font-weight-bold text-h4 mr-6">{{ index + 1 }}</div>
          <AppAvatar :name="user.name || user.email || '?'" :size="80" class="mr-6 elevation-2" />
          <div class="flex-grow-1">
            <div class="text-h5 font-weight-bold">{{ user.name }}</div>
            <div class="text-subtitle-1 text-grey-darken-2">{{ user.company }}</div>
          </div>
          <div class="d-flex align-center">
            <v-icon color="amber" class="mr-2">mdi-star</v-icon>
            <span class="text-h4 font-weight-bold">{{ user.rating }}</span>
          </div>
        </v-card>
      </v-col>
    </v-row>

    <v-row class="mt-12">
      <v-col>
        <v-card class="elevation-4">
          <v-card-title class="text-h5 font-weight-bold pa-4">所有贡献者</v-card-title>
          <v-divider></v-divider>
          <v-table class="contributors-table">
            <thead>
              <tr>
                <th class="text-left font-weight-bold text-subtitle-1">排名</th>
                <th class="text-left font-weight-bold text-subtitle-1">用户</th>
                <th class="text-right font-weight-bold text-subtitle-1">分数</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(user, index) in allContributors" :key="user.uuid">
                <td>{{ index + 4 }}</td>
                <td>
                  <div class="d-flex align-center">
                    <AppAvatar :name="user.name || user.email || '?'" :size="40" class="mr-4" />
                    <div>
                      <div class="font-weight-bold">{{ user.name }}</div>
                      <div class="text-caption text-grey">{{ user.company }}</div>
                    </div>
                  </div>
                </td>
                <td class="text-right font-weight-medium">{{ user.rating }}</td>
              </tr>
            </tbody>
          </v-table>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import leaderboardApi from '@/api/leaderboard';
import AppAvatar from '@/components/AppAvatar.vue';

const contributors = ref([]);

onMounted(async () => {
  try {
    contributors.value = await leaderboardApi.getUserLeaderboard(20); // array
  } catch (error) {
    console.error("Failed to fetch user leaderboard:", error);
  }
});

const topContributors = computed(() => contributors.value.slice(0, 3));
const allContributors = computed(() => contributors.value.slice(3));
</script>

<style scoped>
.contributor-card {
  border-radius: 12px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.contributor-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
}
.rank-badge {
  color: #fff;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #6e8efb, #a777e3);
}
.rank-1 .rank-badge {
  background: linear-gradient(135deg, #ffab2d, #ff7d4a);
}
.rank-2 .rank-badge {
  background: linear-gradient(135deg, #8998a8, #b8c7d8);
}
.rank-3 .rank-badge {
  background: linear-gradient(135deg, #d19a66, #a47b54);
}
.contributors-table {
  background-color: #ffffff;
}
.v-table > .v-table__wrapper > table > tbody > tr:not(:last-child) > td, .v-table > .v-table__wrapper > table > tbody > tr:not(:last-child) > th {
    border-bottom: 1px solid #f0f0f0;
}
</style>
