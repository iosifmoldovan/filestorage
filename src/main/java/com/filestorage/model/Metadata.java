package com.filestorage.model;

/**
 * Represents the Metadata for pagination
 */
public class Metadata {
    BasePagination pagination;

    public Metadata() {
    }

    public Metadata(Integer totalRecords, Integer page, Integer size) {
        this.pagination = new BasePagination(totalRecords, page, size);
    }

    public BasePagination getPagination() {
        return pagination;
    }

    public void setPagination(BasePagination pagination) {
        this.pagination = pagination;
    }
}
