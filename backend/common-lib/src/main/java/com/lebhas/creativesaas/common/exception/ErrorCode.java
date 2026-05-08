package com.lebhas.creativesaas.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    VALIDATION_FAILED("COMMON-400", "Validation failed", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("COMMON-401", "Authentication is required", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("COMMON-403", "Access denied", HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND("COMMON-404", "Resource not found", HttpStatus.NOT_FOUND),
    BUSINESS_RULE_VIOLATION("COMMON-409", "Business rule violation", HttpStatus.CONFLICT),
    TENANT_HEADER_INVALID("TENANT-400", "Workspace header is invalid", HttpStatus.BAD_REQUEST),
    WORKSPACE_CONTEXT_REQUIRED("TENANT-401", "Workspace context is required", HttpStatus.BAD_REQUEST),
    WORKSPACE_ACCESS_DENIED("TENANT-403", "Workspace access denied", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS("AUTH-401", "Invalid email or password", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("AUTH-401-01", "Token is invalid", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("AUTH-401-02", "Token has expired", HttpStatus.UNAUTHORIZED),
    TOKEN_REVOKED("AUTH-401-03", "Token has been revoked", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_INVALID("AUTH-401-04", "Refresh token is invalid", HttpStatus.UNAUTHORIZED),
    INVITATION_INVALID("AUTH-400-01", "Invitation is invalid", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS("USER-409-01", "Email is already registered", HttpStatus.CONFLICT),
    USER_INACTIVE("USER-403-01", "User account is not active", HttpStatus.FORBIDDEN),
    WORKSPACE_NOT_FOUND("WORKSPACE-404-01", "Workspace not found", HttpStatus.NOT_FOUND),
    WORKSPACE_SLUG_ALREADY_EXISTS("WORKSPACE-409-01", "Workspace slug is already in use", HttpStatus.CONFLICT),
    WORKSPACE_MEMBER_NOT_FOUND("WORKSPACE-404-02", "Workspace member not found", HttpStatus.NOT_FOUND),
    WORKSPACE_OWNER_REQUIRED("WORKSPACE-403-01", "Workspace owner access is required", HttpStatus.FORBIDDEN),
    WORKSPACE_PERMISSION_INVALID("WORKSPACE-400-01", "Workspace permission assignment is invalid", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("COMMON-500", "Unexpected server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    public String code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
