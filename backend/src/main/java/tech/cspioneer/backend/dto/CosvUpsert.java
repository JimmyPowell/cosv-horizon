package tech.cspioneer.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "COSV 全量/增量负载（用于在 /vulns 表单接口内嵌提交）")
public class CosvUpsert {
    // 顶层
    public String id; // 可选：若创建时希望自带业务ID（通常系统生成）
    public String schemaVersion;
    public String summary;
    public String details;
    public String published;   // RFC3339
    public String withdrawn;   // RFC3339
    public String confirmedType;
    public Object databaseSpecific; // 任意 JSON，可为 Map/List

    // 关联
    public List<String> aliases;
    public List<String> related;
    public List<Reference> references;

    // 类型/时间线
    public List<String> cweIds;
    public List<String> cweNames;
    public List<TimePoint> timeLine;

    // 危险性（多量表）
    public List<SeverityItem> severity;

    // 受影响面
    public List<Affected> affected;

    // 扩展
    public List<PatchDetail> patchDetails;
    public List<Contributor> contributors;
    public List<Credit> credits;
    public List<String> exploitStatus;

    @Data
    public static class Reference { public String type; public String url; }

    @Data
    public static class TimePoint { public String type; public String value; }

    @Data
    public static class SeverityItem { public String type; public String score; public String level; public Float scoreNum; }

    @Data
    public static class Affected {
        @JsonAlias({"package"})
        public PackageSpec pkg;
        public List<SeverityItem> severity; // 暂存在 databaseSpecific.severity
        public List<Range> ranges;
        public List<String> versions;
        public Object ecosystemSpecific;
        public Object databaseSpecific;
    }

    @Data
    public static class PackageSpec {
        public String ecosystem;
        public String name;
        public String purl;
        public String language;
        public String repository;
        public List<String> introducedCommits;
        public List<String> fixedCommits;
        public String homePage;
        public String edition;
        public Object ecosystemSpecific;
        public Object databaseSpecific;
    }

    @Data
    public static class Range {
        public String type; // ECOSYSTEM|SEMVER|GIT
        public String repo; // for GIT
        public List<Map<String, String>> events; // 单键对象列表
        public Object databaseSpecific;
    }

    @Data
    public static class PatchDetail {
        public String patchUrl;
        public String issueUrl;
        public String mainLanguage;
        public String author;
        public String committer;
        public List<String> branches;
        public List<String> tags;
    }

    @Data
    public static class Contributor { public String org; public String name; public String email; public String contributions; }

    @Data
    public static class Credit { public String name; public List<String> contact; public String type; }
}
