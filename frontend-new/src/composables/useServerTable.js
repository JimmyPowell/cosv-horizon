import { ref } from 'vue';

// Generic server-side table state and fetch handler
export function useServerTable(fetcher) {
  const loading = ref(false);
  const items = ref([]);
  const total = ref(0);
  const page = ref(1); // 1-based
  const size = ref(20);
  const sortBy = ref([]); // [{ key, order }]

  async function fetch(params = {}) {
    loading.value = true;
    try {
      const sort = sortBy.value?.[0];
      const resp = await fetcher({
        page: params.page ?? page.value,
        size: params.size ?? size.value,
        sortBy: sort?.key,
        sortOrder: sort?.order,
        ...params,
      });
      items.value = resp.items || [];
      total.value = typeof resp.total === 'number' ? resp.total : 0;
      if (params.page) page.value = params.page;
      if (params.size) size.value = params.size;
      if (params.sortBy || params.sortOrder) sortBy.value = params.sortBy ? [{ key: params.sortBy, order: params.sortOrder || 'desc' }] : sortBy.value;
    } finally {
      loading.value = false;
    }
  }

  return { loading, items, total, page, size, sortBy, fetch };
}

