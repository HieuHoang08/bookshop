package com.hh.Job.controller;

import com.hh.Job.domain.Address;
import com.hh.Job.domain.Category;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.service.AddressService;
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
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/address")
    @APImessage("Create a new address")
    public ResponseEntity<Address> createAddress(@RequestBody Address address)
            throws IdInvalidException {
        Address createdAddress = addressService.createAddress(address);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAddress);
    }

    @GetMapping("/address/{id}")
    @APImessage("Fetch address by ID")
    public ResponseEntity<Address> fetchAddress(@PathVariable("id") Long id)
            throws IdInvalidException {
        Optional<Address> address = this.addressService.getAddressById(id);
        if (address.isEmpty()) {
            throw new IdInvalidException("Address with id " + id + " not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(address.get());
    }

    @PutMapping("/address/{id}")
    @APImessage("update a address")
    public ResponseEntity<Address> updateAddress(
            @PathVariable("id") Long id,
            @RequestBody Address address)
            throws IdInvalidException {
        address.setId(id);
        Address updatedAddress = addressService.updateAddress(address);
        if (updatedAddress == null) {
            throw new IdInvalidException("Address with id " + id + " not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(updatedAddress);
    }

    @DeleteMapping("/address/{id}")
    @APImessage("delete a address byId")
    public ResponseEntity<Object> deleteAddress(@PathVariable("id") Long id)
            throws IdInvalidException {
        Optional<Address> address = this.addressService.getAddressById(id);
        if (address.isEmpty()) {

            throw new IdInvalidException("Address with id " + id + " not found");
        }
        this.addressService.deleteAddress(id);
        return ResponseEntity.ok().body("delete addres successfully");
    }

    @GetMapping("/address")
    @APImessage("fetch all category")
    public ResponseEntity<ResultPaginationDTO> fetchAllAdress(
            @Filter Specification<Address> spec,
            Pageable pageable
    ) {
        ResultPaginationDTO result = addressService.fetchAllAddress(spec, pageable);
        return  ResponseEntity.ok(result);
    }
}
