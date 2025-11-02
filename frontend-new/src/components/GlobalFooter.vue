<template>
  <footer class="site-footer" v-if="loaded && (icpRecord || psbRecord || copyrightText)">
    <v-container class="py-6">
      <div class="d-flex flex-column align-center text-center">
        <div v-if="icpRecord" class="mb-1">
          <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener" class="footer-link">{{ icpRecord }}</a>
        </div>
        <div v-if="psbRecord" class="mb-2">
          <a href="https://www.beian.gov.cn/" target="_blank" rel="noopener" class="footer-link">
            <v-icon size="16" class="mr-1" color="grey">mdi-shield-check-outline</v-icon>
            {{ psbRecord }}
          </a>
        </div>
        <div v-if="copyrightText" class="text-grey-darken-1" :style="copyrightStyle">
          {{ copyrightText }}
        </div>
      </div>
    </v-container>
  </footer>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import settingsApi from '@/api/settings';

const loaded = ref(false);
const icpRecord = ref('');
const psbRecord = ref('');
const copyrightText = ref('');
const fontFamily = ref('');
const fontSize = ref('');

const copyrightStyle = computed(() => {
  const style = {};
  if (fontFamily.value) style['font-family'] = fontFamily.value;
  if (fontSize.value) style['font-size'] = fontSize.value;
  return style;
});

onMounted(async () => {
  try {
    const s = await settingsApi.getSite();
    icpRecord.value = s.icpRecord || '';
    psbRecord.value = s.psbRecord || '';
    copyrightText.value = s.copyrightText || '';
    fontFamily.value = s.fontFamily || '';
    fontSize.value = s.fontSize || '';
  } catch (e) {
    // ignore
  } finally { loaded.value = true; }
});
</script>

<style scoped>
.site-footer {
  background: #fafbfc;
  border-top: 1px solid #e5e7eb;
}
.footer-link { color: #6b7280; text-decoration: none; }
.footer-link:hover { text-decoration: underline; }
</style>

