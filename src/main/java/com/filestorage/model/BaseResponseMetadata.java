package com.filestorage.model;

import java.util.List;

/**
 * Standardized API response wrapper for all endpoints.
 */
public class BaseResponseMetadata<T> {
    T data;
    Metadata metadata;
    List<ResponseException> exceptions;

    public BaseResponseMetadata() {
    }

    public BaseResponseMetadata(T data) {
        this.data = data;
    }

    public BaseResponseMetadata(T data, List<ResponseException> exceptions) {
        this.data = data;
        this.exceptions = exceptions;
    }

    public BaseResponseMetadata(List<ResponseException> exceptions) {
        this.exceptions = exceptions;
        this.data = null;
        this.metadata = null;
    }

    public BaseResponseMetadata(T data, Metadata metadata, List<ResponseException> exceptions) {
        this.data = data;
        this.metadata = metadata;
        this.exceptions = exceptions;
    }

    public T getData() {
        return data;
    }

    public List<ResponseException> getExceptions() {
        return exceptions;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
}
