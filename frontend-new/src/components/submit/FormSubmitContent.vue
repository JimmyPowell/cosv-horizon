<template>
  <div>
    <!-- Basic fields (Flat design) -->
    <div class="mb-6 pa-4 rounded" style="background-color: #f5f5f5;">
      <div class="d-flex align-center mb-4">
        <v-icon class="mr-2" color="primary">mdi-form-textbox</v-icon>
        <span class="text-h6 font-weight-bold">基本信息</span>
        <v-chip size="small" color="error" variant="text" class="ml-2">* 必填</v-chip>
      </div>
        <v-row>
          <v-col cols="12" md="8">
            <v-text-field 
              v-model="form.summary" 
              label="漏洞标题 *" 
              :error-messages="errors.summary" 
              required
              variant="outlined"
              prepend-inner-icon="mdi-text-short"
              hint="简洁明确地描述漏洞类型和影响"
              persistent-hint
            />
          </v-col>
          <v-col cols="12" md="4">
            <v-select
              v-model="form.language"
              :items="languageOptions"
              item-title="label"
              item-value="value"
              label="编程语言 *"
              :error-messages="errors.language"
              required
              variant="outlined"
              prepend-inner-icon="mdi-code-tags"
              hint="选择漏洞所涉及的主要编程语言"
              persistent-hint
            />
          </v-col>
          <v-col cols="12">
            <div class="mb-3">
              <div class="text-subtitle-2 font-weight-bold mb-2 d-flex align-center">
                <v-icon size="small" class="mr-2" color="primary">mdi-text-long</v-icon>
                漏洞详细描述 *
                <v-chip size="small" color="info" variant="tonal" class="ml-2">支持 Markdown</v-chip>
              </div>
              <v-textarea 
                v-model="form.details" 
                rows="8" 
                :error-messages="errors.details" 
                required
                variant="outlined"
                :placeholder="detailsPlaceholder"
                hint="详细描述漏洞的技术细节、影响范围和复现步骤"
                persistent-hint
              />
            </div>
          </v-col>
          <v-col cols="12">
            <div class="mb-3">
              <div class="text-subtitle-2 font-weight-bold mb-2 d-flex align-center">
                <v-icon size="small" class="mr-2" color="primary">mdi-speedometer</v-icon>
                CVSS 评分信息 *
                <v-chip size="small" color="warning" variant="tonal" class="ml-2">建议使用 CVSS 3.1</v-chip>
              </div>
              <v-row>
                <v-col cols="12" md="4">
                  <v-text-field
                    v-model.number="form.severityNum"
                    type="number"
                    step="0.1"
                    min="0"
                    max="10"
                    label="基础分数 *"
                    :error-messages="errors.severityNum"
                    required
                    variant="outlined"
                    prepend-inner-icon="mdi-numeric"
                    hint="0.0-10.0 的数值"
                    persistent-hint
                  />
                </v-col>
                <v-col cols="12" md="4">
                  <v-select
                    v-model="form.severityLevel"
                    :items="severityLevelOptions"
                    label="严重性等级"
                    variant="outlined"
                    prepend-inner-icon="mdi-alert"
                    hint="根据分数自动推荐"
                    persistent-hint
                  />
                </v-col>
                <v-col cols="12" md="4">
                  <v-text-field
                    v-model="form.cvssVector"
                    label="CVSS 向量字符串"
                    variant="outlined"
                    prepend-inner-icon="mdi-vector-line"
                    placeholder="CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H"
                    hint="完整的 CVSS 向量字符串（可选）"
                    persistent-hint
                  />
                </v-col>
              </v-row>
            </div>
          </v-col>
          <v-col cols="12" md="4">
            <v-select
              v-model="form.categoryCode"
              :items="categoryItems"
              item-title="nameWithCode"
              item-value="code"
              clearable
              label="漏洞分类"
              :loading="loading.categories"
              variant="outlined"
              prepend-inner-icon="mdi-tag"
              hint="选择最符合的漏洞类型分类"
              persistent-hint
            />
          </v-col>
          <v-col cols="12" md="4">
            <v-combobox
              v-model="tagCodesInput"
              :items="tagCodeSuggestions"
              multiple
              chips
              clearable
              label="标签代码"
              :loading="loading.tags"
              variant="outlined"
              prepend-inner-icon="mdi-tag-multiple"
              hint="添加相关标签，支持自定义输入"
              persistent-hint
            />
          </v-col>
        </v-row>
    </div>

    <!-- Affected (Flat design) -->
    <div class="mb-6 pa-4 rounded" style="background-color: #f5f5f5;">
      <div class="d-flex align-center mb-4">
        <v-icon class="mr-2" color="primary">mdi-format-list-bulleted</v-icon>
        <span class="text-h6 font-weight-bold">受影响范围</span>
        <v-chip size="small" color="info" variant="text" class="ml-2">可选</v-chip>
      </div>
      <AffectedEditor v-model="cosv.affected" />
    </div>

    <!-- Related projects (Flat design) -->
    <div class="mb-6 pa-4 rounded" style="background-color: #f5f5f5;">
      <div class="d-flex align-center mb-4">
        <v-icon class="mr-2" color="primary">mdi-source-repository-multiple</v-icon>
        <span class="text-h6 font-weight-bold">相关项目</span>
        <v-chip size="small" color="info" variant="text" class="ml-2">可选</v-chip>
        <v-spacer />
        <v-btn
          size="small"
          variant="outlined"
          prepend-icon="mdi-plus"
          @click="addProject"
          color="primary"
        >
          添加项目
        </v-btn>
      </div>
      <div v-if="projects.length === 0" class="text-center py-8">
        <v-icon size="64" color="grey-lighten-1" class="mb-4">mdi-package-variant</v-icon>
        <div class="text-h6 text-grey-darken-1 mb-2">暂无相关项目</div>
        <div class="text-body-2 text-grey mb-4">添加受此漏洞影响的项目信息</div>
        <v-btn
          variant="outlined"
          prepend-icon="mdi-plus"
          @click="addProject"
          color="primary"
        >
          添加第一个项目
        </v-btn>
      </div>

      <div v-else>
        <!-- Project items (Flat design with lighter background) -->
        <div
          v-for="(p, idx) in projects"
          :key="idx"
          class="mb-3 pa-3 rounded"
          style="background-color: #fafafa; border: 1px solid #eeeeee;"
        >
          <div class="d-flex align-center mb-3">
            <v-icon class="mr-2" color="primary">mdi-package</v-icon>
            <span class="text-subtitle-2 font-weight-bold">项目 {{ idx + 1 }}</span>
            <v-spacer />
            <v-btn
              icon="mdi-delete"
              variant="text"
              color="error"
              size="small"
              @click="removeProject(idx)"
            />
          </div>
          <v-row>
            <v-col cols="12" md="5">
              <v-text-field
                v-model="p.name"
                label="项目名称 *"
                variant="outlined"
                prepend-inner-icon="mdi-text"
                hint="项目的完整名称"
                persistent-hint
                required
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model="p.url"
                label="项目 URL"
                variant="outlined"
                prepend-inner-icon="mdi-link"
                hint="项目主页或仓库地址"
                persistent-hint
                placeholder="https://github.com/..."
              />
            </v-col>
            <v-col cols="12" md="3">
              <v-text-field
                v-model="p.versions"
                label="受影响版本"
                variant="outlined"
                prepend-inner-icon="mdi-tag"
                hint="如：1.0.0-2.1.5"
                persistent-hint
                placeholder="1.0.0, 2.x"
              />
            </v-col>
          </v-row>
        </div>
      </div>
    </div>

    <!-- COSV optional fields (Flat design) -->
    <div class="mb-6 pa-4 rounded" style="background-color: #f5f5f5;">
      <div class="d-flex align-center mb-4">
        <v-icon class="mr-2">mdi-file-document-outline</v-icon>
        <span class="text-h6 font-weight-bold">COSV 可选字段</span>
      </div>
        <v-alert type="info" variant="tonal" class="mb-4">
          注：Identifier 将在创建时自动分配。
        </v-alert>

        <v-expansion-panels variant="accordion">
          <!-- 基础元信息 -->
          <v-expansion-panel>
            <v-expansion-panel-title>基础元信息（schema/version/confirm/database_specific）</v-expansion-panel-title>
            <v-expansion-panel-text>
              <v-row>
                <v-col cols="12" md="4">
                  <v-text-field v-model="cosv.schemaVersion" label="schema_version（如 1.0.0）" />
                </v-col>
                <v-col cols="12" md="4">
                  <v-text-field v-model="cosv.published" label="published（RFC3339，如 2025-01-01T00:00:00Z）" />
                </v-col>
                <v-col cols="12" md="4">
                  <v-text-field v-model="cosv.withdrawn" label="withdrawn（RFC3339）" />
                </v-col>
                <v-col cols="12" md="6">
                  <v-select
                    v-model="cosv.confirmedType"
                    :items="confirmTypeOptions"
                    label="confirm_type（确认类型）"
                    clearable
                  />
                </v-col>
                <v-col cols="12" md="6">
                  <v-textarea
                    v-model="cosv.databaseSpecificText"
                    label="database_specific（JSON，可选）"
                    hint="请输入合法 JSON；留空不提交"
                    persistent-hint
                    rows="3"
                    auto-grow
                  />
                </v-col>
              </v-row>
            </v-expansion-panel-text>
          </v-expansion-panel>

          <!-- 标识与关联 -->
          <v-expansion-panel>
            <v-expansion-panel-title>标识与关联（aliases / related / references）</v-expansion-panel-title>
            <v-expansion-panel-text>
              <v-row>
                <v-col cols="12" md="6">
                  <v-combobox v-model="cosv.aliases" multiple chips clearable label="aliases（如 CVE-2025-12345）" />
                </v-col>
                <v-col cols="12" md="6">
                  <v-combobox v-model="cosv.related" multiple chips clearable label="related（相关漏洞 ID 列表）" />
                </v-col>
                <v-col cols="12">
                  <div class="d-flex align-center mb-2">
                    <div class="text-subtitle-2">references（类型+URL）</div>
                    <v-spacer />
                    <v-btn size="small" variant="text" prepend-icon="mdi-plus" @click="addReference">添加</v-btn>
                  </div>
                  <v-row v-for="(r, idx) in cosv.references" :key="`ref-${idx}`" class="mb-1">
                    <v-col cols="12" md="3"><v-text-field v-model="r.type" label="类型（如 advisory/patch）" /></v-col>
                    <v-col cols="12" md="8"><v-text-field v-model="r.url" label="URL" /></v-col>
                    <v-col cols="12" md="1" class="d-flex align-center">
                      <v-btn icon variant="text" color="error" @click="removeReference(idx)"><v-icon>mdi-delete</v-icon></v-btn>
                    </v-col>
                  </v-row>
                  <div v-if="!cosv.references || cosv.references.length === 0" class="text-grey text-body-2">尚未添加引用（可选）</div>
                </v-col>
              </v-row>
            </v-expansion-panel-text>
          </v-expansion-panel>

          <!-- CWE 与时间线 -->
          <v-expansion-panel>
            <v-expansion-panel-title>CWE 与时间线</v-expansion-panel-title>
            <v-expansion-panel-text>
              <v-row>
                <v-col cols="12" md="6">
                  <v-combobox v-model="cosv.cweIds" multiple chips clearable label="cwe_ids（如 CWE-79）" />
                </v-col>
                <v-col cols="12" md="6">
                  <v-combobox v-model="cosv.cweNames" multiple chips clearable label="cwe_names（如 XSS）" />
                </v-col>
                <v-col cols="12">
                  <div class="d-flex align-center mb-2">
                    <div class="text-subtitle-2">time_line</div>
                    <v-spacer />
                    <v-btn size="small" variant="text" prepend-icon="mdi-plus" @click="addTimeline">添加</v-btn>
                  </div>
                  <v-row v-for="(t, idx) in cosv.timeLine" :key="`tl-${idx}`" class="mb-1">
                    <v-col cols="12" md="4">
                      <v-combobox v-model="t.type" :items="timelineTypeOptions" label="类型" clearable />
                    </v-col>
                    <v-col cols="12" md="7">
                      <v-text-field v-model="t.value" label="时间（RFC3339，如 2025-01-01T00:00:00Z）" />
                    </v-col>
                    <v-col cols="12" md="1" class="d-flex align-center">
                      <v-btn icon variant="text" color="error" @click="removeTimeline(idx)"><v-icon>mdi-delete</v-icon></v-btn>
                    </v-col>
                  </v-row>
                  <div v-if="!cosv.timeLine || cosv.timeLine.length === 0" class="text-grey text-body-2">尚未添加时间点（可选）</div>
                </v-col>
              </v-row>
            </v-expansion-panel-text>
          </v-expansion-panel>

          <!-- 多量表严重性 -->
          <v-expansion-panel>
            <v-expansion-panel-title>多量表严重性（severity[]）</v-expansion-panel-title>
            <v-expansion-panel-text>
              <div class="d-flex align-center mb-2">
                <div class="text-subtitle-2">severity</div>
                <v-spacer />
                <v-btn size="small" variant="text" prepend-icon="mdi-plus" @click="addSeverity">添加</v-btn>
              </div>
              <v-row v-for="(s, idx) in cosv.severity" :key="`sev-${idx}`" class="mb-1">
                <v-col cols="12" md="3"><v-text-field v-model="s.type" label="类型（如 CVSS:3.1/BASE）" /></v-col>
                <v-col cols="12" md="3"><v-text-field v-model="s.score" label="score（原始字符串）" /></v-col>
                <v-col cols="12" md="3"><v-text-field v-model="s.level" label="level（如 HIGH）" /></v-col>
                <v-col cols="12" md="2"><v-text-field v-model.number="s.scoreNum" type="number" step="0.1" min="0" max="10" label="scoreNum" /></v-col>
                <v-col cols="12" md="1" class="d-flex align-center"><v-btn icon variant="text" color="error" @click="removeSeverity(idx)"><v-icon>mdi-delete</v-icon></v-btn></v-col>
              </v-row>
              <div v-if="!cosv.severity || cosv.severity.length === 0" class="text-grey text-body-2">尚未添加严重性条目（可选）</div>
            </v-expansion-panel-text>
          </v-expansion-panel>

          <!-- 修复与贡献 -->
          <v-expansion-panel>
            <v-expansion-panel-title>修复与贡献（patch_details / contributors / credits）</v-expansion-panel-title>
            <v-expansion-panel-text>
              <div class="d-flex align-center mb-2">
                <div class="text-subtitle-2">patch_details</div>
                <v-spacer />
                <v-btn size="small" variant="text" prepend-icon="mdi-plus" @click="addPatchDetail">添加补丁</v-btn>
              </div>
              <v-row v-for="(p, idx) in cosv.patchDetails" :key="`pd-${idx}`" class="mb-2">
                <v-col cols="12" md="3"><v-text-field v-model="p.patchUrl" label="patch_url" /></v-col>
                <v-col cols="12" md="3"><v-text-field v-model="p.issueUrl" label="issue_url" /></v-col>
                <v-col cols="12" md="2"><v-text-field v-model="p.mainLanguage" label="main_language" /></v-col>
                <v-col cols="12" md="2"><v-text-field v-model="p.author" label="author" /></v-col>
                <v-col cols="12" md="1"><v-text-field v-model="p.committer" label="committer" /></v-col>
                <v-col cols="12" md="1" class="d-flex align-center"><v-btn icon variant="text" color="error" @click="removePatchDetail(idx)"><v-icon>mdi-delete</v-icon></v-btn></v-col>
                <v-col cols="12" md="6"><v-combobox v-model="p.branches" multiple chips clearable label="branches" /></v-col>
                <v-col cols="12" md="6"><v-combobox v-model="p.tags" multiple chips clearable label="tags" /></v-col>
              </v-row>
              <div v-if="!cosv.patchDetails || cosv.patchDetails.length === 0" class="text-grey text-body-2">尚未添加补丁信息（可选）</div>

              <v-divider class="my-4" />

              <div class="d-flex align-center mb-2">
                <div class="text-subtitle-2">contributors</div>
                <v-spacer />
                <v-btn size="small" variant="text" prepend-icon="mdi-plus" @click="addContributor">添加贡献者</v-btn>
              </div>
              <v-row class="mb-2">
                <v-col cols="12" md="6">
                  <v-checkbox
                    v-model="addMeAsContributor"
                    :label="`将我加入贡献者${currentUserName ? `（${currentUserName}）` : ''}`"
                    density="compact"
                  />
                </v-col>
                <v-col cols="12" md="6" v-if="props.organizationUuid">
                  <v-checkbox
                    v-model="addOrgAsContributor"
                    :label="`将本组织加入贡献者${orgName ? `（${orgName}）` : ''}`"
                    density="compact"
                  />
                </v-col>
              </v-row>
              <v-row v-for="(c, idx) in cosv.contributors" :key="`ct-${idx}`" class="mb-1">
                <v-col cols="12" md="3"><v-text-field v-model="c.org" label="org（可选）" /></v-col>
                <v-col cols="12" md="3"><v-text-field v-model="c.name" label="name" /></v-col>
                <v-col cols="12" md="3"><v-text-field v-model="c.email" label="email" /></v-col>
                <v-col cols="12" md="2"><v-text-field v-model="c.contributions" label="contributions" /></v-col>
                <v-col cols="12" md="1" class="d-flex align-center"><v-btn icon variant="text" color="error" @click="removeContributor(idx)"><v-icon>mdi-delete</v-icon></v-btn></v-col>
              </v-row>
              <div v-if="!cosv.contributors || cosv.contributors.length === 0" class="text-grey text-body-2">尚未添加贡献者（可选）</div>

              <v-divider class="my-4" />

              <div class="d-flex align-center mb-2">
                <div class="text-subtitle-2">credits</div>
                <v-spacer />
                <v-btn size="small" variant="text" prepend-icon="mdi-plus" @click="addCredit">添加致谢</v-btn>
              </div>
              <v-row v-for="(cr, idx) in cosv.credits" :key="`cr-${idx}`" class="mb-1">
                <v-col cols="12" md="3"><v-text-field v-model="cr.name" label="name" /></v-col>
                <v-col cols="12" md="3"><v-text-field v-model="cr.type" label="type（如 reporter）" /></v-col>
                <v-col cols="12" md="5"><v-combobox v-model="cr.contact" multiple chips clearable label="contact（如邮件或URL）" /></v-col>
                <v-col cols="12" md="1" class="d-flex align-center"><v-btn icon variant="text" color="error" @click="removeCredit(idx)"><v-icon>mdi-delete</v-icon></v-btn></v-col>
              </v-row>
              <div v-if="!cosv.credits || cosv.credits.length === 0" class="text-grey text-body-2">尚未添加致谢（可选）</div>
            </v-expansion-panel-text>
          </v-expansion-panel>

          <!-- 利用状态 -->
          <v-expansion-panel>
            <v-expansion-panel-title>利用状态（exploit_status）</v-expansion-panel-title>
            <v-expansion-panel-text>
              <v-combobox v-model="cosv.exploitStatus" multiple chips clearable label="exploit_status（如 exploited_in_the_wild）" />
            </v-expansion-panel-text>
          </v-expansion-panel>
        </v-expansion-panels>
    </div>

    <!-- Footer actions -->
    <div class="d-flex align-center">
      <v-spacer />
      <v-btn variant="text" class="mr-2" @click="goBack">取消</v-btn>
      <v-btn color="primary" :loading="submitting" @click="submit" :disabled="submitting">提交</v-btn>
    </div>

    
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import categoryApi from '@/api/category';
import tagApi from '@/api/tag';
import vulnerabilityApi from '@/api/vulnerability';
import organizationApi from '@/api/organization';
import { useAuthStore } from '@/stores/auth';
import AffectedEditor from '@/components/submit/AffectedEditor.vue';

