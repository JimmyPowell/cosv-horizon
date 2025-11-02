package tech.cspioneer.backend.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex, HttpServletRequest request) {
        log.warn("ApiException code={} msg={} path={} method={} userUuid={} requestId={}",
                ex.getCode(),
                ex.getMessage(),
                safePath(request),
                request.getMethod(),
                currentUser(),
                MDC.get("requestId"));
        return ResponseEntity.ok(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("AccessDenied path={} method={} userUuid={} requestId={}",
                safePath(request), request.getMethod(), currentUser(), MDC.get("requestId"));
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, "无权限"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuth(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Unauthenticated path={} method={} userUuid={} requestId={}",
                safePath(request), request.getMethod(), currentUser(), MDC.get("requestId"));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "未认证"));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidation(Exception ex, HttpServletRequest request) {
        log.warn("ValidationException type={} msg={} path={} method={} userUuid={} requestId={}",
                ex.getClass().getSimpleName(), messageOf(ex), safePath(request), request.getMethod(), currentUser(), MDC.get("requestId"));
        return ResponseEntity.ok(ApiResponse.error(1001, "参数错误: " + messageOf(ex)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOther(Exception ex, HttpServletRequest request) {
        log.error("UnhandledException type={} path={} method={} userUuid={} requestId={}",
                ex.getClass().getSimpleName(), safePath(request), request.getMethod(), currentUser(), MDC.get("requestId"), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(1500, "服务器内部错误"));
    }

    private String messageOf(Exception ex) {
        String msg = ex.getMessage();
        return msg == null ? ex.getClass().getSimpleName() : msg;
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "anonymous" : String.valueOf(auth.getPrincipal());
    }

    private String safePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri == null ? "/" : uri;
    }
}
