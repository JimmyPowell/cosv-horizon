// Utility to detect the type of user input for smart search

const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const CVE_RE = /^CVE-\d{4}-\d{4,}$/i;
const GHSA_RE = /^GHSA-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}$/i;

// Common ID-like prefixes from OSV ecosystems and internal naming
const IDLIKE_PREFIXES = [
  'CVE', 'GHSA', 'RUSTSEC', 'PYSEC', 'GO', 'GMS', 'DSA', 'OSV', 'COSV', 'COSA', 'CSP'
];

export function detectKeyType(raw) {
  const q = String(raw || '').trim();
  if (!q) return { type: 'empty' };
  if (UUID_RE.test(q)) return { type: 'uuid' };
  if (CVE_RE.test(q)) return { type: 'cve' };
  if (GHSA_RE.test(q)) return { type: 'ghsa' };
  const upper = q.toUpperCase();
  if (IDLIKE_PREFIXES.some(p => upper.startsWith(p + '-'))) return { type: 'idlike' };
  return { type: 'text' };
}

export function isUuid(s) { return detectKeyType(s).type === 'uuid'; }
export function isCve(s) { return detectKeyType(s).type === 'cve'; }
export function isGhsa(s) { return detectKeyType(s).type === 'ghsa'; }
export function isIdLike(s) { return detectKeyType(s).type === 'idlike'; }