const router = useRouter();
const emit = defineEmits(['success']);
const auth = useAuthStore();

// Props from parent
const props = defineProps({
  submitter: {
    type: String,
    required: true,
  },
  organizationUuid: {
    type: String,
    default: null,
  },
});

// Form state
const form = reactive({
  summary: '',
  details: '',
  severityNum: 5.0,
  severityLevel: '',
  cvssVector: '',
  language: '',
  categoryCode: null,
});
const projects = ref([]);
const tagCodesInput = ref([]); // array of string codes

// Options
const languageOptions = [
  { label: 'Java', value: 'JAVA' },
  { label: 'Python', value: 'PYTHON' },
  { label: 'JavaScript', value: 'JAVASCRIPT' },
  { label: 'PHP', value: 'PHP' },
  { label: 'Go', value: 'GO' },
  { label: 'Rust', value: 'RUST' },
  { label: 'C', value: 'C' },
  { label: 'C++', value: 'CPP' },
  { label: '其他', value: 'OTHER' },
];

const severityLevelOptions = [
  { title: '无 (0.0)', value: 'NONE' },
  { title: '低 (0.1-3.9)', value: 'LOW' },
  { title: '中 (4.0-6.9)', value: 'MEDIUM' },
  { title: '高 (7.0-8.9)', value: 'HIGH' },
  { title: '严重 (9.0-10.0)', value: 'CRITICAL' },
];

