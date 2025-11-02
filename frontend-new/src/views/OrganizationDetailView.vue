<template>
  <v-container class="my-8">
    <v-btn variant="text" prepend-icon="mdi-arrow-left" to="/organizations" class="mb-4">
      返回我的组织
    </v-btn>

    <div v-if="loading" class="text-center mt-16">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
    </div>
    
    <div v-else-if="organization">
      <div class="d-flex align-center mb-4">
        <AppAvatar :name="organization.name || '?'" :size="64" class="mr-4" />
        <div>
          <div class="d-flex align-center">
            <h1 class="text-h4 mr-2 mb-0">{{ organization.name }}</h1>
            <v-tooltip v-if="organization.isVerified" text="官方认证组织" location="bottom">
              <template #activator="{ props }">
                <v-icon v-bind="props" color="primary" size="20" class="ml-1">mdi-check-decagram</v-icon>
              </template>
            </v-tooltip>
            <v-chip v-if="organization.status && organization.status!=='ACTIVE'" size="small" :color="orgStatusColor(organization.status)" label class="ml-2">
              {{ organization.status }}
            </v-chip>
          </div>
          <p class="text-grey">{{ organization.description }}</p>
        </div>
      </div>
      
      <v-tabs v-model="tab" class="mb-8">
        <v-tab value="overview">
          <v-icon start>mdi-eye-outline</v-icon>
          概览
        </v-tab>
        <v-tab value="vulns">
          <v-icon start>mdi-bug-outline</v-icon>
          漏洞
        </v-tab>
        <v-tab value="members">
          <v-icon start>mdi-account-group-outline</v-icon>
          成员 ({{ members.length }})
        </v-tab>
        <v-tab value="invites" v-if="isOrgAdmin">
          <v-icon start>mdi-email-multiple-outline</v-icon>
          邀请
        </v-tab>
        <v-tab value="join-requests" v-if="isOrgAdmin">
          <v-icon start>mdi-account-plus-outline</v-icon>
          加入申请
        </v-tab>
        <v-tab value="points" v-if="isOrgAdmin">
          <v-icon start>mdi-star-circle-outline</v-icon>
          积分流水
        </v-tab>
        <v-tab value="settings" v-if="isOrgAdmin">
          <v-icon start>mdi-cog-outline</v-icon>
          设置
        </v-tab>
      </v-tabs>

      <v-window v-model="tab">
        <!-- Overview Tab -->
        <v-window-item value="overview">
          <!-- 申请加入提示 -->
          <v-alert v-if="showApplyJoin" type="info" variant="tonal" class="mb-4">
            该组织允许公开申请加入。
            <v-btn class="ml-2" color="primary" size="small" @click="showJoinDialog = true">申请加入</v-btn>
          </v-alert>

          <!-- 组织详细介绍 -->
          <v-card v-if="organization.freeText" class="elevation-2 mb-4">
            <v-card-title class="d-flex align-center">
              <v-icon class="mr-2">mdi-information-outline</v-icon>
              关于组织
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text class="pa-6">
              <div v-html="renderedOrgFreeText" class="markdown-body"></div>
            </v-card-text>
          </v-card>

          <!-- 组织统计信息 -->
          <v-row class="mb-4">
            <v-col cols="12" md="4">
              <v-card class="elevation-2">
                <v-card-text class="text-center">
                  <v-icon size="48" color="primary">mdi-account-group</v-icon>
                  <div class="text-h4 mt-2">{{ members.length }}</div>
                  <div class="text-grey">成员数量</div>
                </v-card-text>
              </v-card>
            </v-col>
            <v-col cols="12" md="4">
              <v-card class="elevation-2">
                <v-card-text class="text-center">
                  <v-icon size="48" color="orange">mdi-bug</v-icon>
                  <div class="text-h4 mt-2">{{ vulnTotal }}</div>
                  <div class="text-grey">漏洞总数</div>
                </v-card-text>
              </v-card>
            </v-col>
            <v-col cols="12" md="4">
              <v-card class="elevation-2">
                <v-card-text class="text-center">
                  <v-icon size="48" color="green">mdi-star</v-icon>
                  <div class="text-h4 mt-2">{{ (orgPointsSummary.rating ?? organization.rating) || 0 }}</div>
                  <div class="text-grey">组织积分</div>
                  <div class="text-caption text-grey mt-1">排名：{{ orgPointsSummary.rank ?? '-' }}</div>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>

          <!-- 最近漏洞 -->
          <v-card class="elevation-2">
            <v-card-title>
              <v-icon class="mr-2">mdi-bug-outline</v-icon>
              最近漏洞
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text v-if="vulns.length === 0" class="text-center py-16">
              <v-icon size="64" color="grey">mdi-bug-outline</v-icon>
              <p class="mt-4 text-grey">暂无漏洞数据</p>
            </v-card-text>
            <v-card-text v-else>
              <v-list>
                <v-list-item
                  v-for="vuln in vulns.slice(0, 5)"
                  :key="vuln.uuid"
                  @click="goVuln(vuln.uuid)"
                  class="cursor-pointer"
                >
                  <template v-slot:prepend>
                    <v-chip
                      :color="severityToColor(vuln.severity)"
                      size="small"
                      class="mr-2"
                    >
                      {{ formatSeverity(vuln.severity) }}
                    </v-chip>
                  </template>
                  <v-list-item-title>{{ vuln.title }}</v-list-item-title>
                  <v-list-item-subtitle>
                    {{ formatDateTime(vuln.modifiedDate) }}
                  </v-list-item-subtitle>
                </v-list-item>
              </v-list>
              <v-divider></v-divider>
              <div class="text-center pa-4">
                <v-btn variant="text" color="primary" @click="tab = 'vulns'">
                  查看全部漏洞
                  <v-icon end>mdi-arrow-right</v-icon>
                </v-btn>
              </div>
            </v-card-text>
          </v-card>
        </v-window-item>

        <!-- Vulns Tab -->
        <v-window-item value="vulns">
          <v-card class="elevation-2">
            <v-card-title class="d-flex align-center">
              <span>组织漏洞</span>
              <v-spacer></v-spacer>
              <v-select
                v-model="vulnSortBy"
                :items="sortByItems"
                item-title="label"
                item-value="value"
                density="compact"
                style="max-width: 160px"
                hide-details
              />
              <v-select
                v-model="vulnSortOrder"
                :items="sortOrderItems"
                item-title="label"
                item-value="value"
                density="compact"
                style="max-width: 120px"
                hide-details
              />
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text>
              <div v-if="loadingVulns" class="text-center py-8">
                <v-progress-circular indeterminate color="primary"></v-progress-circular>
              </div>
              <template v-else>
                <v-table class="data-table">
                  <thead>
                    <tr>
                      <th style="width: 15%">标识符</th>
                      <th style="width: 45%">标题</th>
                      <th style="width: 10%">严重度</th>
                      <th style="width: 10%">语言</th>
                      <th style="width: 20%">修改时间</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="v in vulns" :key="v.uuid" class="vuln-row" @click="goVuln(v.uuid)" style="cursor:pointer;">
                      <td><code>{{ v.identifier }}</code></td>
                      <td class="font-weight-medium">{{ v.summary }}</td>
                      <td>
                        <v-chip :color="severityToColor(v.severityNum)" size="small" label>{{ formatSeverity(v.severityNum) }}</v-chip>
                      </td>
                      <td>{{ v.language || '-' }}</td>
                      <td>{{ formatDateTime(v.modified) }}</td>
                    </tr>
                  </tbody>
                </v-table>
                <div class="d-flex align-center justify-space-between mt-4">
                  <span class="text-grey">共 {{ vulnTotal }} 条，当前第 {{ vulnPage }} 页</span>
                  <div>
                    <v-btn class="mr-2" variant="outlined" :disabled="vulnPage<=1" @click="changePage(vulnPage-1)">上一页</v-btn>
                    <v-btn variant="outlined" :disabled="vulnPage*vulnSize>=vulnTotal" @click="changePage(vulnPage+1)">下一页</v-btn>
                  </div>
                </div>
              </template>
            </v-card-text>
          </v-card>
        </v-window-item>

        <!-- Members Tab -->
        <v-window-item value="members">
            <v-card class="elevation-2">
              <v-card-title>组织成员</v-card-title>
              <v-list>
                <v-list-item
                  v-for="member in members"
                  :key="member.uuid"
                  :title="member.name || member.email"
                  :subtitle="member.role"
                  @click="$router.push({ path: `/users/${member.uuid}/profile`, query: { fromOrg: organization.uuid, tab: 'members' } })"
                  style="cursor:pointer;"
                >
                  <template v-slot:prepend>
                    <AppAvatar :name="member.name || member.email || '?'" :size="40" class="mr-4" />
                  </template>
                  <template v-slot:append>
                    <v-chip size="small" :color="member.role === 'ADMIN' ? 'primary' : 'default'">{{ member.role }}</v-chip>
                    <v-menu v-if="isOrgAdmin && member.role !== 'ADMIN'">
                      <template v-slot:activator="{ props }">
                        <v-btn icon="mdi-dots-vertical" variant="text" v-bind="props"></v-btn>
                      </template>
                      <v-list>
                        <v-list-item @click="openRemoveMember(member)">
                          <v-list-item-title>移除成员</v-list-item-title>
                        </v-list-item>
                      </v-list>
                    </v-menu>
                  </template>
                </v-list-item>
              </v-list>
            </v-card>
          </v-window-item>

        <!-- Invites Tab (admin only) -->
        <v-window-item value="invites">
          <v-card class="elevation-2">
            <v-card-title class="d-flex align-center">组织邀请</v-card-title>
            <v-card-text>
              <div class="d-flex align-center mb-4">
                <v-text-field v-model="inviteLoginOrEmail" label="用户名或邮箱" variant="outlined" class="mr-4" :disabled="inviting" hide-details></v-text-field>
                <v-btn color="primary" :loading="inviting" :disabled="!inviteLoginOrEmail" @click="sendInvite">发送邀请</v-btn>
              </div>

              <v-divider class="my-6"></v-divider>

              <div class="d-flex align-center mb-3">
                <h3 class="text-h6 mr-4">链接邀请 / 邀请码</h3>
                <v-spacer></v-spacer>
                <v-text-field v-model.number="newLinkExpireDays" type="number" min="0" style="max-width:200px" label="过期天数(可选)" variant="outlined" density="compact" hide-details class="mr-3"></v-text-field>
                <v-btn color="primary" :loading="creatingLink" @click="createInviteLink">生成邀请链接</v-btn>
              </div>

              <div v-if="loadingInviteLinks" class="text-center py-8">
                <v-progress-circular indeterminate color="primary"></v-progress-circular>
              </div>
              <template v-else>
                <v-table v-if="inviteLinks.length > 0">
                  <thead>
                    <tr>
                      <th>邀请码</th>
                      <th>创建时间</th>
                      <th>过期时间</th>
                      <th>状态</th>
                      <th>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="l in inviteLinks" :key="l.uuid">
                      <td><code>{{ l.code }}</code></td>
                      <td>{{ formatDateTime(l.createTime) }}</td>
                      <td>{{ l.expireTime ? formatDateTime(l.expireTime) : '-' }}</td>
                      <td>
                        <v-chip :color="inviteLinkStatusColor(l)" size="small" label>{{ inviteLinkStatusText(l) }}</v-chip>
                      </td>
                      <td>
                        <v-btn size="small" variant="text" prepend-icon="mdi-content-copy" @click="copyCode(l.code)">复制</v-btn>
                        <v-btn size="small" color="error" variant="text" :disabled="!l.isActive" @click="revokeInviteLink(l.uuid)">撤销</v-btn>
                      </td>
                    </tr>
                  </tbody>
                </v-table>
                <div v-else class="text-center py-8 text-grey">暂无邀请链接</div>
              </template>

              <div class="d-flex align-center mb-3">
                <v-select
                  v-model="invitesStatus"
                  :items="invitesStatusItems"
                  item-title="label"
                  item-value="value"
                  label="状态"
                  density="compact"
                  style="max-width: 220px"
                  hide-details
                />
              </div>

              <div v-if="loadingInvites" class="text-center py-8">
                <v-progress-circular indeterminate color="primary"></v-progress-circular>
              </div>
              <template v-else>
                <v-table v-if="orgInvites.length > 0">
                  <thead>
                    <tr>
                      <th>被邀请者</th>
                      <th>邀请人</th>
                      <th>状态</th>
                      <th>创建时间</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="it in orgInvites" :key="it.inviteUuid">
                      <td>{{ it.inviteeName || it.inviteeEmail || '-' }}</td>
                      <td>{{ it.inviterName || '-' }}</td>
                      <td>
                        <v-chip :color="inviteStatusColor(it.status)" size="small" label>{{ it.status }}</v-chip>
                      </td>
                      <td>{{ formatDateTime(it.createTime) }}</td>
                    </tr>
                  </tbody>
                </v-table>

                <div v-else class="text-center py-8 text-grey">暂无邀请</div>

                <div class="d-flex align-center justify-space-between mt-4">
                  <span class="text-grey">共 {{ invitesTotal }} 条，当前第 {{ invitesPage }} 页</span>
                  <div>
                    <v-btn class="mr-2" variant="outlined" :disabled="invitesPage<=1" @click="changeInvitesPage(invitesPage-1)">上一页</v-btn>
                    <v-btn variant="outlined" :disabled="invitesPage*invitesSize>=invitesTotal" @click="changeInvitesPage(invitesPage+1)">下一页</v-btn>
                  </div>
                </div>
              </template>
            </v-card-text>
          </v-card>
        </v-window-item>

        <!-- Points Tab (admin only) -->
        <v-window-item value="points">
          <v-card class="elevation-2">
            <v-card-title class="d-flex align-center">
              <span>组织积分流水</span>
              <v-spacer></v-spacer>
              <v-btn size="small" variant="text" @click="fetchOrgPoints" :loading="loadingOrgPoints">刷新</v-btn>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text>
              <div v-if="loadingOrgPoints" class="text-center py-8">
                <v-progress-circular indeterminate color="primary" />
              </div>
              <template v-else>
                <v-table>
                  <thead>
                    <tr>
                      <th>时间</th>
                      <th>变动</th>
                      <th>原因</th>
                      <th>引用</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="it in orgPoints" :key="it.uuid">
                      <td>{{ formatDateTime(it.createdAt) }}</td>
                      <td :class="{'text-green': it.delta>0, 'text-error': it.delta<0}">{{ it.delta }}</td>
                      <td>{{ it.reason }}</td>
                      <td>{{ it.refType }}: {{ it.refId }}</td>
                    </tr>
                  </tbody>
                </v-table>
                <div class="d-flex align-center justify-space-between mt-4">
                  <span class="text-grey">共 {{ orgPointsTotal }} 条，当前第 {{ orgPointsPage }} 页</span>
                  <div>
                    <v-btn variant="outlined" class="mr-2" :disabled="orgPointsPage<=1" @click="changeOrgPointsPage(orgPointsPage-1)">上一页</v-btn>
                    <v-btn variant="outlined" :disabled="orgPointsPage*orgPointsSize>=orgPointsTotal" @click="changeOrgPointsPage(orgPointsPage+1)">下一页</v-btn>
                  </div>
                </div>
              </template>
            </v-card-text>
          </v-card>
        </v-window-item>

        <!-- Join Requests Tab (admin only) -->
        <v-window-item value="join-requests">
          <v-card class="elevation-2">
            <v-card-title class="d-flex align-center">加入申请</v-card-title>
            <v-card-text>
              <div class="d-flex align-center mb-3">
                <v-select
                  v-model="joinReqStatus"
                  :items="joinReqStatusItems"
                  item-title="label"
                  item-value="value"
                  label="状态"
                  density="compact"
                  style="max-width: 220px"
                  hide-details
                />
                <v-chip class="ml-3" color="info" label>
                  当前：{{ (joinReqStatusItems.find(i => i.value===joinReqStatus)?.label) || '全部' }}
                </v-chip>
              </div>
              <div v-if="loadingJoinRequests" class="text-center py-8">
                <v-progress-circular indeterminate color="primary"></v-progress-circular>
              </div>
              <template v-else>
                <v-table v-if="joinRequests.length > 0">
                  <thead>
                    <tr>
                      <th>申请人</th>
                      <th>邮箱</th>
                      <th>备注</th>
                      <th>状态</th>
                      <th>创建时间</th>
                      <th>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="r in joinRequests" :key="r.requestUuid">
                      <td>{{ r.applicantName || '-' }}</td>
                      <td>{{ r.applicantEmail || '-' }}</td>
                      <td class="text-truncate" style="max-width:360px" :title="r.content">{{ r.content || '-' }}</td>
                      <td>
                        <v-chip :color="inviteStatusColor(r.status)" size="small" label>{{ r.status }}</v-chip>
                      </td>
                      <td>{{ formatDateTime(r.createTime) }}</td>
                      <td>
                        <v-btn v-if="r.status==='ACTIVE'" size="small" class="mr-2" color="primary" variant="text" :loading="actingJoinReqId===r.requestUuid" @click="approveJoinReq(r.requestUuid)">通过</v-btn>
                        <v-btn v-if="r.status==='ACTIVE'" size="small" color="error" variant="text" :loading="actingJoinReqId===r.requestUuid" @click="rejectJoinReq(r.requestUuid)">拒绝</v-btn>
                      </td>
                    </tr>
                  </tbody>
                </v-table>
                <div v-else class="text-center py-8 text-grey">暂无申请</div>
                <div class="d-flex align-center justify-space-between mt-4">
                  <span class="text-grey">共 {{ joinReqTotal }} 条，当前第 {{ joinReqPage }} 页</span>
                  <div>
                    <v-btn class="mr-2" variant="outlined" :disabled="joinReqPage<=1" @click="changeJoinReqsPage(joinReqPage-1)">上一页</v-btn>
                    <v-btn variant="outlined" :disabled="joinReqPage*joinReqSize>=joinReqTotal" @click="changeJoinReqsPage(joinReqPage+1)">下一页</v-btn>
                  </div>
                </div>
              </template>
            </v-card-text>
  </v-card>
        </v-window-item>

        <!-- Remove Member Dialog -->
        <v-dialog v-model="showRemoveDialog" max-width="420">
          <v-card>
            <v-card-title class="text-h6">确认移除成员</v-card-title>
            <v-card-text>
              确定要从组织中移除
              <strong>{{ memberToRemove?.name || memberToRemove?.userName || '-' }}</strong>
              吗？此操作不可撤销。
            </v-card-text>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn variant="text" @click="showRemoveDialog=false">取消</v-btn>
              <v-btn color="error" variant="flat" @click="confirmRemoveMember">移除</v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>

        <!-- Settings Tab -->
