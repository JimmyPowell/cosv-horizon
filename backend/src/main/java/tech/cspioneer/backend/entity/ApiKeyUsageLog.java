package tech.cspioneer.backend.entity;

import java.time.LocalDateTime;

public class ApiKeyUsageLog {
    private Long id;
    private String uuid;
    private Long apiKeyId;
    private LocalDateTime requestTimestamp;
    private String requestIpAddress;
    private String requestMethod;
    private String requestPath;
    private Integer responseStatusCode;
    private String userAgent;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public Long getApiKeyId() { return apiKeyId; }
    public void setApiKeyId(Long apiKeyId) { this.apiKeyId = apiKeyId; }
    public LocalDateTime getRequestTimestamp() { return requestTimestamp; }
    public void setRequestTimestamp(LocalDateTime requestTimestamp) { this.requestTimestamp = requestTimestamp; }
    public String getRequestIpAddress() { return requestIpAddress; }
    public void setRequestIpAddress(String requestIpAddress) { this.requestIpAddress = requestIpAddress; }
    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
    public String getRequestPath() { return requestPath; }
    public void setRequestPath(String requestPath) { this.requestPath = requestPath; }
    public Integer getResponseStatusCode() { return responseStatusCode; }
    public void setResponseStatusCode(Integer responseStatusCode) { this.responseStatusCode = responseStatusCode; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}