const categoryItems = ref([]);
const tagCodeSuggestions = ref([]);

const loading = reactive({ categories: false, tags: false, orgs: false });
const submitting = ref(false);

const errors = reactive({ summary: '', details: '', language: '', severityNum: '' });

import { useToast } from '@/stores/toast';
const toast = useToast();

// Placeholder text for details field
const detailsPlaceholder = `## 漏洞概述
简要描述漏洞的基本情况和影响范围

## 技术细节
详细说明漏洞的技术原理和成因

## 影响范围
- 受影响的版本：
- 受影响的组件：
- 潜在危害：

## 复现步骤
1. 环境准备
2. 具体操作步骤
3. 预期结果

## 修复建议
提供修复方案或缓解措施`;

// COSV optional state
const cosv = reactive({
  schemaVersion: '',
  published: '',
  withdrawn: '',
  confirmedType: '',
  databaseSpecificText: '',
  aliases: [],
  related: [],
  references: [],
  cweIds: [],
  cweNames: [],
  timeLine: [],
  severity: [],
  patchDetails: [],
  contributors: [],
  credits: [],
  exploitStatus: [],
  affected: [],
});

const confirmTypeOptions = ['manual_confirmed','algorithm_confirmed','double_confirmed'];
const timelineTypeOptions = ['introduced','found','fixed','disclosed','published','withdrawn'];

