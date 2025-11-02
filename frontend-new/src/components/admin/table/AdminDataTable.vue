<template>
  <v-data-table-server
    :headers="headers"
    :items="items"
    :items-length="total"
    :loading="loading"
    :page="page"
    :items-per-page="size"
    :sort-by="sortByInternal"
    :density="density"
    @update:page="onUpdatePage"
    @update:items-per-page="onUpdateSize"
    @update:sort-by="onUpdateSort"
    class="admin-data-table elevation-2"
    hover
  >
    <!-- Forward item.<key> slots only -->
    <template v-for="header in headersWithSlots" :key="header.key" #[`item.${header.key}`]="slotProps">
      <slot :name="`item.${header.key}`" v-bind="slotProps || {}" />
    </template>

    <!-- Optional known slots -->
    <template v-if="$slots['no-data']" #no-data="slotProps">
      <slot name="no-data" v-bind="slotProps || {}" />
    </template>
    <template v-if="$slots.top" #top="slotProps">
      <slot name="top" v-bind="slotProps || {}" />
    </template>
    <template v-if="$slots.bottom" #bottom="slotProps">
      <slot name="bottom" v-bind="slotProps || {}" />
    </template>
  </v-data-table-server>
</template>

<script setup>
import { computed, useSlots } from 'vue';

const props = defineProps({
  headers: { type: Array, required: true },
  items: { type: Array, required: true },
  total: { type: Number, required: true },
  loading: { type: Boolean, default: false },
  page: { type: Number, default: 1 }, // 1-based
  size: { type: Number, default: 20 },
  sortBy: { type: Array, default: () => [] }, // [{key, order}]
  density: { type: String, default: 'comfortable' },
});

const emit = defineEmits(['update:page', 'update:size', 'update:sortBy']);

const sortByInternal = computed(() => props.sortBy);

const slots = useSlots();

// 计算属性：只返回有对应插槽的 headers
const headersWithSlots = computed(() => {
  return props.headers.filter(header => {
    const slotName = `item.${header.key}`;
    return !!slots[slotName];
  });
});

function onUpdatePage(p) { emit('update:page', p); }
function onUpdateSize(s) { emit('update:size', s); }
function onUpdateSort(sb) { emit('update:sortBy', sb); }
</script>

<style scoped>
.admin-data-table {
  border-radius: 12px;
  overflow: hidden;
  background: white;
}

:deep(.v-data-table__thead) {
  background: linear-gradient(to bottom, #f8f9fa, #f1f3f5);
}

:deep(.v-data-table__thead th) {
  font-weight: 600 !important;
  color: #495057 !important;
  text-transform: uppercase;
  font-size: 0.75rem !important;
  letter-spacing: 0.5px;
  border-bottom: 2px solid #dee2e6 !important;
}

:deep(.v-data-table__tr:hover) {
  background-color: #f8f9fa !important;
}

:deep(.v-data-table__td) {
  padding: 12px 16px !important;
}

:deep(.v-data-table-footer) {
  border-top: 1px solid #dee2e6;
  background: #fafbfc;
}
</style>
