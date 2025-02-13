package com.filestorage.model;

import java.util.List;

/**
 * Standardized API response wrapper for all endpoints.
 */
public class BaseResponse<T> {
    private T data;
    List<ResponseException> exceptions;

    public BaseResponse(T data) {
        this.data = data;
        this.exceptions = null;
    }

    public BaseResponse(List<ResponseException> exceptions) {
        this.data = null;
        this.exceptions = exceptions;
    }

    public T getData() {
        return data;
    }

    public List<ResponseException> getExceptions() {
        return exceptions;
    }
}
