<template>
  <v-container class="my-8">
    <v-btn variant="text" prepend-icon="mdi-arrow-left" @click="$router.back()" class="mb-4">返回</v-btn>

    <div v-if="loading" class="text-center mt-16">
      <v-progress-circular indeterminate color="primary" size="64" />
    </div>
    <div v-else-if="user">
      <div class="d-flex align-center mb-6">
        <AppAvatar :name="user.name || '?'" :size="72" class="mr-4" />
        <div>
          <h1 class="text-h4">{{ user.name }}</h1>
          <div class="text-grey">{{ user.company || '-' }} • {{ user.location || '-' }}</div>
        </div>
      </div>

      <v-card class="elevation-2 mb-6">
        <v-card-title>个人信息</v-card-title>
        <v-card-text>
          <div class="mb-2"><strong>GitHub：</strong><a v-if="user.gitHub" :href="user.gitHub" target="_blank">{{ user.gitHub }}</a><span v-else>-</span></div>
          <div class="mb-2"><strong>网站：</strong><a v-if="user.website" :href="user.website" target="_blank">{{ user.website }}</a><span v-else>-</span></div>
          <div class="mb-2"><strong>简介：</strong><span>{{ user.freeText || '-' }}</span></div>
        </v-card-text>
      </v-card>
    </div>
    <div v-else class="text-center mt-16">
      <h2 class="text-h5 text-grey">用户未找到</h2>
    </div>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import userApi from '@/api/user';
import AppAvatar from '@/components/AppAvatar.vue';

const route = useRoute();
const loading = ref(true);
const user = ref(null);

const fetchUser = async () => {
  loading.value = true;
  try {
    const resp = await userApi.getByUuid(route.params.id);
    if (resp.data && resp.data.code === 0) {
      user.value = resp.data.data.user || null;
    }
  } catch (_) { user.value = null; }
  finally { loading.value = false; }
};

onMounted(fetchUser);
</script>

<style scoped>
</style>
