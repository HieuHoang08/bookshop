package com.hh.Job.controller;

import com.hh.Job.domain.Cart;
import com.hh.Job.domain.request.ReqUpdateCartDTO;
import com.hh.Job.domain.response.RestResponse;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.cart.CartDTO;
import com.hh.Job.domain.response.cart.ResCreateCart;
import com.hh.Job.domain.response.cart.ResUpdateCartDTO;
import com.hh.Job.service.CartService;
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
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/carts")
    @APImessage("create a cart")
    public ResponseEntity<RestResponse<CartDTO>> createCart(@RequestBody ResCreateCart request) {
        CartDTO cartDto = cartService.createCart(request);

        RestResponse<CartDTO> response = new RestResponse<>(
                HttpStatus.OK.value(),
                null,
                "Cart created successfully",
                cartDto
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/carts/{id}")
    @APImessage("fetch cart byId")
    public ResponseEntity<CartDTO> getCartById(@PathVariable("id") Long id)
            throws IdInvalidException {

        Optional<Cart> optionalCart = cartService.handleGetCart(id);

        // Nếu không tìm thấy cart => ném exception
        if (optionalCart.isEmpty()) {
            throw new IdInvalidException("Cart với ID " + id + " không tìm thấy");
        }


        CartDTO cartDto = cartService.toCartDto(optionalCart.get());

        // Nếu tìm thấy => trả về dữ liệu
        return ResponseEntity.ok(cartDto);
    }

    @PutMapping("/carts/{id}")
    @APImessage("update a cart")
    public ResponseEntity<ResUpdateCartDTO> updateCart(
            @PathVariable("id") Long id,
            @RequestBody ReqUpdateCartDTO req
    )throws IdInvalidException {
        ResUpdateCartDTO updatedCart = this.cartService.handleUpdateCart(id, req);
        if (updatedCart == null) {
            throw new IdInvalidException("Cart voi id" + id +"khong ton tai");
        }
        return ResponseEntity.ok(updatedCart);
    }

    @GetMapping("/carts")
    @APImessage("fetch all cart")
    public ResponseEntity<ResultPaginationDTO> getAllCarts(
            @Filter Specification<Cart> specification,
            Pageable pageable)throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.OK).body(this.cartService.fetchAllCart(specification, pageable));
    }

    @DeleteMapping("/carts/{id}")
    @APImessage("delete an carts")
    public ResponseEntity<RestResponse> deleteCart(@PathVariable("id") Long id)
            throws IdInvalidException {
        this.cartService.handleDeteleCart(id);
        return ResponseEntity.ok(null);
    }

}