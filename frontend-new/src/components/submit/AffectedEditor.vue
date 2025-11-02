<template>
  <div>
    <div class="d-flex align-center mb-3">
      <div class="text-subtitle-1 font-weight-bold">受影响范围（affected[]）</div>
      <v-spacer />
      <v-btn size="small" variant="text" prepend-icon="mdi-plus" @click="addAffected">添加受影响条目</v-btn>
    </div>

    <div v-if="entries.length === 0" class="text-grey text-body-2 mb-2">尚未添加</div>

    <v-card v-for="(a, idx) in entries" :key="idx" class="mb-3" variant="outlined">
      <v-card-title class="d-flex align-center">
        <div>条目 {{ idx + 1 }}</div>
        <v-spacer />
        <v-btn icon variant="text" color="error" @click="removeAffected(idx)"><v-icon>mdi-delete</v-icon></v-btn>
      </v-card-title>
      <v-divider />
      <v-card-text>
        <v-row>
          <v-col cols="12" md="3">
            <v-select v-model="a.pkg.ecosystem" :items="ecosystems" label="生态（ecosystem）" clearable density="comfortable" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model="a.pkg.name" label="包名（name）" density="comfortable" required />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model="a.pkg.language" label="语言（可选）" density="comfortable" />
          </v-col>
          <v-col cols="12" md="3">
            <v-text-field v-model="a.pkg.repository" label="仓库（可选）" density="comfortable" />
          </v-col>
          <v-col cols="12" md="6">
            <v-text-field v-model="a.pkg.purl" label="purl（可选）" density="comfortable" />
          </v-col>
          <v-col cols="12" md="6">
            <v-combobox v-model="a.versions" multiple chips clearable label="受影响版本（versions，逗号分隔或逐项录入）" :items="[]" density="comfortable" />
          </v-col>
        </v-row>

        <v-expand-transition>
          <div>
            <div class="d-flex align-center mb-2">
              <div class="text-subtitle-2">版本范围（ranges，可选）</div>
              <v-spacer />
              <v-btn size="small" variant="text" prepend-icon="mdi-plus" @click="addRange(idx)">添加范围</v-btn>
            </div>
            <v-card v-for="(r, rIdx) in a.ranges" :key="rIdx" class="mb-2" variant="tonal">
              <v-card-text>
                <v-row>
                  <v-col cols="12" md="3">
                    <v-select v-model="r.type" :items="rangeTypes" label="类型" density="comfortable" />
                  </v-col>
                  <v-col cols="12" md="7" v-if="r.type === 'GIT'">
                    <v-text-field v-model="r.repo" label="repo（仅 GIT）" density="comfortable" />
                  </v-col>
                  <v-col cols="12" :md="r.type === 'GIT' ? 2 : 9" class="d-flex align-center justify-end">
                    <v-btn icon variant="text" color="error" @click="removeRange(idx, rIdx)"><v-icon>mdi-delete</v-icon></v-btn>
                  </v-col>
                </v-row>
                <div class="d-flex align-center mb-2">
                  <div class="text-subtitle-2">事件（events）</div>
                  <v-spacer />
                  <v-btn size="small" variant="text" prepend-icon="mdi-plus" @click="addEvent(idx, rIdx)">添加事件</v-btn>
                </div>
                <v-row v-for="(ev, eIdx) in r.events" :key="eIdx">
                  <v-col cols="12" md="3">
                    <v-select v-model="ev.key" :items="eventKeys" label="事件键" density="comfortable" />
                  </v-col>
                  <v-col cols="12" md="7">
                    <v-text-field v-model="ev.value" label="值（如 1.2.3 或 提交号）" density="comfortable" />
                  </v-col>
                  <v-col cols="12" md="2" class="d-flex align-center">
                    <v-btn icon variant="text" color="error" @click="removeEvent(idx, rIdx, eIdx)"><v-icon>mdi-delete</v-icon></v-btn>
                  </v-col>
                </v-row>
              </v-card-text>
            </v-card>
          </div>
        </v-expand-transition>
      </v-card-text>
    </v-card>
  </div>
  
</template>

<script setup>
import { toRefs, reactive, watch, defineProps, defineEmits } from 'vue';

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
});
const emit = defineEmits(['update:modelValue']);

const state = reactive({
  entries: Array.isArray(props.modelValue) ? JSON.parse(JSON.stringify(props.modelValue)) : [],
});

watch(() => props.modelValue, (val) => {
  state.entries = Array.isArray(val) ? JSON.parse(JSON.stringify(val)) : [];
});
watch(() => state.entries, (val) => emit('update:modelValue', JSON.parse(JSON.stringify(val))), { deep: true });

const { entries } = toRefs(state);

const ecosystems = ['Maven','npm','PyPI','Cargo','Composer','NuGet','Go Modules','RubyGems','Other'];
const rangeTypes = ['SEMVER','ECOSYSTEM','GIT'];
const eventKeys = ['introduced','fixed','last_affected'];

function addAffected() {
  entries.value.push({
    pkg: { ecosystem: '', name: '', purl: '', language: '', repository: '' },
    versions: [],
    ranges: [],
  });
}
function removeAffected(idx) { entries.value.splice(idx, 1); }

function addRange(aIdx) {
  entries.value[aIdx].ranges.push({ type: 'SEMVER', repo: '', events: [] });
}
function removeRange(aIdx, rIdx) { entries.value[aIdx].ranges.splice(rIdx, 1); }

function addEvent(aIdx, rIdx) {
  entries.value[aIdx].ranges[rIdx].events.push({ key: 'introduced', value: '' });
}
function removeEvent(aIdx, rIdx, eIdx) { entries.value[aIdx].ranges[rIdx].events.splice(eIdx, 1); }
</script>

<style scoped>
</style>