<v-window-item value="settings">
          <v-card class="elevation-2">
            <v-card-title>组织设置</v-card-title>
            <v-card-text>
              <v-form @submit.prevent="updateOrganization">
                <!-- 组织名称 -->
                <v-text-field
                  v-model="editableOrganization.name"
                  label="组织名称"
                  variant="outlined"
                  class="mb-4"
                ></v-text-field>

                <!-- 组织简介 -->
                <div class="mb-4">
                  <v-textarea
                    v-model="editableOrganization.description"
                    label="组织简介"
                    variant="outlined"
                    rows="2"
                    counter="200"
                    maxlength="200"
                    placeholder="用一两句话简单介绍你的组织..."
                    hint="简短的组织描述，将显示在组织列表和卡片中（最多 200 字）"
                    persistent-hint
                  ></v-textarea>
                </div>

                <!-- 组织详细介绍 (支持 Markdown) -->
                <div class="mb-4">
                  <label class="text-subtitle-2 mb-2 d-block">
                    <v-icon size="small" class="mr-1">mdi-text-box-outline</v-icon>
                    组织详细介绍（支持 Markdown 格式）
                  </label>
                  <div class="text-caption text-grey mb-2">
                    详细介绍你的组织，包括使命、愿景、团队成员、联系方式等。支持 Markdown 格式，可以添加标题、列表、链接、代码块等丰富内容。
                  </div>

                  <v-tabs v-model="freeTextTab" class="mb-2">
                    <v-tab value="edit">
                      <v-icon start>mdi-pencil</v-icon>
                      编辑
                    </v-tab>
                    <v-tab value="preview">
                      <v-icon start>mdi-eye</v-icon>
                      预览
                    </v-tab>
                  </v-tabs>

                  <v-window v-model="freeTextTab">
                    <v-window-item value="edit">
                      <v-textarea
                        v-model="editableOrganization.freeText"
                        variant="outlined"
                        placeholder="# 关于我们&#10;&#10;我们是一个专注于**安全研究**的组织...&#10;&#10;## 我们的使命&#10;&#10;- 发现并报告安全漏洞&#10;- 提升网络安全意识&#10;- 促进安全社区发展"
                        rows="12"
                        hide-details
                      ></v-textarea>
                      <div class="text-caption text-grey mt-1">
                        💡 提示：使用 Markdown 语法让组织介绍更加丰富，支持标题、列表、代码块等格式
                      </div>
                    </v-window-item>
                    <v-window-item value="preview">
                      <v-card variant="outlined" class="pa-4" style="min-height: 300px;">
                        <div v-html="renderedFreeText" class="markdown-body"></div>
                      </v-card>
                    </v-window-item>
                  </v-window>
                </div>

                <!-- 组织权限设置 -->
                <v-divider class="my-6"></v-divider>
                <div class="text-subtitle-2 mb-3">
                  <v-icon size="small" class="mr-1">mdi-shield-lock-outline</v-icon>
                  权限与可见性设置
                </div>
                <div class="mb-4">
                  <v-switch
                    v-model="editableOrganization.isPublic"
                    label="公开可见"
                    color="primary"
                    hide-details
                    class="mb-2"
                  >
                    <template v-slot:label>
                      <div>
                        <div class="font-weight-medium">公开可见</div>
                        <div class="text-caption text-grey">开启后，任何人都可以查看组织信息和公开漏洞</div>
                      </div>
                    </template>
                  </v-switch>

                  <v-switch
                    v-model="editableOrganization.allowJoinRequest"
                    label="允许公开申请加入"
                    color="primary"
                    hide-details
                    class="mb-2"
                  >
                    <template v-slot:label>
                      <div>
                        <div class="font-weight-medium">允许公开申请加入</div>
                        <div class="text-caption text-grey">开启后，用户可以主动申请加入组织，需管理员审批</div>
                      </div>
                    </template>
                  </v-switch>

                  <v-switch
                    v-model="editableOrganization.allowInviteLink"
                    label="允许邀请链接/邀请码"
                    color="primary"
                    hide-details
                  >
                    <template v-slot:label>
                      <div>
                        <div class="font-weight-medium">允许邀请链接/邀请码</div>
                        <div class="text-caption text-grey">开启后，管理员可以生成邀请链接或邀请码邀请成员</div>
                      </div>
                    </template>
                  </v-switch>
                </div>
                <v-btn type="submit" color="primary" :loading="updating">保存更改</v-btn>

                <v-divider class="my-6"></v-divider>
                <div>
                  <div class="text-subtitle-2 mb-2"><v-icon size="small" class="mr-1">mdi-star-circle-outline</v-icon> 组织积分策略（覆盖全局）</div>
                  <div class="text-grey-darken-1 mb-4">仅对本组织生效，未配置项继承全局策略。拒绝扣分已禁用。</div>
                  <v-row>
                    <v-col cols="12" md="6">
                      <div class="text-body-2 font-weight-medium mb-2">事件增量</div>
                      <v-row>
                        <v-col cols="6"><v-text-field v-model.number="orgPointsForm.events.submitted.userDelta" type="number" label="提交-个人" hide-details /></v-col>
                        <v-col cols="6"><v-text-field v-model.number="orgPointsForm.events.submitted.orgDelta" type="number" label="提交-组织" hide-details /></v-col>
                        <v-col cols="6"><v-text-field v-model.number="orgPointsForm.events.published.userDelta" type="number" label="发布-个人" hide-details /></v-col>
                        <v-col cols="6"><v-text-field v-model.number="orgPointsForm.events.published.orgDelta" type="number" label="发布-组织" hide-details /></v-col>
                      </v-row>
                    </v-col>
                    <v-col cols="12" md="6">
                      <div class="text-body-2 font-weight-medium mb-2">严重度加权</div>
                      <v-select :items="severityModes" v-model="orgPointsForm.severity.mode" label="模式" hide-details />
                      <div v-if="orgPointsForm.severity.mode === 'LEVEL_MULTIPLIER'">
                        <v-row>
                          <v-col cols="6"><v-text-field v-model.number="orgPointsForm.severity.levels.critical" type="number" step="0.1" label="CRITICAL 倍率" hide-details /></v-col>
                          <v-col cols="6"><v-text-field v-model.number="orgPointsForm.severity.levels.high" type="number" step="0.1" label="HIGH 倍率" hide-details /></v-col>
                          <v-col cols="6"><v-text-field v-model.number="orgPointsForm.severity.levels.medium" type="number" step="0.1" label="MEDIUM 倍率" hide-details /></v-col>
                          <v-col cols="6"><v-text-field v-model.number="orgPointsForm.severity.levels.low" type="number" step="0.1" label="LOW 倍率" hide-details /></v-col>
                        </v-row>
                      </div>
                      <div v-else-if="orgPointsForm.severity.mode === 'SCORE_LINEAR'">
                        <v-row>
                          <v-col cols="6"><v-text-field v-model.number="orgPointsForm.severity.linear.k" type="number" step="0.1" label="k" hide-details /></v-col>
                          <v-col cols="6"><v-text-field v-model.number="orgPointsForm.severity.linear.b" type="number" step="0.1" label="b" hide-details /></v-col>
                        </v-row>
                      </div>
                    </v-col>
                  </v-row>
                  <div class="d-flex align-center">
                    <v-btn color="primary" class="mr-2" :loading="savingOrgPoints" @click="saveOrgPointsPolicy">保存组织积分策略</v-btn>
                    <v-select class="mr-2" style="max-width: 160px" :items="eventOptions" v-model="orgPreview.event" label="事件" hide-details />
                    <v-text-field class="mr-2" style="max-width: 180px" v-model.number="orgPreview.severityNum" type="number" step="0.1" label="严重度分数(可选)" hide-details />
                    <v-select class="mr-2" style="max-width: 180px" :items="levelOptions" v-model="orgPreview.severityLevel" label="严重度等级(可选)" hide-details />
                    <v-btn variant="outlined" :loading="orgPreviewing" @click="doOrgPreview">预览计算</v-btn>
                    <div class="ml-4 text-grey-darken-2" v-if="orgPreviewResult">结果：个人 {{ orgPreviewResult.userDelta }}，组织 {{ orgPreviewResult.orgDelta }}</div>
                  </div>
                </div>

                <v-divider class="my-6"></v-divider>
                <div>
                  <div class="text-subtitle-2 mb-2"><v-icon size="small" class="mr-1">mdi-alert</v-icon> 风险操作</div>
                  <v-alert type="warning" variant="tonal" class="mb-3">
                    暂停后组织仍可被查看，但不可加入/邀请，也不可用组织身份提交内容；删除后组织不可见且名称可复用。
                  </v-alert>
                  <div class="d-flex align-center">
                    <v-btn v-if="organization.status==='ACTIVE'" color="warning" class="mr-2" @click="confirmSuspend">暂停组织</v-btn>
                    <v-btn v-if="organization.status==='SUSPENDED'" color="primary" class="mr-2" @click="confirmRestore">恢复组织</v-btn>
                    <v-btn v-if="organization.status==='SUSPENDED'" color="error" @click="confirmDelete">删除组织</v-btn>
                  </div>
                </div>
              </v-form>
            </v-card-text>
          </v-card>
        </v-window-item>
      </v-window>
    </div>

    <div v-else class="text-center mt-16">
       <h1 class="text-h4 text-grey">组织未找到</h1>
    </div>

    <!-- Join banner (for invitee) -->
    <v-alert
      v-if="showJoinBanner"
      type="info"
      variant="tonal"
      class="mt-4"
    >
      你被邀请加入组织 <strong>{{ organization.name }}</strong>
      <div class="mt-2">
        <v-btn color="primary" class="mr-2" @click="acceptMyInvite">接受</v-btn>
        <v-btn color="grey" variant="text" @click="rejectMyInvite">拒绝</v-btn>
      </div>
    </v-alert>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" location="top right">
      {{ snackbar.text }}
    </v-snackbar>

    <!-- 公开申请加入 对话框 -->
    <v-dialog v-model="showJoinDialog" max-width="520">
      <v-card>
        <v-card-title class="text-h6">申请加入组织</v-card-title>
        <v-card-text>
          <v-textarea v-model="joinMessage" label="备注（可选）" variant="outlined" hide-details rows="3"></v-textarea>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="showJoinDialog = false">取消</v-btn>
          <v-btn color="primary" @click="submitJoinByPublic">提交申请</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import organizationApi from '@/api/organization';
