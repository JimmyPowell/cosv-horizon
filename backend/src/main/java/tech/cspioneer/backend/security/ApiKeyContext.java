package tech.cspioneer.backend.security;

public class ApiKeyContext {
    private String subjectType; // USER or ORG
    private String userUuid;
    private String orgUuid;
    private String apiKeyUuid;
    private String scopes;

    public String getSubjectType() { return subjectType; }
    public void setSubjectType(String subjectType) { this.subjectType = subjectType; }
    public String getUserUuid() { return userUuid; }
    public void setUserUuid(String userUuid) { this.userUuid = userUuid; }
    public String getOrgUuid() { return orgUuid; }
    public void setOrgUuid(String orgUuid) { this.orgUuid = orgUuid; }
    public String getApiKeyUuid() { return apiKeyUuid; }
    public void setApiKeyUuid(String apiKeyUuid) { this.apiKeyUuid = apiKeyUuid; }
    public String getScopes() { return scopes; }
    public void setScopes(String scopes) { this.scopes = scopes; }
}

