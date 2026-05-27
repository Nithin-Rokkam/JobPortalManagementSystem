package com.capg.jobportal.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/*
 * Generic paginated response DTO. Uses @Getter/@Setter since it
 * has a custom all-args constructor used throughout the service.
 */
@Getter
@Setter
public class PagedResponse<T> {

    private List<T> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean isLast;

    public PagedResponse(List<T> content, int currentPage,
                         int totalPages, long totalElements, boolean isLast) {
        this.content       = content;
        this.currentPage   = currentPage;
        this.totalPages    = totalPages;
        this.totalElements = totalElements;
        this.isLast        = isLast;
    }
}
