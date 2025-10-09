package com.hh.Job.domain.response.borrow;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BorrowOrderDTO {
    private Long orderId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

}