import pointsApi from '@/api/points';
import { useAuthStore } from '@/stores/auth';
import { storeToRefs } from 'pinia';
import vulnerabilityApi from '@/api/vulnerability';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import AppAvatar from '@/components/AppAvatar.vue';

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const updating = ref(false);
const organization = ref(null);
const members = ref([]);
const editableOrganization = ref({});
const inviteLoginOrEmail = ref('');
const inviting = ref(false);
const showJoinDialog = ref(false);
const joinMessage = ref('');
const freeTextTab = ref('preview'); // 'edit' or 'preview' for Markdown editor - 默认预览
// Admin invites list state
const orgInvites = ref([]);
const invitesPage = ref(1);
const invitesSize = ref(10);
const invitesTotal = ref(0);
const invitesStatus = ref('ALL');
const invitesStatusItems = [
  { label: '全部', value: 'ALL' },
  { label: '待处理', value: 'ACTIVE' },
  { label: '已接受', value: 'ACCEPTED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已过期', value: 'EXPIRED' },
];
const loadingInvites = ref(false);
// Vulns state
const vulns = ref([]);
const vulnTotal = ref(0);
const vulnPage = ref(1);
const vulnSize = ref(10);
const vulnSortBy = ref('modified');
const vulnSortOrder = ref('desc');
const loadingVulns = ref(false);
const sortByItems = [
  { label: '按修改时间', value: 'modified' },
  { label: '按严重度', value: 'severity' },
];
const sortOrderItems = [
  { label: '降序', value: 'desc' },
  { label: '升序', value: 'asc' },
];
const tab = ref((route.query && route.query.tab) ? String(route.query.tab) : 'overview');
const snackbar = reactive({
  show: false,
  text: '',
  color: 'success'
});
const disbanding = ref(false);
const orgPointsSummary = ref({ rating: 0, rank: null });

