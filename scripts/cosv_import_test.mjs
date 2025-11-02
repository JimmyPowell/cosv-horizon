#!/usr/bin/env node
// COSV Horizon API end-to-end test (read + file upload single/multi)
// Usage:
//   HOST=http://localhost:8080 API_KEY=cosv_xxx node scripts/cosv_import_test.mjs
// Optional flags:
//   --host=http://host:port  --key=cosv_xxx  --language=PYTHON

import fs from 'fs/promises';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

function arg(name, def) {
  const kv = process.argv.find(a => a.startsWith(`--${name}=`));
  if (kv) return kv.split('=')[1];
  return process.env[name.toUpperCase()] || def;
}

const HOST = (arg('host', 'http://localhost:8080')).replace(/\/$/, '');
const API_KEY = arg('key', process.env.API_KEY || '');
const LANGUAGE = arg('language', 'PYTHON');

if (!API_KEY) {
  console.error('Missing API key. Set API_KEY env or --key=');
  process.exit(1);
}

const headersJson = {
  'X-API-Key': API_KEY,
  'Content-Type': 'application/json',
};

async function httpJson(method, url, body, extraHeaders = {}) {
  const res = await fetch(url, {
    method,
    headers: { 'X-API-Key': API_KEY, ...extraHeaders },
    body,
  });
  const text = await res.text();
  let data;
  try { data = JSON.parse(text); } catch { data = { raw: text }; }
  if (!res.ok) {
    const msg = data?.message || res.statusText || String(text).slice(0, 200);
    throw new Error(`${method} ${url} -> ${res.status}: ${msg}`);
  }
  return data;
}

async function httpGet(url) { return httpJson('GET', url); }
async function httpPost(url, json) {
  return httpJson('POST', url, JSON.stringify(json), { 'Content-Type': 'application/json' });
}

async function uploadJson(filename, content, organizationUuid) {
  const fd = new FormData();
  const blob = new Blob([content], { type: 'application/json' });
  fd.append('file', blob, filename);
  fd.append('mimeType', 'application/json');
  if (organizationUuid) fd.append('organizationUuid', organizationUuid);
  const res = await fetch(`${HOST}/cosv/files`, { method: 'POST', headers: { 'X-API-Key': API_KEY }, body: fd });
  if (!res.ok) {
    const t = await res.text();
    throw new Error(`upload failed: ${res.status} ${t}`);
  }
  return res.json();
}

function buildSinglePayload(categoryCode, tagCodes) {
  return JSON.stringify({
    schema_version: '1.0.0',
    summary: '批量导入（单条）示例：HTTP 请求走私',
    details: '示例详细描述（单条）。',
    severity: [ { type: 'CVSS:3.1/BASE', score: 'AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:M/A:L', level: 'HIGH', score_num: 7.8 } ],
    affected: [ { pkg: { ecosystem: 'PyPI', name: 'aiohttp', purl: 'pkg:pypi/aiohttp', language: 'PYTHON', repository: 'https://github.com/aio-libs/aiohttp' } } ],
    database_specific: {
      category_code: categoryCode || undefined,
      tag_codes: Array.isArray(tagCodes) && tagCodes.length ? tagCodes : undefined,
    }
  });
}

function buildMultiPayload(categoryCodeA, tagCodesA, categoryCodeB, tagCodesB) {
  return JSON.stringify({ items: [
    {
      schema_version: '1.0.0',
      summary: '多条示例-1：HTTP 请求走私',
      details: '示例详细描述（多条-1）。',
      severity: [ { type: 'CVSS:3.1/BASE', score: 'AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:M/A:L', level: 'HIGH', score_num: 7.5 } ],
      affected: [ { pkg: { ecosystem: 'PyPI', name: 'fastapi', purl: 'pkg:pypi/fastapi', language: 'PYTHON', repository: 'https://github.com/tiangolo/fastapi' } } ],
      database_specific: {
        category_code: categoryCodeA || undefined,
        tag_codes: Array.isArray(tagCodesA) && tagCodesA.length ? tagCodesA : undefined,
      }
    },
    {
      schema_version: '1.0.0',
      summary: '多条示例-2：模板注入 RCE',
      details: '示例详细描述（多条-2）。',
      severity: [ { type: 'CVSS:3.1/BASE', score: 'AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H', level: 'CRITICAL', score_num: 9.8 } ],
      affected: [ { pkg: { ecosystem: 'Maven', name: 'org.example:tmpl', purl: 'pkg:maven/org.example/tmpl', language: 'JAVA', repository: 'https://github.com/example/tmpl' } } ],
      database_specific: {
        category_code: categoryCodeB || undefined,
        tag_codes: Array.isArray(tagCodesB) && tagCodesB.length ? tagCodesB : undefined,
      }
    }
  ]});
}

