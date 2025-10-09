package com.hh.Job.controller;


import com.hh.Job.domain.BorrowTransaction;
import com.hh.Job.domain.Order;
import com.hh.Job.domain.Publisher;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.borrow.BorrowOrderDTO;
import com.hh.Job.domain.response.borrow.ResBorrowDTO;
import com.hh.Job.service.BorrowTransactionService;
import com.hh.Job.util.annotation.APImessage;
import com.hh.Job.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class BorrowTransactionController {

    private final BorrowTransactionService borrowTransactionService;
    public BorrowTransactionController(BorrowTransactionService borrowTransactionService) {
        this.borrowTransactionService = borrowTransactionService;
    }

    @PostMapping("/borrows")
    @APImessage("create a borrows")
    public ResponseEntity<ResBorrowDTO> createBorrow(@RequestBody BorrowOrderDTO borrowTr)
            throws IdInvalidException {

        ResBorrowDTO createBr = borrowTransactionService.createBorrow(borrowTr);
        return ResponseEntity.status(HttpStatus.CREATED).body(createBr);
    }


    @PutMapping("/borrows/{id}")
    @APImessage("update a borrows")
    public ResponseEntity<ResBorrowDTO> updateBorrow(
            @RequestBody BorrowTransaction borrowTransaction,
            @PathVariable("id") Long id)
            throws IdInvalidException {
        borrowTransaction.setId(id);
        ResBorrowDTO updateBr = borrowTransactionService.updateBorrow(borrowTransaction);
        if(updateBr == null){
            throw new IdInvalidException("Borrow transaction id" + borrowTransaction.getId() + " not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(updateBr);
    }

    @GetMapping("/borrows/{id}")
    @APImessage("fetch borrows byId")
    public ResponseEntity<ResBorrowDTO> fetchBorrows(@PathVariable("id") Long id)
            throws IdInvalidException {

        ResBorrowDTO dto = borrowTransactionService.findBorrowById(id);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }


    @DeleteMapping("/borrows/{id}")
    @APImessage("delete a borrows")
    public ResponseEntity<Object> deleteBorrows(@PathVariable("id") Long id)
            throws IdInvalidException {
        BorrowTransaction br = borrowTransactionService.findById(id);
        if(br == null){
            throw new IdInvalidException("Borrow transaction id" + id + " not found");
        }
        borrowTransactionService.deleteBorrow(id);
        return ResponseEntity.ok().body("delete success");
    }

    @GetMapping("/borrows")
    @APImessage("fetch all borrows")
    public ResponseEntity<ResultPaginationDTO> fetchAllBorrows(
            @Filter Specification<BorrowTransaction> spec,
            Pageable pageable
            ){
        ResultPaginationDTO result = borrowTransactionService.fetchAllTransactions(spec,pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