// 一键贡献者注入开关与上下文
const addMeAsContributor = ref(false);
const addOrgAsContributor = ref(false);
const currentUserName = computed(() => auth?.user?.name || '');
const currentUserEmail = computed(() => auth?.user?.email || '');
const currentUserOrg = computed(() => auth?.user?.company || '');
const orgName = ref('');

// use global toast store

function addProject() { projects.value.push({ name: '', url: '', versions: '', type: 'AFFECTED' }); }
function removeProject(idx) { projects.value.splice(idx, 1); }

const canSubmit = computed(() => {
  if (!form.summary?.trim()) return false;
  if (!form.details?.trim()) return false;
  if (!form.language) return false;
  const sev = Number(form.severityNum);
  if (Number.isNaN(sev) || sev < 0 || sev > 10) return false;
  if (props.submitter === 'ORG' && !props.organizationUuid) return false;
  return true;
});

function validate() {
  errors.summary = form.summary?.trim() ? '' : '摘要为必填项';
  errors.details = form.details?.trim() ? '' : '详情为必填项';
  errors.language = form.language ? '' : '请选择语言';
  const sev = Number(form.severityNum);
  errors.severityNum = (Number.isNaN(sev) || sev < 0 || sev > 10) ? '严重性必须在 0 到 10 之间' : '';
  return !errors.summary && !errors.details && !errors.language && !errors.severityNum;
}

