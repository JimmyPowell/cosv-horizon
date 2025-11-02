// Utilities for avatar initials and background color
import { Pinyin } from 'tiny-pinyin';

function hashString(str) {
  let hash = 0;
  if (!str) return hash;
  for (let i = 0; i < str.length; i++) {
    hash = (hash << 5) - hash + str.charCodeAt(i);
    hash |= 0; // Convert to 32bit integer
  }
  return hash;
}

export function stringToHslColor(str, s = 65, l = 55) {
  const hash = Math.abs(hashString(String(str || '')));
  const hue = hash % 360;
  return `hsl(${hue}, ${s}%, ${l}%)`;
}

export function getInitialFromName(name) {
  const n = String(name || '').trim();
  if (!n) return '?';
  const first = n.charAt(0);
  // Chinese range
  const isCJK = /[\u4e00-\u9fa5]/.test(first);
  if (isCJK) {
    try {
      const py = Pinyin.convertToPinyin(first, '', true) || first;
      const ch = py.charAt(0) || first;
      return ch.toUpperCase();
    } catch (_) {
      return first.toUpperCase();
    }
  }
  return first.toUpperCase();
}