// ===== Org points settings (override) =====
const severityModes = ['NONE', 'LEVEL_MULTIPLIER', 'SCORE_LINEAR'];
const eventOptions = ['SUBMITTED', 'PUBLISHED'];
const levelOptions = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'];
const orgPointsForm = ref({
  events: {
    submitted: { userDelta: 0, orgDelta: 2 },
    published: { userDelta: 5, orgDelta: 10 },
  },
  severity: {
    mode: 'LEVEL_MULTIPLIER',
    levels: { critical: 2.0, high: 1.5, medium: 1.0, low: 0.5 },
    linear: { k: 1.0, b: 0.0 },
  }
});
const savingOrgPoints = ref(false);
const orgPreview = ref({ event: 'SUBMITTED', severityNum: null, severityLevel: null });
const orgPreviewing = ref(false);
const orgPreviewResult = ref(null);

// (removed duplicate declarations)
const fetchOrgPoints = async () => {
  if (!isOrgAdmin.value || !organization.value?.uuid) return;
  loadingOrgPoints.value = true;
  try {
    const resp = await pointsApi.getOrgPoints(organization.value.uuid, { page: orgPointsPage.value, size: orgPointsSize.value });
    if (resp.data && resp.data.code === 0 && resp.data.data) {
      orgPoints.value = resp.data.data.items || [];
      orgPointsTotal.value = resp.data.data.total || 0;
    }
  } catch (_) {}
  finally { loadingOrgPoints.value = false; }
};
const changeOrgPointsPage = (p) => { orgPointsPage.value = p; fetchOrgPoints(); };

