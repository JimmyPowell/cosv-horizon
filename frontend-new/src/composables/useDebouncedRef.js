import { ref, watch } from 'vue';

export function useDebouncedRef(initial = '', delay = 300) {
  const source = ref(initial);
  const debounced = ref(initial);
  let timer = null;
  watch(source, (v) => {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => { debounced.value = v; }, delay);
  });
  return { source, debounced };
}

