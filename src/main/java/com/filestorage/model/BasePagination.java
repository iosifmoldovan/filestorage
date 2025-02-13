package com.filestorage.model;

public class BasePagination {

    private int totalRecords;
    private int page;
    private int size;

    public BasePagination() {
    }

    public BasePagination(Integer totalRecords, Integer page, Integer size) {
        this.totalRecords = totalRecords;
        this.page = page;
        this.size = size;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