async function submit() {
  if (!validate()) return;
  submitting.value = true;
  try {
    // Build payload
    const payload = {
      summary: form.summary.trim(),
      details: form.details.trim(),
      severityNum: Number(form.severityNum),
      language: form.language,
    };
    if (props.submitter === 'ORG' && props.organizationUuid) payload.organizationUuid = props.organizationUuid;
    if (form.categoryCode) payload.categoryCode = form.categoryCode;
    const tagCodes = (tagCodesInput.value || []).map(s => String(s).trim()).filter(Boolean);
    if (tagCodes.length > 0) payload.tagCodes = tagCodes;
    const cleanProjects = (projects.value || []).map(p => ({
      name: (p?.name || '').trim(),
      url: (p?.url || '').trim() || undefined,
      versions: (p?.versions || '').trim() || undefined,
      type: (p?.type || '').trim() || undefined,
    })).filter(p => p.name);
    if (cleanProjects.length > 0) payload.projects = cleanProjects;

    // Build COSV payload if provided
    const cosvPayload = {};
    const hasStr = (s) => typeof s === 'string' && s.trim().length > 0;
    if (hasStr(cosv.schemaVersion)) cosvPayload.schemaVersion = cosv.schemaVersion.trim();
    if (hasStr(cosv.published)) cosvPayload.published = cosv.published.trim();
    if (hasStr(cosv.withdrawn)) cosvPayload.withdrawn = cosv.withdrawn.trim();
    if (hasStr(cosv.confirmedType)) cosvPayload.confirmedType = cosv.confirmedType.trim();
    if (hasStr(cosv.databaseSpecificText)) {
      try {
        const parsed = JSON.parse(cosv.databaseSpecificText);
        cosvPayload.databaseSpecific = parsed;
      } catch (err) {
        toast.error('database_specific 需为合法 JSON');
        submitting.value = false;
        return;
      }
    }
    const arrStr = (arr) => Array.isArray(arr) ? arr.map(x => String(x || '').trim()).filter(Boolean) : [];
    const mapRows = (arr, pred) => Array.isArray(arr) ? arr.filter(pred) : [];
    const refs = mapRows(cosv.references, r => hasStr(r?.url)).map(r => ({ type: hasStr(r?.type) ? r.type.trim() : undefined, url: r.url.trim() }));
    if (refs.length) cosvPayload.references = refs;
    const als = arrStr(cosv.aliases); if (als.length) cosvPayload.aliases = als;
    const rels = arrStr(cosv.related); if (rels.length) cosvPayload.related = rels;
    const cids = arrStr(cosv.cweIds); if (cids.length) cosvPayload.cweIds = cids;
    const cns = arrStr(cosv.cweNames); if (cns.length) cosvPayload.cweNames = cns;
    const tls = mapRows(cosv.timeLine, t => hasStr(t?.type) && hasStr(t?.value)).map(t => ({ type: t.type.trim(), value: t.value.trim() }));
    if (tls.length) cosvPayload.timeLine = tls;
    const sevs = mapRows(cosv.severity, s => hasStr(s?.type) || hasStr(s?.score) || hasStr(s?.level) || (typeof s?.scoreNum === 'number')).map(s => ({
      type: hasStr(s?.type) ? s.type.trim() : undefined,
      score: hasStr(s?.score) ? s.score.trim() : undefined,
      level: hasStr(s?.level) ? s.level.trim() : undefined,
      scoreNum: (typeof s?.scoreNum === 'number') ? Number(s.scoreNum) : undefined,
    }));
    if (sevs.length) cosvPayload.severity = sevs;
    const pds = mapRows(cosv.patchDetails, p => hasStr(p?.patchUrl) || hasStr(p?.issueUrl) || hasStr(p?.author) || hasStr(p?.committer) || hasStr(p?.mainLanguage) || (Array.isArray(p?.branches) && p.branches.length) || (Array.isArray(p?.tags) && p.tags.length)).map(p => ({
      patchUrl: hasStr(p?.patchUrl) ? p.patchUrl.trim() : undefined,
      issueUrl: hasStr(p?.issueUrl) ? p.issueUrl.trim() : undefined,
      mainLanguage: hasStr(p?.mainLanguage) ? p.mainLanguage.trim() : undefined,
      author: hasStr(p?.author) ? p.author.trim() : undefined,
      committer: hasStr(p?.committer) ? p.committer.trim() : undefined,
      branches: arrStr(p?.branches),
      tags: arrStr(p?.tags),
    }));
    if (pds.length) cosvPayload.patchDetails = pds;
    let contribs = mapRows(cosv.contributors, c => hasStr(c?.name) || hasStr(c?.email) || hasStr(c?.org) || hasStr(c?.contributions)).map(c => ({
      org: hasStr(c?.org) ? c.org.trim() : undefined,
      name: hasStr(c?.name) ? c.name.trim() : undefined,
      email: hasStr(c?.email) ? c.email.trim() : undefined,
      contributions: hasStr(c?.contributions) ? c.contributions.trim() : undefined,
    }));
    // 一键注入贡献者
    const addIfAbsent = (list, item) => {
      const key = (v) => [v.org||'', v.name||'', v.email||''].map(x => String(x).trim().toLowerCase()).join('|');
      const exists = list.some(v => key(v) === key(item));
      if (!exists) list.push(item);
    };
    if (addMeAsContributor?.value && currentUserName?.value) {
      addIfAbsent(contribs, {
        org: currentUserOrg?.value || undefined,
        name: currentUserName.value,
        email: currentUserEmail?.value || undefined,
        contributions: 'reporter',
      });
    }
    if (addOrgAsContributor?.value && orgName?.value) {
      addIfAbsent(contribs, {
        org: orgName.value,
        name: undefined,
        email: undefined,
        contributions: 'organization',
      });
    }
    if (contribs.length) cosvPayload.contributors = contribs;
    const creds = mapRows(cosv.credits, cr => hasStr(cr?.name) || (Array.isArray(cr?.contact) && cr.contact.length) || hasStr(cr?.type)).map(cr => ({
      name: hasStr(cr?.name) ? cr.name.trim() : undefined,
      type: hasStr(cr?.type) ? cr.type.trim() : undefined,
      contact: arrStr(cr?.contact),
    }));
    if (creds.length) cosvPayload.credits = creds;
    const exs = arrStr(cosv.exploitStatus); if (exs.length) cosvPayload.exploitStatus = exs;

    // affected[]
    const afs = mapRows(cosv.affected, a => a && a.pkg && typeof a.pkg.name === 'string' && a.pkg.name.trim().length > 0).map(a => {
      const pkg = {
        ecosystem: hasStr(a.pkg?.ecosystem) ? a.pkg.ecosystem.trim() : undefined,
        name: a.pkg.name.trim(),
        purl: hasStr(a.pkg?.purl) ? a.pkg.purl.trim() : undefined,
        language: hasStr(a.pkg?.language) ? a.pkg.language.trim() : undefined,
        repository: hasStr(a.pkg?.repository) ? a.pkg.repository.trim() : undefined,
      };
      const versions = Array.isArray(a.versions) ? a.versions.map(v => String(v || '').trim()).filter(Boolean) : undefined;
      const ranges = Array.isArray(a.ranges) ? a.ranges.filter(r => hasStr(r?.type)).map(r => {
        const events = Array.isArray(r.events) ? r.events.filter(e => hasStr(e?.key) && hasStr(e?.value)).map(e => ({ [e.key.trim()]: e.value.trim() })) : [];
        const rd = { type: r.type.trim() };
        if (hasStr(r?.repo) && r.type === 'GIT') rd.repo = r.repo.trim();
        if (events.length) rd.events = events;
        return rd;
      }) : undefined;
      const entry = { pkg };
      if (versions && versions.length) entry.versions = versions;
      if (ranges && ranges.length) entry.ranges = ranges;
      return entry;
    });
    if (afs.length) cosvPayload.affected = afs;

    // Attach COSV only if not empty
    if (Object.keys(cosvPayload).length > 0) payload.cosv = cosvPayload;

    const res = await vulnerabilityApi.create(payload);
    // 后端统一返回 { code, message, data }，非0需友好提示
    if (!res || res.data?.code !== 0) {
      const em = res?.data?.message || '提交失败';
      toast.error(em);
      submitting.value = false;
      return;
    }
    const v = res.data.data?.vulnerability || {};
    const uuid = v.uuid;
    const status = v.status;

    if (status === 'ACTIVE' && uuid) {
      toast.success('创建成功，已发布');
      emit('success');
      router.push(`/vulnerabilities/${uuid}`);
    } else {
      toast.success('已提交，待审核');
      emit('success');
      router.push('/dashboard');
    }
  } catch (e) {
    // Gracefully show backend error
    const msg = e?.response?.data?.message || e?.message || '提交失败';
    toast.error(msg);
  } finally {
    submitting.value = false;
  }
}