const authStore = useAuthStore();
const { user: currentUser } = storeToRefs(authStore);
const isOrgAdmin = computed(() => {
  // Prefer organization.role if present; otherwise infer from membership
  if (organization.value && organization.value.role) return organization.value.role === 'ADMIN';
  const myUuid = currentUser.value?.uuid;
  if (!myUuid) return false;
  const me = members.value.find(m => m.uuid === myUuid);
  return !!me && me.role === 'ADMIN';
});
const isMember = computed(() => {
  const myUuid = currentUser.value?.uuid;
  if (!myUuid) return false;
  return !!members.value.find(m => m.uuid === myUuid);
});
const pendingInviteForMe = ref(null);
const showJoinBanner = computed(() => !isMember.value && !!pendingInviteForMe.value);
const showApplyJoin = computed(() => !isMember.value && !!organization.value?.isPublic && !!organization.value?.allowJoinRequest);

// Markdown rendering for organization freeText (in settings)
const renderedFreeText = computed(() => {
  const text = editableOrganization.value?.freeText || '';
  if (!text.trim()) {
    return '<p class="text-grey">暂无组织介绍</p>';
  }
  try {
    const rawHtml = marked.parse(text);
    const cleanHtml = DOMPurify.sanitize(rawHtml, {
      ALLOWED_TAGS: [
        'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
        'p', 'br', 'hr',
        'strong', 'em', 'del', 'code', 'pre',
        'ul', 'ol', 'li',
        'a', 'img',
        'blockquote',
        'table', 'thead', 'tbody', 'tr', 'th', 'td'
      ],
      ALLOWED_ATTR: ['href', 'src', 'alt', 'title', 'class', 'target', 'rel']
    });
    return cleanHtml;
  } catch (error) {
    console.error('❌ [Settings] Markdown rendering error:', error);
    return '<p class="text-grey">Markdown 渲染错误</p>';
  }
});

// Markdown rendering for organization freeText (in overview)
const renderedOrgFreeText = computed(() => {
  const text = organization.value?.freeText || '';
  if (!text.trim()) {
    return '';
  }
  try {
    const rawHtml = marked.parse(text);
    const cleanHtml = DOMPurify.sanitize(rawHtml, {
      ALLOWED_TAGS: [
        'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
        'p', 'br', 'hr',
        'strong', 'em', 'del', 'code', 'pre',
        'ul', 'ol', 'li',
        'a', 'img',
        'blockquote',
        'table', 'thead', 'tbody', 'tr', 'th', 'td'
      ],
      ALLOWED_ATTR: ['href', 'src', 'alt', 'title', 'class', 'target', 'rel']
    });
    return cleanHtml;
  } catch (error) {
    console.error('❌ [Overview] Markdown rendering error:', error);
    return '<p class="text-grey">Markdown 渲染错误</p>';
  }
});

const fetchOrganizationDetails = async () => {
  loading.value = true;
  const orgUuid = route.params.id;
  try {
    const orgResponse = await organizationApi.getByUuid(orgUuid);
    console.log('📥 [Fetch] Organization response:', orgResponse.data);
    if (orgResponse.data && orgResponse.data.code === 0) {
      organization.value = orgResponse.data.data.organization || orgResponse.data.data;
      console.log('✅ [Fetch] Organization data:', organization.value);
      console.log('🔍 [Fetch] freeText:', organization.value.freeText);
      editableOrganization.value = { ...organization.value };
      console.log('✅ [Fetch] editableOrganization:', editableOrganization.value);
      try { orgPointsSummary.value = await pointsApi.getOrgSummary(organization.value.uuid); } catch (_) {}
      await loadOrgPointsPolicy();
      // 加载组织积分策略覆盖
      await loadOrgPointsPolicy();
    } else {
      showSnackbar(orgResponse.data.message || '获取组织详情失败', 'error');
      return;
    }

    // Fetch members according to visibility and membership
    try {
      // If member/admin, load full members. Else if public, load public members. Else skip.
      const myUuid = currentUser.value?.uuid;
      if (myUuid) {
        // Try full list; if 403 fallback to public list when org is public
        const respMembers = await organizationApi.getMembers(orgUuid);
        if (respMembers.data && respMembers.data.code === 0) {
          members.value = respMembers.data.data.items || [];
        }
      }
    } catch (err) {
      const status = err?.response?.status;
      if (status === 403 && organization.value?.isPublic) {
        try {
          const pm = await organizationApi.getPublicMembers(orgUuid);
          if (pm.data && pm.data.code === 0) members.value = pm.data.data.items || [];
        } catch (_) { /* ignore */ }
      }
      // Otherwise ignore member errors silently for non-members/private orgs
    }

  } catch (error) {
    console.error('Failed to fetch organization details:', error);
    const errorMessage = error.response?.data?.message || '获取数据失败';
    showSnackbar(errorMessage, 'error');
  } finally {
    loading.value = false;
  }
};

