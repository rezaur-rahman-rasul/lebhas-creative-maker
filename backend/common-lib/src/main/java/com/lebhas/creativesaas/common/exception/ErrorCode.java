package com.lebhas.creativesaas.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    VALIDATION_FAILED("COMMON-400", "Validation failed", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("COMMON-401", "Authentication is required", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("COMMON-403", "Access denied", HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND("COMMON-404", "Resource not found", HttpStatus.NOT_FOUND),
    BUSINESS_RULE_VIOLATION("COMMON-409", "Business rule violation", HttpStatus.CONFLICT),
    TENANT_HEADER_INVALID("TENANT-400", "Workspace header is invalid", HttpStatus.BAD_REQUEST),
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
