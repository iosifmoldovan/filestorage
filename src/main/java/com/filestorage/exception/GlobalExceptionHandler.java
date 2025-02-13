package com.filestorage.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.filestorage.model.BaseResponse;
import com.filestorage.model.ResponseException;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;

/**
 * Global exception handler for handling all API errors in a structured format.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles requests to non-existent endpoints (404 Not Found).
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponse<ResponseException>> handleNoResourceFoundException(
            HttpRequestMethodNotSupportedException ex) {
        return buildErrorResponse(
                "RESOURCE_NOT_FOUND",
                "This endpoint does not exist. Check the URL and try again.",
                HttpStatus.NOT_FOUND);
    }

    /**
     * Handles MultipartException (e.g., when trying to upload a file without
     * multipart request).
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<BaseResponse<ResponseException>> handleMultipartException(MultipartException ex) {
        logger.warn("GlobalExceptionHandler.handleMultipartException(): {}", ex.getMessage());

        return buildErrorResponse(
                "INVALID_MULTIPART_REQUEST",
                "Expected a file upload, but the request was not multipart. Make sure you're sending 'multipart/form-data'.",
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles missing request parameters.
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<BaseResponse<ResponseException>> handleMissingFileException(
            MissingServletRequestPartException ex) {
        logger.warn("GlobalExceptionHandler.handleMissingFileException(): {}", ex.getMessage());

        return buildErrorResponse(
                "MISSING_FILE",
                "No file was uploaded. Make sure your request includes a 'file' field.",
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles missing request parameters.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<ResponseException>> handleMissingParamException(
            MissingServletRequestParameterException ex) {
        logger.error("GlobalExceptionHandler.handleMissingParamException(): {}", ex.getMessage());

        return buildErrorResponse(
                "MISSING_PARAMETER",
                "Missing required parameter: " + ex.getParameterName() + ".",
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles file not found exceptions (e.g., when attempting to retrieve a
     * missing file).
     */
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<BaseResponse<ResponseException>> handleFileNotFoundException(FileNotFoundException ex) {
        logger.warn("GlobalExceptionHandler.handleFileNotFoundException(): {}", ex.getMessage());

        return buildErrorResponse(
                "FILE_NOT_FOUND",
                "The requested file was not found.",
                HttpStatus.NOT_FOUND);
    }

    /**
     * Handles illegal argument exceptions (e.g., validation failures).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<ResponseException>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error("GlobalExceptionHandler.handleIllegalArgumentException(): {}", ex.getMessage());

        return buildErrorResponse(
                "INVALID_REQUEST",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles generic runtime exceptions.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse<ResponseException>> handleRuntimeException(RuntimeException ex) {
        logger.error("GlobalExceptionHandler.handleRuntimeException(): {}", ex.getMessage(), ex);

        return buildErrorResponse(
                "APPLICATION_ERROR",
                "Something went wrong. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles all unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<ResponseException>> handleGenericException(Exception ex) {
        logger.error("GlobalExceptionHandler.handleGenericException(): Unexpected error", ex);

        return buildErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "Something went wrong. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Utility method to build a structured error response using ApiResponse and
     * ErrorResponse.
     */
    private ResponseEntity<BaseResponse<ResponseException>> buildErrorResponse(String code, String message,
            HttpStatus status) {
        List<ResponseException> apiError = Collections.singletonList(new ResponseException(code, message));

        BaseResponse<ResponseException> errorResponse = new BaseResponse<>(apiError);
        return new ResponseEntity<>(errorResponse, status);
    }
}