const fetchVulns = async () => {
  if (!organization.value?.uuid) return;
  loadingVulns.value = true;
  try {
    const resp = await vulnerabilityApi.list({
      organizationUuid: organization.value.uuid,
      page: vulnPage.value,
      size: vulnSize.value,
      sortBy: vulnSortBy.value,
      sortOrder: vulnSortOrder.value,
      withTotal: true,
    });
    if (resp.data && resp.data.code === 0) {
      vulns.value = resp.data.data.items || [];
      vulnTotal.value = resp.data.data.total ?? (vulns.value.length);
    } else {
      showSnackbar(resp.data.message || '获取漏洞列表失败', 'error');
    }
  } catch (err) {
    showSnackbar(err.response?.data?.message || '获取漏洞列表失败', 'error');
  } finally {
    loadingVulns.value = false;
  }
};

const changePage = (p) => { vulnPage.value = p; fetchVulns(); };
const goVuln = (uuid) => { router.push('/vulnerabilities/' + uuid); };
const formatDateTime = (iso) => {
  if (!iso) return '-';
  try { return new Date(iso).toLocaleString('zh-CN'); } catch { return iso; }
};
const formatSeverity = (s) => (s == null ? '-' : s.toFixed(1));
const severityToColor = (s) => {
  if (s == null) return 'grey';
  if (s >= 9.0) return 'red-darken-2';
  if (s >= 7.0) return 'orange-darken-2';
  if (s >= 4.0) return 'amber-darken-2';
  return 'green-darken-2';
};

// (removed duplicate functions)

// Organization status color mapping
const orgStatusColor = (st) => {
  switch (st) {
    case 'ACTIVE': return 'success';
    case 'SUSPENDED': return 'warning';
    case 'BANNED': return 'error';
    case 'PENDING': return 'warning';
    case 'REJECTED': return 'error';
    case 'DELETED': return 'grey';
    default: return 'grey';
  }
};

// Remove member dialog state
const showRemoveDialog = ref(false);
const memberToRemove = ref(null);

const openRemoveMember = (member) => {
  memberToRemove.value = member;
  showRemoveDialog.value = true;
};

const confirmRemoveMember = async () => {
  if (!memberToRemove.value) { showRemoveDialog.value = false; return; }
  await removeMember(memberToRemove.value);
  showRemoveDialog.value = false;
  memberToRemove.value = null;
};

const removeMember = async (member) => {
  try {
    const orgUuid = organization.value.uuid;
    await organizationApi.removeMember(orgUuid, member.uuid);
    showSnackbar('成员移除成功', 'success');
    // Refresh member list
    fetchOrganizationDetails();
  } catch (error) {
    console.error('Failed to remove member:', error);
    const errorMessage = error.response?.data?.message || '移除成员失败';
    showSnackbar(errorMessage, 'error');
  }
};

const updateOrganization = async () => {
  updating.value = true;
  try {
    const orgUuid = organization.value.uuid;
    const updateData = {
      name: editableOrganization.value.name,
      description: editableOrganization.value.description,
      freeText: editableOrganization.value.freeText,
      isPublic: editableOrganization.value.isPublic,
      allowJoinRequest: editableOrganization.value.allowJoinRequest,
      allowInviteLink: editableOrganization.value.allowInviteLink,
    };
    console.log('📤 [Update] Sending data:', updateData);
    const response = await organizationApi.update(orgUuid, updateData);
    console.log('📥 [Update] Response:', response.data);
    if (response.data && response.data.code === 0) {
      const updated = response.data.data.organization || response.data.data;
      console.log('✅ [Update] Updated organization:', updated);
      console.log('🔍 [Update] freeText in response:', updated.freeText);
      organization.value = updated;
      editableOrganization.value = { ...updated };
      console.log('✅ [Update] editableOrganization after update:', editableOrganization.value);
      showSnackbar('组织信息更新成功', 'success');
    } else {
      showSnackbar(response.data.message || '更新失败', 'error');
    }
  } catch (error) {
    console.error('❌ [Update] Failed to update organization:', error);
    const errorMessage = error.response?.data?.message || '更新失败';
    showSnackbar(errorMessage, 'error');
  } finally {
    updating.value = false;
  }
};

// === Suspend/Restore/Delete ===
const confirmSuspend = async () => {
  if (!isOrgAdmin.value) { showSnackbar('只有管理员可以暂停组织', 'error'); return; }
  try {
    const resp = await organizationApi.suspend(organization.value.uuid);
    if (resp.data && resp.data.code === 0) { showSnackbar('组织已暂停', 'success'); fetchOrganizationDetails(); }
    else { showSnackbar(resp.data?.message || '操作失败', 'error'); }
  } catch (err) { showSnackbar(err.response?.data?.message || '操作失败', 'error'); }
};

const confirmRestore = async () => {
  if (!isOrgAdmin.value) { showSnackbar('只有管理员可以恢复组织', 'error'); return; }
  try {
    const resp = await organizationApi.restore(organization.value.uuid);
    if (resp.data && resp.data.code === 0) { showSnackbar('组织已恢复', 'success'); fetchOrganizationDetails(); }
    else { showSnackbar(resp.data?.message || '操作失败', 'error'); }
  } catch (err) { showSnackbar(err.response?.data?.message || '操作失败', 'error'); }
};

const confirmDelete = async () => {
  if (!isOrgAdmin.value) { showSnackbar('只有管理员可以删除组织', 'error'); return; }
  const name = organization.value?.name || '';
  const input = window.prompt(`删除组织将不可恢复，且名称可被复用。\n请输入组织名称以确认删除：`);
  if (!input || input.trim() !== name) { showSnackbar('名称不匹配，已取消', 'error'); return; }
  try {
    const resp = await organizationApi.delete(organization.value.uuid);
    if (resp.data && resp.data.code === 0) {
      showSnackbar('组织已删除', 'success');
      router.push('/organizations');
    } else { showSnackbar(resp.data?.message || '操作失败', 'error'); }
  } catch (err) { showSnackbar(err.response?.data?.message || '操作失败', 'error'); }
};

onMounted(() => {
  fetchOrganizationDetails();
  // 概览页面也需要显示漏洞数据
  if (tab.value === 'overview') {
    fetchVulns();
  }
});

