package tech.cspioneer.backend.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "统一响应结构")
public class ApiResponse<T> {
    @Schema(description = "业务码，0为成功")
    private int code;
    @Schema(description = "提示信息")
    private String message;
    @Schema(description = "业务数据")
    private T data;

    public ApiResponse() {}

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "OK", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

