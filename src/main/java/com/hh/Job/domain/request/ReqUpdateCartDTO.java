package com.hh.Job.domain.request;

import com.hh.Job.domain.constant.CartEnum;
import com.hh.Job.domain.constant.CartType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateCartDTO {
    private Integer quantity;
    private CartEnum status;
    private CartType cartType;
    private UserDTO user;
    private BookDTO book;

    @Getter
    @Setter
    public static class UserDTO {
        private Long id;
    }

    @Getter
    @Setter
    public static class BookDTO {
        private Long id;
    }
}