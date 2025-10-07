package com.hh.Job.domain.response;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResultPaginationDTO<T> {
    private Meta meta;
//    private Object result;
    private List<T> result;

    @Getter
    @Setter
    public static class Meta {
        private int page;
        private int pageSize;
        private int pages;
        private long total;
    }
}