// Load tab-specific data
watch(() => tab.value, (t) => {
  if (t === 'overview' || t === 'vulns') fetchVulns();
  if (t === 'invites') { fetchOrgInvitations(); fetchInviteLinks(); }
  if (t === 'join-requests') fetchJoinRequests();
  if (t === 'points') fetchOrgPoints();
});
// After org details loaded, check pending invite for me
watch(organization, (o) => { if (o?.uuid) checkMyPendingInvite(); });

const showSnackbar = (text, color = 'success') => {
  snackbar.text = text;
  snackbar.color = color;
  snackbar.show = true;
};

const sendInvite = async () => {
  if (!isOrgAdmin.value) {
    showSnackbar('只有管理员可以邀请成员', 'error');
    return;
  }
  if (!inviteLoginOrEmail.value) return;
  inviting.value = true;
  try {
    const orgUuid = organization.value.uuid;
    const resp = await organizationApi.invite(orgUuid, inviteLoginOrEmail.value);
    if (resp.data && resp.data.code === 0) {
      showSnackbar('邀请已发送', 'success');
      inviteLoginOrEmail.value = '';
      // refresh invites list if on invites tab
      if (tab.value === 'invites') fetchOrgInvitations();
    } else {
      showSnackbar(resp.data.message || '邀请失败', 'error');
    }
  } catch (err) {
    showSnackbar(err.response?.data?.message || '邀请失败', 'error');
  } finally {
    inviting.value = false;
  }
};

const inviteStatusColor = (status) => {
  switch ((status || '').toUpperCase()) {
    case 'ACTIVE': return 'info';
    case 'ACCEPTED': return 'success';
    case 'REJECTED': return 'error';
    case 'EXPIRED': return 'grey';
    default: return 'grey';
  }
};

const fetchOrgInvitations = async () => {
  if (!isOrgAdmin.value || !organization.value?.uuid) return;
  loadingInvites.value = true;
  try {
    const params = {
      page: invitesPage.value,
      size: invitesSize.value,
      withTotal: true,
    };
    if (invitesStatus.value && invitesStatus.value !== 'ALL') params.status = invitesStatus.value;
    const resp = await organizationApi.orgInvitations(organization.value.uuid, params);
    if (resp.data && resp.data.code === 0) {
      orgInvites.value = resp.data.data.items || [];
      invitesTotal.value = resp.data.data.total ?? (orgInvites.value.length);
    } else {
      showSnackbar(resp.data.message || '获取邀请列表失败', 'error');
    }
  } catch (err) {
    showSnackbar(err.response?.data?.message || '获取邀请列表失败', 'error');
  } finally {
    loadingInvites.value = false;
  }
};

const changeInvitesPage = (p) => { invitesPage.value = p; fetchOrgInvitations(); };
watch(invitesStatus, () => { invitesPage.value = 1; fetchOrgInvitations(); });

const checkMyPendingInvite = async () => {
  try {
    const resp = await organizationApi.myInvitations({ orgUuid: organization.value.uuid, status: 'ACTIVE' });
    if (resp.data && resp.data.code === 0) {
      const items = resp.data.data.items || [];
      pendingInviteForMe.value = items.length > 0 ? items[0] : null;
    }
  } catch (_) { /* silent */ }
};

const acceptMyInvite = async () => {
  if (!pendingInviteForMe.value) return;
  try {
    const resp = await organizationApi.acceptInvite(pendingInviteForMe.value.inviteUuid);
    if (resp.data && resp.data.code === 0) {
      showSnackbar('已接受邀请', 'success');
      pendingInviteForMe.value = null;
      fetchOrganizationDetails();
      if (tab.value === 'invites') fetchOrgInvitations();
    } else {
      showSnackbar(resp.data.message || '操作失败', 'error');
    }
  } catch (err) {
    showSnackbar(err.response?.data?.message || '操作失败', 'error');
  }
};

const rejectMyInvite = async () => {
  if (!pendingInviteForMe.value) return;
  try {
    const resp = await organizationApi.rejectInvite(pendingInviteForMe.value.inviteUuid);
    if (resp.data && resp.data.code === 0) {
      showSnackbar('已拒绝邀请', 'success');
      pendingInviteForMe.value = null;
      if (tab.value === 'invites') fetchOrgInvitations();
    } else {
      showSnackbar(resp.data.message || '操作失败', 'error');
    }
  } catch (err) {
    showSnackbar(err.response?.data?.message || '操作失败', 'error');
  }
};

const confirmDisband = async () => {
  if (!organization.value?.uuid) return;
  if (!isOrgAdmin.value) { showSnackbar('只有管理员可以解散组织', 'error'); return; }
  if (!confirm('确认解散该组织？此操作将暂停组织并撤销邀请链接，且不可轻易恢复。')) return;
  disbanding.value = true;
  try {
    const resp = await organizationApi.disband(organization.value.uuid);
    if (resp.data && resp.data.code === 0) {
      showSnackbar('组织已解散', 'success');
      router.push('/organizations');
    } else {
      showSnackbar(resp.data?.message || '操作失败', 'error');
    }
  } catch (err) {
    showSnackbar(err.response?.data?.message || '操作失败', 'error');
  } finally {
    disbanding.value = false;
  }
};

// ===== Invite Links state & actions =====
const inviteLinks = ref([]);
const loadingInviteLinks = ref(false);
const creatingLink = ref(false);
const newLinkExpireDays = ref(null);

const fetchInviteLinks = async () => {
  if (!isOrgAdmin.value || !organization.value?.uuid) return;
  loadingInviteLinks.value = true;
  try {
    const resp = await organizationApi.listInviteLinks(organization.value.uuid);
    if (resp.data && resp.data.code === 0) {
      inviteLinks.value = resp.data.data.items || [];
    }
  } catch (_) {}
  finally { loadingInviteLinks.value = false; }
};

const createInviteLink = async () => {
  if (!isOrgAdmin.value || !organization.value?.uuid) return;
  creatingLink.value = true;
  try {
    const resp = await organizationApi.createInviteLink(organization.value.uuid, { expiresInDays: newLinkExpireDays.value });
    if (resp.data && resp.data.code === 0) {
      showSnackbar('邀请链接已生成', 'success');
      newLinkExpireDays.value = null;
      fetchInviteLinks();
    } else {
      showSnackbar(resp.data?.message || '生成失败', 'error');
    }
  } catch (err) {
    showSnackbar(err.response?.data?.message || '生成失败', 'error');
  } finally {
    creatingLink.value = false;
  }
};

const revokeInviteLink = async (uuid) => {
  try {
    const resp = await organizationApi.revokeInviteLink(uuid);
    if (resp.data && resp.data.code === 0) {
      showSnackbar('已撤销邀请链接', 'success');
      fetchInviteLinks();
    } else {
      showSnackbar(resp.data?.message || '操作失败', 'error');
    }
  } catch (err) {
    showSnackbar(err.response?.data?.message || '操作失败', 'error');
  }
};

const copyCode = async (code) => {
  try { await navigator.clipboard.writeText(String(code || '')); showSnackbar('已复制邀请码'); } catch (_) { showSnackbar('复制失败', 'error'); }
};

const inviteLinkStatusText = (l) => {
  const expired = l.expireTime && new Date(l.expireTime) < new Date();
  if (expired) return 'EXPIRED';
  return l.isActive ? 'ACTIVE' : 'REVOKED';
};
const inviteLinkStatusColor = (l) => {
  const t = inviteLinkStatusText(l);
  if (t === 'ACTIVE') return 'info';
  if (t === 'EXPIRED') return 'grey';
  return 'error';
};