async function main() {
  console.log(`Host: ${HOST}`);
  console.log(`Using personal API key (scopes must include vuln:read, vuln:write)`);

  // 0) 读取平台分类/标签（优先取可用 code 作演示）
  let catCodeA, catCodeB, tagCodesA = [], tagCodesB = [];
  try {
    const cats = await httpGet(`${HOST}/categories?page=1&size=50`);
    const items = cats?.data?.items || [];
    catCodeA = items[0]?.code;
    catCodeB = items[1]?.code || items[0]?.code;
  } catch (e) {
    console.warn('Fetch categories failed (will proceed without explicit category):', e.message);
  }
  try {
    const tags = await httpGet(`${HOST}/tags?page=1&size=50`);
    const items = tags?.data?.items || [];
    tagCodesA = items.slice(0, 2).map(t => t.code).filter(Boolean);
    tagCodesB = items.slice(2, 4).map(t => t.code).filter(Boolean);
  } catch (e) {
    console.warn('Fetch tags failed (will proceed without explicit tags):', e.message);
  }

  // 1) 漏洞读取（分页列表）
  console.log('\n[1] GET /vulns?size=5');
  try {
    const r = await httpGet(`${HOST}/vulns?size=5`);
    console.log('Read success. items:', (r?.data?.items || []).length);
  } catch (e) {
    console.error('Read failed:', e.message);
  }

  // 2) 单条上传 + 解析 + 入库
  console.log('\n[2] Single upload/parse/ingest');
  try {
    const single = buildSinglePayload(catCodeA, tagCodesA);
    const up = await uploadJson('single.json', single);
    const raw = up?.data?.rawFileUuid; if (!raw) throw new Error('No rawFileUuid in upload response');
    console.log('Upload OK rawFileUuid=', raw);

    const params = new URLSearchParams();
    params.set('mode', 'AUTO');
    params.set('language', LANGUAGE);
    const parse = await httpJson('POST', `${HOST}/cosv/files/${raw}/parse-batch?${params.toString()}`);
    console.log('Parse OK total=', parse?.data?.total);

    const ingest = await httpPost(`${HOST}/cosv/files/${raw}/ingest-batch`, {
      action: 'AUTO',
      conflictPolicy: 'SKIP_ALIAS',
      language: LANGUAGE,
    });
    console.log('Ingest OK success=', ingest?.data?.success, 'failed=', ingest?.data?.failed);
  } catch (e) {
    console.error('Single ingest failed:', e.message);
  }

  // 3) 多条上传 + 解析 + 入库
  console.log('\n[3] Multi upload/parse/ingest');
  try {
    const multi = buildMultiPayload(catCodeA, tagCodesA, catCodeB, tagCodesB);
    const up = await uploadJson('multi.json', multi);
    const raw = up?.data?.rawFileUuid; if (!raw) throw new Error('No rawFileUuid in upload response');
    console.log('Upload OK rawFileUuid=', raw);

    const params = new URLSearchParams();
    params.set('mode', 'AUTO');
    params.set('language', LANGUAGE);
    const parse = await httpJson('POST', `${HOST}/cosv/files/${raw}/parse-batch?${params.toString()}`);
    console.log('Parse OK total=', parse?.data?.total, 'conflicts=', parse?.data?.conflictCount);

    const ingest = await httpPost(`${HOST}/cosv/files/${raw}/ingest-batch`, {
      action: 'AUTO',
      conflictPolicy: 'SKIP_ALIAS',
      language: LANGUAGE,
    });
    console.log('Ingest OK success=', ingest?.data?.success, 'failed=', ingest?.data?.failed);
  } catch (e) {
    console.error('Multi ingest failed:', e.message);
  }

  console.log('\nDone.');
}

main().catch(e => { console.error(e); process.exit(1); });

