<template>
  <div class="home-page">
    <!-- Hero Section -->
    <section class="hero-section d-flex align-center">
      <v-container class="text-center">
        <h1 class="text-h2 font-weight-bold mb-6" style="line-height: 1.2;">
          专业的 <span class="highlight">漏洞情报</span> 平台
        </h1>
        <p class="text-h5 text-grey-darken-1 mx-auto mb-10" style="max-width: 700px;">
          实时收集、分析和展示全球软件系统漏洞信息，为您的网络安全提供全方位保护
        </p>
        <div class="mt-8">
          <v-btn size="x-large" color="primary" variant="flat" class="mr-4">免费试用</v-btn>
          <v-btn size="x-large" variant="outlined">了解更多</v-btn>
        </div>

        <!-- Stats Section -->
        <v-row class="mt-16" justify="center">
          <v-col v-for="stat in stats" :key="stat.label" cols="6" md="3">
            <div class="text-h4 font-weight-bold text-primary">{{ stat.value }}</div>
            <div class="text-body-1 text-grey-darken-1">{{ stat.label }}</div>
          </v-col>
        </v-row>
      </v-container>
    </section>

    <!-- Features Section -->
    <section class="features-section">
      <v-container>
        <div class="text-center mb-12">
          <h2 class="text-h4 font-weight-bold">核心服务特性</h2>
          <p class="text-body-1 text-grey-darken-1 mx-auto" style="max-width: 600px;">
            提供全面的漏洞情报服务，助力企业构建更安全的网络环境
          </p>
        </div>
        <v-row>
          <v-col v-for="feature in features" :key="feature.title" cols="12" md="4">
            <div class="feature-item">
              <v-row>
                <v-col cols="auto">
                  <div class="feature-icon-background" :style="{ backgroundColor: feature.bgColor }">
                    <v-icon :icon="feature.icon" :color="feature.color" size="32"></v-icon>
                  </div>
                </v-col>
                <v-col>
                  <h3 class="text-h6 font-weight-bold mb-2">{{ feature.title }}</h3>
                  <p class="text-body-2 text-grey-darken-1">{{ feature.description }}</p>
                </v-col>
              </v-row>
            </div>
          </v-col>
        </v-row>
      </v-container>
    </section>

    <!-- About Section -->
    <section class="about-section">
      <v-container>
        <v-row align="center">
          <v-col cols="12" md="6">
            <h2 class="text-h4 font-weight-bold mb-6">为什么选择 VulnScope？</h2>
            <p class="text-body-1 text-grey-darken-1 mb-8" style="line-height: 1.8;">
              VulnScope 是一个专业的软件系统漏洞数据收集与展示平台，致力于为企业和安全研究人员提供最全面、最及时的漏洞情报服务。
            </p>
            <div class="space-y-4">
              <div v-for="item in aboutItems" :key="item.title" class="d-flex align-start">
                <v-icon color="green" class="mr-3 mt-1">mdi-check-circle</v-icon>
                <div>
                  <h3 class="font-weight-bold">{{ item.title }}</h3>
                  <p class="text-grey-darken-1">{{ item.description }}</p>
                </div>
              </div>
            </div>
          </v-col>
          <v-col cols="12" md="6">
            <!-- Placeholder for the image/graphic part -->
            <v-card color="blue-grey-lighten-5" flat class="pa-8 rounded-lg">
               <v-row>
                  <v-col cols="6" v-for="card in aboutCards" :key="card.title">
                     <v-card class="pa-4 text-center">
                        <div class="text-h5 font-weight-bold" :class="card.color">{{ card.title }}</div>
                        <div class="text-body-2 text-grey-darken-1">{{ card.description }}</div>
                     </v-card>
                  </v-col>
               </v-row>
            </v-card>
          </v-col>
        </v-row>
      </v-container>
    </section>

    <!-- CTA Section -->
    <section class="cta-section">
      <v-container class="text-center">
        <h2 class="text-h4 font-weight-bold text-white mb-6">立即开始保护您的系统安全</h2>
        <p class="text-h6 text-white mx-auto mb-8" style="max-width: 700px; opacity: 0.8;">
          加入数千家企业的选择，获得专业的漏洞情报服务，构建更安全的网络环境
        </p>
        <div>
          <v-btn size="x-large" color="white" class="mr-4">免费试用 30 天</v-btn>
          <v-btn size="x-large" variant="outlined" color="white">联系销售团队</v-btn>
        </div>
      </v-container>
    </section>

    <!-- Footer Section -->
    <footer class="footer-section">
      <v-container>
        <v-row>
          <v-col cols="12" md="6">
            <div class="d-flex align-center mb-4">
              <v-icon color="blue-lighten-2" size="32" class="mr-2">mdi-shield-check</v-icon>
              <span class="text-h5 font-weight-bold">VulnScope</span>
            </div>
            <p class="text-grey-lighten-1" style="max-width: 400px;">
              专业的软件系统漏洞数据收集与展示平台，为您的网络安全保驾护航。
            </p>
          </v-col>
          <v-col cols="6" md="3">
            <h3 class="font-weight-bold mb-4">产品服务</h3>
            <ul class="list-style-none">
              <li v-for="item in products" :key="item" class="mb-2">
                <a href="#" class="text-grey-lighten-1 text-decoration-none">{{ item }}</a>
              </li>
            </ul>
          </v-col>
          <v-col cols="6" md="3">
            <h3 class="font-weight-bold mb-4">联系我们</h3>
            <ul class="list-style-none">
              <li v-for="item in contacts" :key="item" class="mb-2">
                <a href="#" class="text-grey-lighten-1 text-decoration-none">{{ item }}</a>
              </li>
            </ul>
          </v-col>
        </v-row>
        <v-divider class="my-8"></v-divider>
        <div class="text-center text-grey-darken-1">
          © 2024 VulnScope. 保留所有权利。
        </div>
      </v-container>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const stats = ref([
  { value: '50K+', label: '漏洞数据' },
  { value: '99.9%', label: '可用性' },
  { value: '24/7', label: '实时监控' },
  { value: '1000+', label: '企业用户' },
]);

