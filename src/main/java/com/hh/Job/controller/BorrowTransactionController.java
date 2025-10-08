package com.hh.Job.controller;


import com.hh.Job.service.BorrowTransactionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BorrowTransactionController {

    private final BorrowTransactionService borrowTransactionService;
    public BorrowTransactionController(BorrowTransactionService borrowTransactionService) {
        this.borrowTransactionService = borrowTransactionService;
    }
}
