package com.hh.Job.service;


import com.hh.Job.repository.BorrowTransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class BorrowTransactionService {

    private final BorrowTransactionRepository borrowTransactionRepository;

    public BorrowTransactionService(BorrowTransactionRepository borrowTransactionRepository) {
        this.borrowTransactionRepository = borrowTransactionRepository;
    }
}
