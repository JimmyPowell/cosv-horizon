import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useToastStore = defineStore('toast', () => {
  const queue = ref([]); // { id, text, type, timeout, actionText, onAction }
  const current = ref(null);
  const visible = ref(false);

  function _showNext() {
    if (visible.value || current.value) return;
    const next = queue.value.shift();
    if (!next) return;
    current.value = next;
    visible.value = true;
  }

  function hide() {
    // 先关闭可见性，延迟清空当前项，避免退出动画期间样式回落到默认（info）导致颜色闪烁
    const prev = current.value;
    visible.value = false;
    setTimeout(() => {
      // 若期间未被新的 toast 覆盖，清空并展示下一条
      if (current.value === prev) {
        current.value = null;
        _showNext();
      }
    }, 300); // 与 v-snackbar 默认过渡时长保持一致（~200-300ms）
  }

  function enqueue(payload) {
    const id = Date.now() + Math.random();
    const item = {
      id,
      text: String(payload.text || ''),
      type: payload.type || 'info', // success | error | info | warning
      timeout: typeof payload.timeout === 'number' ? payload.timeout : defaultTimeout(payload.type),
      actionText: payload.actionText,
      onAction: payload.onAction,
    };
    queue.value.push(item);
    _showNext();
  }

  function defaultTimeout(type) {
    switch (type) {
      case 'success': return 2200;
      case 'info': return 2500;
      case 'warning': return 3000;
      case 'error': return 3500;
      default: return 2500;
    }
  }

  function iconOf(type) {
    switch (type) {
      case 'success': return 'mdi-check-circle-outline';
      case 'error': return 'mdi-alert-circle-outline';
      case 'warning': return 'mdi-alert-outline';
      default: return 'mdi-information-outline';
    }
  }

  function colorOf(type) {
    switch (type) {
      case 'success': return 'success';
      case 'error': return 'error';
      case 'warning': return 'warning';
      default: return 'info';
    }
  }

  // Public API
  function success(text, opts = {}) { enqueue({ ...opts, text, type: 'success' }); }
  function error(text, opts = {}) { enqueue({ ...opts, text, type: 'error' }); }
  function info(text, opts = {}) { enqueue({ ...opts, text, type: 'info' }); }
  function warning(text, opts = {}) { enqueue({ ...opts, text, type: 'warning' }); }

  const currentIcon = computed(() => current.value ? iconOf(current.value.type) : '');
  const currentColor = computed(() => current.value ? colorOf(current.value.type) : 'info');

  return { queue, current, visible, currentIcon, currentColor, enqueue, hide, success, error, info, warning };
});

// Convenience helper for components
export function useToast() {
  const store = useToastStore();
  return {
    success: store.success,
    error: store.error,
    info: store.info,
    warning: store.warning,
  };
}