const loadOrgPointsPolicy = async () => {
  if (!isOrgAdmin.value || !organization.value?.uuid) return;
  try {
    const s = await organizationApi.getPointsPolicy(organization.value.uuid);
    orgPointsForm.value = {
      events: {
        submitted: { userDelta: s?.events?.submitted?.userDelta ?? 0, orgDelta: s?.events?.submitted?.orgDelta ?? 2 },
        published: { userDelta: s?.events?.published?.userDelta ?? 5, orgDelta: s?.events?.published?.orgDelta ?? 10 },
      },
      severity: {
        mode: s?.severity?.mode || 'LEVEL_MULTIPLIER',
        levels: {
          critical: s?.severity?.levels?.critical ?? 2.0,
          high: s?.severity?.levels?.high ?? 1.5,
          medium: s?.severity?.levels?.medium ?? 1.0,
          low: s?.severity?.levels?.low ?? 0.5,
        },
        linear: {
          k: s?.severity?.linear?.k ?? 1.0,
          b: s?.severity?.linear?.b ?? 0.0,
        },
      },
    };
  } catch (_) {}
};

const saveOrgPointsPolicy = async () => {
  if (!isOrgAdmin.value || !organization.value?.uuid) return;
  savingOrgPoints.value = true;
  try {
    await organizationApi.updatePointsPolicy(organization.value.uuid, orgPointsForm.value);
    showSnackbar('组织积分策略已保存');
  } catch (e) {
    const status = e?.response?.status;
    if (status === 403) showSnackbar('只有组织管理员可修改组织积分策略', 'error');
    else showSnackbar(e?.response?.data?.message || '保存失败', 'error');
  } finally { savingOrgPoints.value = false; }
};

const doOrgPreview = async () => {
  if (!isOrgAdmin.value || !organization.value?.uuid) return;
  orgPreviewing.value = true;
  orgPreviewResult.value = null;
  try {
    orgPreviewResult.value = await organizationApi.previewPointsPolicy(organization.value.uuid, { ...orgPreview.value });
  } catch (e) {
    const status = e?.response?.status;
    if (status === 403) showSnackbar('只有组织管理员可预览组织积分策略', 'error');
  }
  finally { orgPreviewing.value = false; }
};

// ===== Join Requests state & actions =====
const joinRequests = ref([]);
const joinReqPage = ref(1);
const joinReqSize = ref(10);
const joinReqTotal = ref(0);
const joinReqStatus = ref('ACTIVE');
const joinReqStatusItems = [
  { label: '待处理', value: 'ACTIVE' },
  { label: '已接受', value: 'ACCEPTED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '全部', value: 'ALL' },
];
const loadingJoinRequests = ref(false);
const actingJoinReqId = ref(null);

const fetchJoinRequests = async () => {
  if (!isOrgAdmin.value || !organization.value?.uuid) return;
  loadingJoinRequests.value = true;
  try {
    const params = { page: joinReqPage.value, size: joinReqSize.value };
    if (joinReqStatus.value && joinReqStatus.value !== 'ALL') params.status = joinReqStatus.value;
    const resp = await organizationApi.listJoinRequests(organization.value.uuid, params);
    if (resp.data && resp.data.code === 0) {
      joinRequests.value = resp.data.data.items || [];
      joinReqTotal.value = resp.data.data.total ?? (joinRequests.value.length);
    } else {
      showSnackbar(resp.data?.message || '获取加入申请失败', 'error');
    }
  } catch (err) {
    showSnackbar(err.response?.data?.message || '获取加入申请失败', 'error');
  } finally { loadingJoinRequests.value = false; }
};

const changeJoinReqsPage = (p) => { joinReqPage.value = p; fetchJoinRequests(); };
watch(joinReqStatus, () => { joinReqPage.value = 1; fetchJoinRequests(); });

const approveJoinReq = async (uuid) => {
  actingJoinReqId.value = uuid;
  try {
    const resp = await organizationApi.approveJoinRequest(uuid);
    if (resp.data && resp.data.code === 0) {
      showSnackbar('已通过申请', 'success');
      fetchJoinRequests();
      fetchOrganizationDetails();
    } else {
      showSnackbar(resp.data?.message || '操作失败', 'error');
    }
  } catch (err) {
    showSnackbar(err.response?.data?.message || '操作失败', 'error');
  } finally { actingJoinReqId.value = null; }
};

const rejectJoinReq = async (uuid) => {
  actingJoinReqId.value = uuid;
  try {
    const resp = await organizationApi.rejectJoinRequest(uuid);
    if (resp.data && resp.data.code === 0) {
      showSnackbar('已拒绝申请', 'success');
      fetchJoinRequests();
    } else {
      showSnackbar(resp.data?.message || '操作失败', 'error');
    }
  } catch (err) {
    showSnackbar(err.response?.data?.message || '操作失败', 'error');
  } finally { actingJoinReqId.value = null; }
};

// Apply join dialog submit (public org + allowJoinRequest)
const submitJoinByPublic = async () => {
  try {
    const resp = await organizationApi.submitJoinRequest(organization.value.uuid, joinMessage.value || undefined);
    if (resp.data && resp.data.code === 0) {
      showSnackbar('申请已提交，等待管理员审批', 'success');
      showJoinDialog.value = false;
      joinMessage.value = '';
    } else {
      showSnackbar(resp.data?.message || '提交失败', 'error');
    }
  } catch (err) {
    showSnackbar(err.response?.data?.message || '提交失败', 'error');
  }
};
</script>

<style scoped>
/* Markdown 预览样式 */
.markdown-body {
  line-height: 1.6;
  color: #24292f;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-body :deep(h1) {
  font-size: 2em;
  border-bottom: 1px solid #d0d7de;
  padding-bottom: 0.3em;
}

.markdown-body :deep(h2) {
  font-size: 1.5em;
  border-bottom: 1px solid #d0d7de;
  padding-bottom: 0.3em;
}

.markdown-body :deep(h3) {
  font-size: 1.25em;
}

.markdown-body :deep(p) {
  margin-bottom: 16px;
}

.markdown-body :deep(code) {
  background-color: rgba(175, 184, 193, 0.2);
  padding: 0.2em 0.4em;
  border-radius: 6px;
  font-size: 85%;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

.markdown-body :deep(pre) {
  background-color: #f6f8fa;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
  margin-bottom: 16px;
}

.markdown-body :deep(pre code) {
  background-color: transparent;
  padding: 0;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin-bottom: 16px;
  padding-left: 2em;
}

.markdown-body :deep(li) {
  margin-bottom: 4px;
}

.markdown-body :deep(a) {
  color: #0969da;
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.markdown-body :deep(blockquote) {
  border-left: 4px solid #d0d7de;
  padding-left: 16px;
  color: #57606a;
  margin-bottom: 16px;
}

.markdown-body :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 16px;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid #d0d7de;
  padding: 8px 12px;
}

.markdown-body :deep(th) {
  background-color: #f6f8fa;
  font-weight: 600;
}

.markdown-body :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
}

.markdown-body :deep(strong) {
  font-weight: 600;
}

.markdown-body :deep(em) {
  font-style: italic;
}

.markdown-body :deep(del) {
  text-decoration: line-through;
}
</style>
