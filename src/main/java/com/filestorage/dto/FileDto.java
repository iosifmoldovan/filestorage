package com.filestorage.dto;

/**
 * DTO for representing file information in API responses.
 */
public class FileDto {
    private String fileName;

    public FileDto(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
