package com.filestorage.model;

/**
 * Represents API error responses with code and message.
 */
public class ResponseException {

    private String errorCode = "";
    private String exceptionSource;
    private String exceptionMessage;

    public ResponseException() {
    }

    public ResponseException(String exceptionSource, String exceptionMessage) {
        this.exceptionSource = exceptionSource;
        this.exceptionMessage = exceptionMessage;
    }

    public ResponseException(String exceptionSource, String exceptionMessage, String errorCode) {
        this.exceptionSource = exceptionSource;
        this.exceptionMessage = exceptionMessage;
        this.errorCode = errorCode;
    }

    public String getExceptionSource() {
        return exceptionSource;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setExceptionSource(String exceptionSource) {
        this.exceptionSource = exceptionSource;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
}