const products = ref(['漏洞数据库', '威胁情报', '安全分析', 'API 接口']);
const contacts = ref(['技术支持', '商务合作', '隐私政策', '服务条款']);

const aboutItems = ref([
  { title: '全面覆盖', description: '覆盖主流软件系统和组件的漏洞信息' },
  { title: '实时更新', description: '7x24小时实时监控和数据更新' },
  { title: '专业分析', description: '专业安全团队提供深度分析和建议' },
]);

const aboutCards = ref([
    { title: 'CVE-2024', description: '最新漏洞', color: 'text-blue' },
    { title: '高危', description: '风险等级', color: 'text-red' },
    { title: '已修复', description: '状态更新', color: 'text-green' },
    { title: '分析中', description: '处理进度', color: 'text-purple' },
]);

const features = ref([
  {
    title: '实时数据收集',
    description: '自动化收集全球漏洞数据库信息，确保数据的及时性和准确性',
    icon: 'mdi-database-search',
    color: '#2196F3', // blue
    bgColor: '#E3F2FD' // blue lighten-5
  },
  {
    title: '智能分析',
    description: '基于AI算法对漏洞进行风险评估和影响分析，提供专业建议',
    icon: 'mdi-magnify-scan',
    color: '#4CAF50', // green
    bgColor: '#E8F5E9' // green lighten-5
  },
  {
    title: '趋势预测',
    description: '分析漏洞发展趋势，预测潜在安全威胁，提前做好防护准备',
    icon: 'mdi-chart-trending-up',
    color: '#9C27B0', // purple
    bgColor: '#F3E5F5' // purple lighten-5
  },
  {
    title: '预警通知',
    description: '高危漏洞实时预警，多渠道通知确保关键信息不遗漏',
    icon: 'mdi-alert-outline',
    color: '#F44336', // red
    bgColor: '#FFEBEE' // red lighten-5
  },
  {
    title: '团队协作',
    description: '支持多用户协作，权限管理，让安全团队高效协同工作',
    icon: 'mdi-account-group',
    color: '#FBC02D', // yellow darken-2
    bgColor: '#FFFDE7' // yellow lighten-5
  },
  {
    title: '数据安全',
    description: '企业级安全保障，数据加密传输存储，符合行业安全标准',
    icon: 'mdi-lock-check',
    color: '#3F51B5', // indigo
    bgColor: '#E8EAF6' // indigo lighten-5
  },
]);
</script>

<style scoped>
.home-page {
  width: 100%;
}

.hero-section {
  padding: 128px 0;
  background-color: #f9fafb;
}

.highlight {
  background: -webkit-linear-gradient(45deg, #2196F3, #9C27B0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.features-section {
  padding: 96px 0;
  background-color: #ffffff;
}

.feature-item {
  padding: 16px;
}

.feature-icon-background {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  margin-right: 16px;
}

.about-section {
  padding: 96px 0;
  background-color: #f9fafb;
}

.cta-section {
  padding: 96px 0;
  background: linear-gradient(45deg, #2196F3, #9C27B0);
}

.footer-section {
  padding: 64px 0;
  background-color: #212121;
  color: white;
}

.list-style-none {
  list-style: none;
  padding: 0;
}
</style>
