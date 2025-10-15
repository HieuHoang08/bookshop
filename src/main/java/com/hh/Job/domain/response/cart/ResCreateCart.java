package com.hh.Job.domain.response.cart;

import com.hh.Job.domain.constant.CartEnum;
import com.hh.Job.domain.constant.CartType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor

@Getter
@Setter
public class ResCreateCart {
    private int quantity;
    private CartEnum status;
    private CartType cartType;

    private Long userId;
    private Long bookId;

    private List<ResCreateCartDetail> cartDetails;

   @Getter
   @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResCreateCartDetail {
        private int quantity;
        private Long bookId;
    }

}