async function loadCategories() {
  loading.categories = true;
  try {
    const res = await categoryApi.list({ page: 1, size: 100, withTotal: false });
    const items = res?.data?.data?.items || [];
    categoryItems.value = items.map(it => ({
      code: it?.code,
      nameWithCode: it?.name && it?.code ? `${it.name}（${it.code}）` : (it?.name || it?.code || ''),
    })).filter(i => i.code);
  } catch {
    categoryItems.value = [];
  } finally {
    loading.categories = false;
  }
}

async function loadTags() {
  loading.tags = true;
  try {
    const res = await tagApi.list({ page: 1, size: 100, withTotal: false });
    const items = res?.data?.data?.items || [];
    tagCodeSuggestions.value = items.map(t => t?.code).filter(Boolean);
  } catch {
    tagCodeSuggestions.value = [];
  } finally {
    loading.tags = false;
  }
}

onMounted(async () => {
  loadCategories();
  loadTags();
  // 开关默认值：个人提交默认勾选“将我加入贡献者”
  addMeAsContributor.value = props.submitter === 'USER';
  addOrgAsContributor.value = false;
  if (props.organizationUuid) {
    try {
      const r = await organizationApi.getByUuid(props.organizationUuid);
      if (r?.data?.code === 0) orgName.value = r.data.data?.organization?.name || '';
    } catch {}
  }
});

