package com.example.dialogflow.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Builder
public class Pagination {

    private long page;
    private long pageSize;
    private long totalPages;
    private long totalCount;

    public Pagination(long page, long pageSize, long totalPages, long totalCount) {
        super();
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.totalCount = totalCount;
    }
}

