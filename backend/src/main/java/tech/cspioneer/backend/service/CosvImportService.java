package tech.cspioneer.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.cspioneer.backend.common.ApiException;
import tech.cspioneer.backend.dto.CosvUpsert;
import tech.cspioneer.backend.entity.RawCosvFile;
import tech.cspioneer.backend.entity.User;
import tech.cspioneer.backend.enums.FileStatus;
import tech.cspioneer.backend.mapper.*;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;

@Service
public class CosvImportService {
    private final RawCosvFileMapper rawMapper;
    private final UserMapper userMapper;
    private final OrganizationMapper organizationMapper;
    private final LnkUserOrganizationMapper linkMapper;
    private final VulnerabilityMetadataMapper vmMapper;
    private final VulnerabilityMetadataAliasMapper aliasMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final VulnerabilityService vulnService;

    private final ObjectMapper snakeMapper;

    public CosvImportService(RawCosvFileMapper rawMapper,
                             UserMapper userMapper,
                             OrganizationMapper organizationMapper,
                             LnkUserOrganizationMapper linkMapper,
                             VulnerabilityMetadataMapper vmMapper,
                             VulnerabilityMetadataAliasMapper aliasMapper,
                             CategoryMapper categoryMapper,
                             TagMapper tagMapper,
                             VulnerabilityService vulnService) {
        this.rawMapper = rawMapper;
        this.userMapper = userMapper;
        this.organizationMapper = organizationMapper;
        this.linkMapper = linkMapper;
        this.vmMapper = vmMapper;
        this.aliasMapper = aliasMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.vulnService = vulnService;
        this.snakeMapper = new ObjectMapper();
        this.snakeMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Transactional
    public String upload(Principal principal,
                         MultipartFile file,
                         String organizationUuid,
                         String mimeType) {
        if (file == null || file.isEmpty()) throw new ApiException(1001, "文件不能为空");
        User user = requireUserByUuid(principal.getName());
        Long orgId = null;
        if (organizationUuid != null && !organizationUuid.isBlank()) {
            var org = Optional.ofNullable(organizationMapper.findByUuid(organizationUuid))
                    .orElseThrow(() -> new ApiException(404, "组织不存在"));
            var link = linkMapper.findByOrgIdAndUserId(org.getId(), user.getId());
            if (link == null || link.getRole() != tech.cspioneer.backend.enums.OrganizationRole.ADMIN) {
                throw new ApiException(1012, "仅组织管理员可代表组织上传");
            }
            orgId = org.getId();
        }
        try {
            RawCosvFile r = new RawCosvFile();
            r.setUuid(UUID.randomUUID().toString());
            r.setFileName(file.getOriginalFilename());
            r.setUserId(user.getId());
            r.setOrganizationId(orgId);
            r.setStatus(FileStatus.UPLOADED);
            r.setStatusMessage(null);
            r.setContentLength(file.getSize());
            r.setStorageUrl(null);
            r.setContent(file.getBytes());
            r.setChecksumSha256(null);
            r.setMimeType(mimeType != null ? mimeType : file.getContentType());
            rawMapper.insert(r);
            return r.getUuid();
        } catch (Exception e) {
            throw new ApiException(500, "文件保存失败: " + e.getMessage());
        }
    }

    public Map<String, Object> parse(String rawFileUuid,
                                     String language,
                                     String categoryCode,
                                     List<String> tagCodes,
                                     String mode) {
        RawCosvFile rf = Optional.ofNullable(rawMapper.findByUuid(rawFileUuid))
                .orElseThrow(() -> new ApiException(404, "原始文件不存在"));
        CosvUpsert cosv = readCosv(rf);

        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> conflicts = new ArrayList<>();

        // Identifier based upsert suggestion
        String targetUuid = null;
        String cosvId = cosv != null ? cosv.getId() : null;
        if (cosvId != null && !cosvId.isBlank()) {
            var existing = vmMapper.findByIdentifier(cosvId.trim());
            if (existing != null) {
                targetUuid = existing.getUuid();
            }
        }
        String suggestedAction = (targetUuid != null ? "UPDATE" : "CREATE");

        // Aliases conflict
        if (cosv != null && cosv.getAliases() != null) {
            for (String a : cosv.getAliases()) {
                if (a == null || a.isBlank()) continue;
                String vu = aliasMapper.findVulnerabilityUuidByAlias(a.trim());
                if (vu != null && (targetUuid == null || !targetUuid.equals(vu))) {
                    Map<String, Object> c = new LinkedHashMap<>();
                    c.put("type", "ALIAS_CONFLICT");
                    c.put("alias", a);
                    c.put("vulnerabilityUuid", vu);
                    conflicts.add(c);
                }
            }
        }

        // Category/Tag/Language validation (allow per-item override via database_specific)
        String perItemCat = resolvePerItemCategoryCode(cosv);
        if (perItemCat == null) perItemCat = resolvePerItemCategoryByName(cosv);
        List<String> perItemTags = resolvePerItemTagCodes(cosv);
        if (perItemTags == null || perItemTags.isEmpty()) perItemTags = resolvePerItemTagByNames(cosv);
        String effectiveCategory = perItemCat != null ? perItemCat : normalizeCode(categoryCode);
        List<String> effectiveTags = (perItemTags != null && !perItemTags.isEmpty())
                ? perItemTags
                : (tagCodes == null ? List.of() : tagCodes.stream().filter(Objects::nonNull).map(this::normalizeCode).filter(Objects::nonNull).toList());

        if (effectiveCategory != null) {
            var c = categoryMapper.findByCode(effectiveCategory);
            if (c == null) {
                Map<String, Object> cc = new LinkedHashMap<>(); cc.put("type", "CATEGORY_NOT_FOUND"); cc.put("categoryCode", effectiveCategory); conflicts.add(cc);
            }
        }
        if (effectiveTags != null) {
            for (String t : effectiveTags) {
                if (t == null || t.isBlank()) continue;
                String tt = normalizeCode(t);
                if (tagMapper.countByCode(tt) == 0) {
                    Map<String, Object> cc = new LinkedHashMap<>(); cc.put("type", "TAG_NOT_FOUND"); cc.put("tagCode", tt); conflicts.add(cc);
                }
            }
        }
        if (language != null && !language.isBlank()) {
            try { tech.cspioneer.backend.enums.ProgrammingLanguage.fromCode(language); } catch (Exception e) {
                Map<String, Object> cc = new LinkedHashMap<>(); cc.put("type", "LANGUAGE_INVALID"); cc.put("language", language); conflicts.add(cc);
            }
        }

        // Aggregate severity suggestion（允许从 score 文本中提取数字作为兜底）
        Float sev = null;
        if (cosv != null && cosv.getSeverity() != null) {
            for (var s : cosv.getSeverity()) {
                if (s == null) continue;
                Float n = s.getScoreNum();
                if (n == null && s.getScore() != null) n = tryParseScoreNum(s.getScore());
                if (n != null) { if (sev == null || n > sev) sev = n; }
            }
        }

        result.put("rawFileUuid", rawFileUuid);
        result.put("suggestedAction", suggestedAction);
        if (targetUuid != null) result.put("targetVulnerabilityUuid", targetUuid);
        result.put("conflicts", conflicts);
        result.put("aggregatedSeverityNum", sev);
        result.put("cosvPreview", cosv);
        return result;
    }

    public Map<String, Object> parseBatch(String rawFileUuid,
                                          String language,
                                          String categoryCode,
                                          List<String> tagCodes,
                                          String mode) {
        RawCosvFile rf = Optional.ofNullable(rawMapper.findByUuid(rawFileUuid))
                .orElseThrow(() -> new ApiException(404, "原始文件不存在"));
        List<CosvUpsert> list = readCosvList(rf);
        if (list.isEmpty()) throw new ApiException(400, "文件中未解析到任何记录");
        List<Map<String, Object>> items = new ArrayList<>();
        int idx = 0;
        for (CosvUpsert cosv : list) {
            Map<String, Object> one = new LinkedHashMap<>();
            String targetUuid = null;
            String cosvId = cosv != null ? cosv.getId() : null;
            if (cosvId != null && !cosvId.isBlank()) {
                var existing = vmMapper.findByIdentifier(cosvId.trim());
                if (existing != null) targetUuid = existing.getUuid();
            }
            String m = Optional.ofNullable(mode).orElse("AUTO");
            String suggestedAction = (targetUuid != null ? "UPDATE" : ("UPDATE".equalsIgnoreCase(m) ? "UPDATE" : "CREATE"));

            List<Map<String, Object>> conflicts = new ArrayList<>();
            if (cosv != null && cosv.getAliases() != null) {
                for (String a : cosv.getAliases()) {
                    if (a == null || a.isBlank()) continue;
                    String vu = aliasMapper.findVulnerabilityUuidByAlias(a.trim());
                    if (vu != null && (targetUuid == null || !targetUuid.equals(vu))) {
                        Map<String, Object> c = new LinkedHashMap<>(); c.put("type", "ALIAS_CONFLICT"); c.put("alias", a); c.put("vulnerabilityUuid", vu); conflicts.add(c);
                    }
                }
            }
            // Per-item override for category/tags
            String perItemCatB = resolvePerItemCategoryCode(cosv);
            if (perItemCatB == null) perItemCatB = resolvePerItemCategoryByName(cosv);
            List<String> perItemTagsB = resolvePerItemTagCodes(cosv);
            if (perItemTagsB == null || perItemTagsB.isEmpty()) perItemTagsB = resolvePerItemTagByNames(cosv);
            String normCategory = perItemCatB != null ? perItemCatB : normalizeCode(categoryCode);
            if (normCategory != null && categoryMapper.findByCode(normCategory) == null) {
                Map<String, Object> cc = new LinkedHashMap<>(); cc.put("type", "CATEGORY_NOT_FOUND"); cc.put("categoryCode", normCategory); conflicts.add(cc);
            }
            List<String> effTags = (perItemTagsB != null && !perItemTagsB.isEmpty())
                    ? perItemTagsB
                    : (tagCodes == null ? List.of() : tagCodes.stream().filter(Objects::nonNull).map(this::normalizeCode).filter(Objects::nonNull).toList());
            if (effTags != null) {
                for (String t : effTags) {
                    if (t == null || t.isBlank()) continue; String tt = normalizeCode(t);
                    if (tagMapper.countByCode(tt) == 0) {
                        Map<String, Object> cc = new LinkedHashMap<>(); cc.put("type", "TAG_NOT_FOUND"); cc.put("tagCode", tt); conflicts.add(cc);
                    }
                }
            }
            if (language != null && !language.isBlank()) {
                try { tech.cspioneer.backend.enums.ProgrammingLanguage.fromCode(language); } catch (Exception e) { Map<String, Object> cc = new LinkedHashMap<>(); cc.put("type", "LANGUAGE_INVALID"); cc.put("language", language); conflicts.add(cc); }
            }
            Float sev = null;
            if (cosv != null && cosv.getSeverity() != null) {
                for (var s : cosv.getSeverity()) {
                    if (s == null) continue;
                    Float n = s.getScoreNum();
                    if (n == null && s.getScore() != null) n = tryParseScoreNum(s.getScore());
                    if (n != null) { if (sev == null || n > sev) sev = n; }
                }
            }
            one.put("index", idx++);
            if (cosvId != null) one.put("id", cosvId);
            if (cosv != null && cosv.getSummary() != null) one.put("summary", cosv.getSummary());
            one.put("suggestedAction", suggestedAction);
            if (targetUuid != null) one.put("targetVulnerabilityUuid", targetUuid);
            one.put("conflicts", conflicts);
            one.put("aggregatedSeverityNum", sev);
            items.add(one);
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("rawFileUuid", rawFileUuid);
        res.put("items", items);
        res.put("total", items.size());
        res.put("conflictCount", items.stream().mapToInt(i -> ((List<?>)i.getOrDefault("conflicts", List.of())).size()).sum());
        return res;
    }

    @Transactional
    public Map<String, Object> ingest(Principal principal,
                                      String rawFileUuid,
                                      String action,
                                      String targetVulnUuid,
                                      String conflictPolicy,
                                      String publishPolicy,
                                      String organizationUuid,
                                      String language,
                                      String categoryCode,
                                      List<String> tagCodes) {
        User user = requireUserByUuid(principal.getName());
        RawCosvFile rf = Optional.ofNullable(rawMapper.findByUuid(rawFileUuid))
                .orElseThrow(() -> new ApiException(404, "原始文件不存在"));

        // Resolve org from param (and check admin) or reuse raw file org
        Long orgId = rf.getOrganizationId();
        String finalOrgUuid = organizationUuid;
        if (finalOrgUuid != null && !finalOrgUuid.isBlank()) {
            var org = Optional.ofNullable(organizationMapper.findByUuid(finalOrgUuid))
                    .orElseThrow(() -> new ApiException(404, "组织不存在"));
            var link = linkMapper.findByOrgIdAndUserId(org.getId(), user.getId());
            if (link == null || link.getRole() != tech.cspioneer.backend.enums.OrganizationRole.ADMIN) throw new ApiException(1012, "仅组织管理员可代表组织导入");
            orgId = org.getId();
        } else if (orgId != null) {
            var org = organizationMapper.findById(orgId);
            finalOrgUuid = org != null ? org.getUuid() : null;
        }

        CosvUpsert cosv = readCosv(rf);
        if (cosv == null) throw new ApiException(400, "文件解析失败");

        // Resolve summary/details/language/severity
        String summary = Optional.ofNullable(cosv.getSummary()).filter(s -> !s.isBlank()).orElseThrow(() -> new ApiException(400, "summary 缺失"));
        String details = Optional.ofNullable(cosv.getDetails()).filter(s -> !s.isBlank()).orElse("");
        Float sev = null;
        if (cosv.getSeverity() != null) {
            for (var s : cosv.getSeverity()) {
                if (s == null) continue;
                Float n = s.getScoreNum();
                if (n == null && s.getScore() != null) n = tryParseScoreNum(s.getScore());
                if (n != null) { if (sev == null || n > sev) sev = n; }
            }
        }
        if (sev == null) throw new ApiException(400, "severityNum 缺失：请提供 severity.score_num 或在元数据中指定");
        String lang = language;
        if (lang == null || lang.isBlank()) {
            // 尝试从 affected.pkg.language 推断
            if (cosv.getAffected() != null) {
                for (var a : cosv.getAffected()) {
                    if (a != null && a.getPkg() != null && a.getPkg().getLanguage() != null && !a.getPkg().getLanguage().isBlank()) { lang = a.getPkg().getLanguage(); break; }
                }
            }
        }
        if (lang == null || lang.isBlank()) throw new ApiException(400, "language 缺失：请在请求参数中指定");

        // Conflict policy for aliases
        if (cosv.getAliases() != null && "SKIP_ALIAS".equalsIgnoreCase(conflictPolicy)) {
            List<String> filtered = new ArrayList<>();
            for (String a : cosv.getAliases()) {
                if (a == null || a.isBlank()) continue;
                String vu = aliasMapper.findVulnerabilityUuidByAlias(a.trim());
                if (vu == null || (targetVulnUuid != null && targetVulnUuid.equals(vu))) filtered.add(a);
            }
            cosv.setAliases(filtered);
        } else if (cosv.getAliases() != null && "FAIL".equalsIgnoreCase(Optional.ofNullable(conflictPolicy).orElse("FAIL"))) {
            for (String a : cosv.getAliases()) {
                if (a == null || a.isBlank()) continue;
                String vu = aliasMapper.findVulnerabilityUuidByAlias(a.trim());
                if (vu != null && (targetVulnUuid == null || !targetVulnUuid.equals(vu))) throw new ApiException(1015, "别名冲突: " + a);
            }
        }

        // 允许 per-item 覆盖分类/标签（database_specific.category_code / tag_codes）
        String overrideCat = resolvePerItemCategoryCode(cosv);
        if (overrideCat == null) overrideCat = resolvePerItemCategoryByName(cosv);
        List<String> overrideTags = resolvePerItemTagCodes(cosv);
        if (overrideTags == null || overrideTags.isEmpty()) overrideTags = resolvePerItemTagByNames(cosv);
        String effectiveCategory = overrideCat != null ? overrideCat : normalizeCode(categoryCode);
        List<String> effectiveTags = (overrideTags != null && !overrideTags.isEmpty())
                ? overrideTags
                : (tagCodes == null ? List.of() : tagCodes.stream().filter(Objects::nonNull).map(this::normalizeCode).filter(Objects::nonNull).toList());

        // Pre-validate dictionary & language (return ApiException 400 instead of 500)
        if (effectiveCategory != null) {
            var cc = categoryMapper.findByCode(effectiveCategory);
            if (cc == null) throw new ApiException(400, "分类不存在");
        }
        if (effectiveTags != null) {
            for (String t : effectiveTags) {
                if (t == null || t.isBlank()) continue; if (tagMapper.findByCode(t) == null) throw new ApiException(400, "标签不存在: " + t);
            }
        }
        if (language != null && !language.isBlank()) {
            try { tech.cspioneer.backend.enums.ProgrammingLanguage.fromCode(language); } catch (Exception e) { throw new ApiException(400, "语言非法"); }
        }

        // Validate ranges.events structure (single-key object per element)
        validateRangesEvents(cosv);

        // Perform action
        Map<String, Object> resp = new LinkedHashMap<>();
        if ("UPDATE".equalsIgnoreCase(action)) {
            if (targetVulnUuid == null || targetVulnUuid.isBlank()) throw new ApiException(1001, "目标漏洞uuid缺失");
            var updated = vulnService.updateWithRaw(user.getUuid(), targetVulnUuid, summary, details, sev, lang, null, effectiveCategory, cosv, rf.getId());
            // 更新路径：若提供标签则追加关联（去重由唯一索引保障）
            if (effectiveTags != null) {
                for (String t : effectiveTags) { if (t == null || t.isBlank()) continue; vulnService.addTag(user.getUuid(), updated.getUuid(), t); }
            }
            rawMapper.updateStatus(rawFileUuid, FileStatus.PROCESSED.getCode(), "更新成功");
            resp.put("vulnerability", updated);
            resp.put("cosv", vulnService.buildCosvView(updated));
        } else { // CREATE 默认
            var created = vulnService.createWithRaw(user.getUuid(), finalOrgUuid, summary, details, sev, lang, effectiveCategory, effectiveTags, null, cosv, rf.getId());
            rawMapper.updateStatus(rawFileUuid, FileStatus.PROCESSED.getCode(), "创建成功");
            resp.put("vulnerability", created);
            resp.put("cosv", vulnService.buildCosvView(created));
        }
        return resp;
    }

    @Transactional
    public Map<String, Object> ingestBatch(Principal principal,
                                           String rawFileUuid,
                                           String action,
                                           String conflictPolicy,
                                           String publishPolicy,
                                           String organizationUuid,
                                           String language,
                                           String categoryCode,
                                           List<String> tagCodes) {
        User user = requireUserByUuid(principal.getName());
        RawCosvFile rf = Optional.ofNullable(rawMapper.findByUuid(rawFileUuid))
                .orElseThrow(() -> new ApiException(404, "原始文件不存在"));
        Long orgId = rf.getOrganizationId();
        String finalOrgUuid = organizationUuid;
        if (finalOrgUuid != null && !finalOrgUuid.isBlank()) {
            var org = Optional.ofNullable(organizationMapper.findByUuid(finalOrgUuid)).orElseThrow(() -> new ApiException(404, "组织不存在"));
            var link = linkMapper.findByOrgIdAndUserId(org.getId(), user.getId());
            if (link == null || link.getRole() != tech.cspioneer.backend.enums.OrganizationRole.ADMIN) throw new ApiException(1012, "仅组织管理员可代表组织导入");
            orgId = org.getId();
        } else if (orgId != null) {
            var org = organizationMapper.findById(orgId); finalOrgUuid = org != null ? org.getUuid() : null;
        }
        List<CosvUpsert> list = readCosvList(rf);
        if (list.isEmpty()) throw new ApiException(400, "文件中未解析到任何记录");
        List<Map<String, Object>> results = new ArrayList<>();
        int success = 0, failed = 0;
        for (var cosv : list) {
            Map<String, Object> r = new LinkedHashMap<>();
            try {
                // resolve fields
                String summary = Optional.ofNullable(cosv.getSummary()).filter(s -> !s.isBlank()).orElseThrow(() -> new ApiException(400, "summary 缺失"));
                String details = Optional.ofNullable(cosv.getDetails()).filter(s -> !s.isBlank()).orElse("");
            Float sev = null;
            if (cosv.getSeverity() != null) {
                for (var s : cosv.getSeverity()) {
                    if (s == null) continue;
                    Float n = s.getScoreNum();
                    if (n == null && s.getScore() != null) n = tryParseScoreNum(s.getScore());
                    if (n != null) { if (sev == null || n > sev) sev = n; }
                }
            }
                if (sev == null) throw new ApiException(400, "severityNum 缺失");
                String lang = language; if (lang == null || lang.isBlank()) { if (cosv.getAffected() != null) for (var a : cosv.getAffected()) { if (a != null && a.getPkg() != null && a.getPkg().getLanguage() != null && !a.getPkg().getLanguage().isBlank()) { lang = a.getPkg().getLanguage(); break; } } }
                if (lang == null || lang.isBlank()) throw new ApiException(400, "language 缺失");
                String cosvId = cosv.getId();
                String targetUuid = null;
                if (cosvId != null && !cosvId.isBlank()) { var existing = vmMapper.findByIdentifier(cosvId.trim()); if (existing != null) targetUuid = existing.getUuid(); }
                String act = action == null ? "AUTO" : action;
                if ("AUTO".equalsIgnoreCase(act)) act = (targetUuid != null) ? "UPDATE" : "CREATE";

                // alias conflict policy
                if (cosv.getAliases() != null && "SKIP_ALIAS".equalsIgnoreCase(conflictPolicy)) {
                    List<String> filtered = new ArrayList<>();
                    for (String a : cosv.getAliases()) { if (a == null || a.isBlank()) continue; String vu = aliasMapper.findVulnerabilityUuidByAlias(a.trim()); if (vu == null || (targetUuid != null && targetUuid.equals(vu))) filtered.add(a); }
                    cosv.setAliases(filtered);
                } else if (cosv.getAliases() != null) {
                    for (String a : cosv.getAliases()) { if (a == null || a.isBlank()) continue; String vu = aliasMapper.findVulnerabilityUuidByAlias(a.trim()); if (vu != null && (targetUuid == null || !targetUuid.equals(vu))) throw new ApiException(1015, "别名冲突: " + a); }
                }

                // effective codes (allow per-item overrides via database_specific)
        String perItemCat = resolvePerItemCategoryCode(cosv);
        if (perItemCat == null) perItemCat = resolvePerItemCategoryByName(cosv);
        List<String> perItemTags = resolvePerItemTagCodes(cosv);
        if (perItemTags == null || perItemTags.isEmpty()) perItemTags = resolvePerItemTagByNames(cosv);
                String normCat = perItemCat != null ? perItemCat : normalizeCode(categoryCode);
                List<String> normTags = (perItemTags != null && !perItemTags.isEmpty())
                        ? perItemTags
                        : (tagCodes == null ? List.of() : tagCodes.stream().filter(Objects::nonNull).map(this::normalizeCode).filter(Objects::nonNull).toList());

                // Pre-validate dictionary & language to avoid 500
                if (normCat != null) {
                    var cc = categoryMapper.findByCode(normCat);
                    if (cc == null) throw new ApiException(400, "分类不存在");
                }
                if (normTags != null) {
                    for (String t : normTags) { if (t == null || t.isBlank()) continue; if (tagMapper.findByCode(t) == null) throw new ApiException(400, "标签不存在: " + t); }
                }
                try { tech.cspioneer.backend.enums.ProgrammingLanguage.fromCode(lang); } catch (Exception e) { throw new ApiException(400, "语言非法"); }

                // validate ranges.events before touching DB
                validateRangesEvents(cosv);
                if ("UPDATE".equalsIgnoreCase(act)) {
                    if (targetUuid == null) throw new ApiException(1001, "未找到可更新的目标");
                    var updated = vulnService.updateWithRaw(user.getUuid(), targetUuid, summary, details, sev, lang, null, normCat, cosv, rf.getId());
                    // apply tags on update
                    if (normTags != null) {
                        for (String t : normTags) { if (t == null || t.isBlank()) continue; vulnService.addTag(user.getUuid(), updated.getUuid(), t); }
                    }
                    r.put("status", "OK"); r.put("action", "UPDATE"); r.put("uuid", updated.getUuid()); r.put("identifier", updated.getIdentifier()); success++;
                } else {
                    var created = vulnService.createWithRaw(user.getUuid(), finalOrgUuid, summary, details, sev, lang, normCat, normTags, null, cosv, rf.getId());
                    r.put("status", "OK"); r.put("action", "CREATE"); r.put("uuid", created.getUuid()); r.put("identifier", created.getIdentifier()); success++;
                }
            } catch (Exception ex) {
                r.put("status", "ERROR"); r.put("message", ex.getMessage()); failed++;
            }
            results.add(r);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("rawFileUuid", rawFileUuid);
        out.put("total", results.size());
        out.put("success", success);
        out.put("failed", failed);
        out.put("items", results);
        return out;
    }

    private CosvUpsert readCosv(RawCosvFile rf) {
        try {
            if (rf.getContent() == null) return null;
            String json = new String(rf.getContent(), StandardCharsets.UTF_8);
            return snakeMapper.readValue(json, CosvUpsert.class);
        } catch (Exception e) {
            throw new ApiException(400, "COSV解析失败: " + e.getMessage());
        }
    }

    private List<CosvUpsert> readCosvList(RawCosvFile rf) {
        try {
            if (rf.getContent() == null) return List.of();
            String raw = new String(rf.getContent(), StandardCharsets.UTF_8).trim();
            // NDJSON first-pass: if multi-line and each non-empty line seems a JSON object, try parse-by-line
            if (raw.indexOf('\n') >= 0) {
                String[] lines = raw.split("\n");
                List<CosvUpsert> items = new ArrayList<>();
                for (String line : lines) {
                    String s = line == null ? null : line.trim();
                    if (s == null || s.isEmpty()) continue;
                    if (!s.startsWith("{") || !s.endsWith("}")) continue;
                    try {
                        items.add(snakeMapper.readValue(s, CosvUpsert.class));
                    } catch (Exception ignore) {}
                }
                if (items.size() >= 2) return items;
            }
            if (raw.startsWith("[")) {
                java.util.List<CosvUpsert> arr = snakeMapper.readValue(
                        raw,
                        snakeMapper.getTypeFactory().constructCollectionType(java.util.List.class, CosvUpsert.class)
                );
                return (arr == null) ? java.util.List.of() : arr;
            }
            if (raw.startsWith("{")) {
                // try object with items
                var node = snakeMapper.readTree(raw);
                if (node.has("items") && node.get("items").isArray()) {
                    var it = node.get("items");
                    List<CosvUpsert> list = new ArrayList<>();
                    for (var el : it) list.add(snakeMapper.treeToValue(el, CosvUpsert.class));
                    return list;
                }
                // single object fallback
                CosvUpsert one = snakeMapper.treeToValue(node, CosvUpsert.class);
                return one == null ? List.of() : List.of(one);
            }
            // NDJSON fallback
            List<CosvUpsert> items = new ArrayList<>();
            for (String line : raw.split("\n")) {
                String s = line.trim(); if (s.isEmpty()) continue;
                items.add(snakeMapper.readValue(s, CosvUpsert.class));
            }
            return items;
        } catch (Exception e) {
            throw new ApiException(400, "COSV批量解析失败: " + e.getMessage());
        }
    }

    private String normalizeCode(String s) { if (s == null) return null; String t = s.trim(); if (t.isEmpty()) return null; return t.toUpperCase(java.util.Locale.ROOT); }

    // ----- per-item override helpers -----
    @SuppressWarnings("unchecked")
    private String resolvePerItemCategoryCode(CosvUpsert cosv) {
        if (cosv == null) return null;
        Object ds = cosv.getDatabaseSpecific();
        if (ds instanceof Map<?, ?> m) {
            Object v = null;
            if (m.containsKey("category_code")) v = m.get("category_code");
            else if (m.containsKey("categoryCode")) v = m.get("categoryCode");
            if (v == null) return null;
            String s = String.valueOf(v);
            return normalizeCode(s);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<String> resolvePerItemTagCodes(CosvUpsert cosv) {
        if (cosv == null) return null;
        Object ds = cosv.getDatabaseSpecific();
        if (ds instanceof Map<?, ?> m) {
            Object v = null;
            if (m.containsKey("tag_codes")) v = m.get("tag_codes");
            else if (m.containsKey("tagCodes")) v = m.get("tagCodes");
            if (v == null) return null;
            List<String> out = new ArrayList<>();
            if (v instanceof List<?> arr) {
                for (Object e : arr) { if (e == null) continue; String n = normalizeCode(String.valueOf(e)); if (n != null) out.add(n); }
            } else {
                String s = String.valueOf(v);
                for (String p : s.split(",")) { String n = normalizeCode(p); if (n != null) out.add(n); }
            }
            return out;
        }
        return null;
    }

    // Optional: resolve by names (category_name/tag_names) to platform codes
    private String resolvePerItemCategoryByName(CosvUpsert cosv) {
        if (cosv == null) return null;
        Object ds = cosv.getDatabaseSpecific();
        if (ds instanceof Map<?, ?> m) {
            Object v = null;
            if (m.containsKey("category_name")) v = m.get("category_name");
            else if (m.containsKey("categoryName")) v = m.get("categoryName");
            if (v == null) return null;
            String name = String.valueOf(v).trim(); if (name.isEmpty()) return null;
            try {
                var c = categoryMapper.findByCode(name);
                if (c != null) return normalizeCode(c.getCode());
            } catch (Exception ignore) {}
            try {
                // there is no direct findByName in mapper; emulate via list(q)
                var list = categoryMapper.list(name, 1, 0);
                if (list != null && !list.isEmpty()) return normalizeCode(list.get(0).getCode());
            } catch (Exception ignore) {}
        }
        return null;
    }

    private List<String> resolvePerItemTagByNames(CosvUpsert cosv) {
        if (cosv == null) return null;
        Object ds = cosv.getDatabaseSpecific();
        List<String> out = new ArrayList<>();
        if (ds instanceof Map<?, ?> m) {
            Object v = null;
            if (m.containsKey("tag_names")) v = m.get("tag_names");
            else if (m.containsKey("tagNames")) v = m.get("tagNames");
            if (v == null) return null;
            List<String> names = new ArrayList<>();
            if (v instanceof List<?> arr) {
                for (Object e : arr) { if (e != null) names.add(String.valueOf(e).trim()); }
            } else {
                String s = String.valueOf(v);
                for (String p : s.split(",")) { String t = p == null ? null : p.trim(); if (t != null && !t.isEmpty()) names.add(t); }
            }
            for (String nm : names) {
                if (nm == null || nm.isBlank()) continue;
                // try code
                var t = tagMapper.findByCode(nm);
                if (t == null) t = tagMapper.findByName(nm);
                if (t != null && t.getCode() != null) out.add(normalizeCode(t.getCode()));
            }
        }
        return out.isEmpty() ? null : out;
    }

    private void validateRangesEvents(CosvUpsert cosv) {
        if (cosv == null || cosv.getAffected() == null) return;
        for (var a : cosv.getAffected()) {
            if (a == null || a.getRanges() == null) continue;
            for (var r : a.getRanges()) {
                if (r == null || r.getEvents() == null) continue;
                for (var ev : r.getEvents()) {
                    if (ev == null || ev.isEmpty() || ev.size() != 1) {
                        throw new ApiException(400, "ranges.events 元素必须为单键对象");
                    }
                }
            }
        }
    }

    // 提取文本中的首个数字（如 "8.8 HIGH" -> 8.8），用于在缺失 score_num 时兜底
    private Float tryParseScoreNum(String scoreText) {
        if (scoreText == null) return null;
        String s = scoreText.trim();
        if (s.isEmpty()) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("([0-9]+(?:\\.[0-9]+)?)").matcher(s);
        if (m.find()) {
            try { return Float.parseFloat(m.group(1)); } catch (Exception ignore) {}
        }
        return null;
    }

    private User requireUserByUuid(String uuid) {
        User u = userMapper.findByUuid(uuid);
        if (u == null || u.getStatus() == null) throw new ApiException(1005, "用户状态异常");
        return u;
    }
}