// COSV helpers
function addReference() { if (!Array.isArray(cosv.references)) cosv.references = []; cosv.references.push({ type: '', url: '' }); }
function removeReference(i) { if (Array.isArray(cosv.references)) cosv.references.splice(i, 1); }
function addTimeline() { if (!Array.isArray(cosv.timeLine)) cosv.timeLine = []; cosv.timeLine.push({ type: '', value: '' }); }
function removeTimeline(i) { if (Array.isArray(cosv.timeLine)) cosv.timeLine.splice(i, 1); }
function addSeverity() { if (!Array.isArray(cosv.severity)) cosv.severity = []; cosv.severity.push({ type: '', score: '', level: '', scoreNum: undefined }); }
function removeSeverity(i) { if (Array.isArray(cosv.severity)) cosv.severity.splice(i, 1); }
function addPatchDetail() { if (!Array.isArray(cosv.patchDetails)) cosv.patchDetails = []; cosv.patchDetails.push({ patchUrl: '', issueUrl: '', mainLanguage: '', author: '', committer: '', branches: [], tags: [] }); }
function removePatchDetail(i) { if (Array.isArray(cosv.patchDetails)) cosv.patchDetails.splice(i, 1); }
function addContributor() { if (!Array.isArray(cosv.contributors)) cosv.contributors = []; cosv.contributors.push({ org: '', name: '', email: '', contributions: '' }); }
function removeContributor(i) { if (Array.isArray(cosv.contributors)) cosv.contributors.splice(i, 1); }
function addCredit() { if (!Array.isArray(cosv.credits)) cosv.credits = []; cosv.credits.push({ name: '', type: '', contact: [] }); }
function removeCredit(i) { if (Array.isArray(cosv.credits)) cosv.credits.splice(i, 1); }
</script>

<style scoped>
/* Unified border radius */
.rounded {
  border-radius: 8px;
}

.v-text-field .v-field {
  border-radius: 8px;
}

.v-select .v-field {
  border-radius: 8px;
}

.v-textarea .v-field {
  border-radius: 8px;
}

.v-btn {
  border-radius: 8px;
  text-transform: none;
  font-weight: 500;
}

.v-chip {
  border-radius: 6px;
}
</style>
function goBack() {
  router.back();
}
