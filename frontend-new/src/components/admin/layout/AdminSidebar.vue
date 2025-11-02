<template>
  <div class="pa-3 admin-sidebar" :class="{ 'is-collapsed': collapsed }">
    <div class="text-h6 font-weight-bold mb-3 d-flex align-center justify-space-between">
      <div class="d-flex align-center">
        <v-icon size="24" color="primary" class="mr-2">mdi-shield-crown-outline</v-icon>
        <span v-if="!collapsed">管理员中心</span>
      </div>
      <v-btn icon size="small" variant="text" @click="$emit('toggle')">
        <v-icon>{{ collapsed ? 'mdi-chevron-right' : 'mdi-chevron-left' }}</v-icon>
      </v-btn>
    </div>
    <v-divider class="mb-2"></v-divider>
    <v-list nav density="compact">
      <SidebarItem :to="{ name: 'admin-users' }" icon="mdi-account-multiple-outline" label="用户管理" :collapsed="collapsed" />
      <SidebarItem :to="{ name: 'admin-organizations' }" icon="mdi-account-group-outline" label="组织管理" :collapsed="collapsed" />
      <SidebarItem :to="{ name: 'admin-vulnerabilities' }" icon="mdi-bug-outline" label="漏洞管理" :collapsed="collapsed" />
      <SidebarItem :to="{ name: 'admin-vuln-review' }" icon="mdi-clipboard-check-outline" label="漏洞审核" :collapsed="collapsed" />
      <SidebarItem :to="{ name: 'admin-categories' }" icon="mdi-shape-outline" label="分类管理" :collapsed="collapsed" />
      <SidebarItem :to="{ name: 'admin-tags' }" icon="mdi-tag-outline" label="标签管理" :collapsed="collapsed" />
      <SidebarItem :to="{ name: 'admin-settings' }" icon="mdi-cog-outline" label="系统设置" :collapsed="collapsed" />
    </v-list>
  </div>
  
</template>

<script setup>
import { defineProps, defineComponent, h, resolveComponent } from 'vue';

defineProps({
  collapsed: { type: Boolean, default: false }
});

// 正确定义 SidebarItem 组件
const SidebarItem = defineComponent({
  name: 'SidebarItem',
  props: {
    to: { type: Object, required: true },
    icon: { type: String, required: true },
    label: { type: String, required: true },
    collapsed: { type: Boolean, default: false }
  },
  setup(props) {
    return () => {
      const VTooltip = resolveComponent('VTooltip');
      const VListItem = resolveComponent('VListItem');
      const VIcon = resolveComponent('VIcon');
      const VListItemTitle = resolveComponent('VListItemTitle');

      if (props.collapsed) {
        return h(
          VTooltip,
          { text: props.label, location: 'end' },
          {
            activator: ({ props: tooltipProps }) =>
              h(
                VListItem,
                { ...tooltipProps, to: props.to, rounded: 'lg', class: 'mb-1' },
                {
                  prepend: () => h(VIcon, { icon: props.icon }),
                }
              ),
          }
        );
      } else {
        return h(
          VListItem,
          { to: props.to, rounded: 'lg', class: 'mb-1' },
          {
            prepend: () => h(VIcon, { class: 'mr-2', icon: props.icon }),
            default: () => h(VListItemTitle, null, { default: () => props.label }),
          }
        );
      }
    };
  }
});
</script>

<style scoped>
.admin-sidebar.is-collapsed {
  padding-left: 8px !important;
  padding-right: 8px !important;
}
</style>
