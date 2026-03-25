package com.capg.jobportal.dto;


import java.util.List;

public class PagedResponse<T> {

    private List<T> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean isLast;

    public PagedResponse(List<T> content, int currentPage,
                         int totalPages, long totalElements, boolean isLast) {
        this.content = content;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.isLast = isLast;
    }

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public boolean isLast() { return isLast; }
    public void setLast(boolean last) { isLast = last; }
}