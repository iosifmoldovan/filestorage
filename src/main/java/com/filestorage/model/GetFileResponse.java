package com.filestorage.model;

import java.util.List;

import com.filestorage.dto.FileDto;

/**
 * DTO for representing a list of files in API responses.
 */
public class GetFileResponse {
    private List<FileDto> files;

    public GetFileResponse(List<FileDto> files) {
        this.files = files;
    }

    public List<FileDto> getFiles() {
        return files;
    }

    public void setFiles(List<FileDto> files) {
        this.files = files;
    }
}
