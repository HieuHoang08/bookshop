package com.hh.Job.service;

import com.hh.Job.domain.Book;
import com.hh.Job.domain.Cart;
import com.hh.Job.domain.User;
import com.hh.Job.domain.request.ReqUpdateCartDTO;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.cart.ResUpdateCartDTO;
import com.hh.Job.repository.BookRepository;
import com.hh.Job.repository.CartRepository;
import com.hh.Job.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;


    public CartService(CartRepository cartRepository,
                       UserRepository userRepository,
                       BookRepository bookRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    public Cart handleCreateCart(Cart cart) {
        return cartRepository.save(cart);
    }

    public Optional<Cart> handleGetCart(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return cartRepository.findById(id);
    }

    public ResUpdateCartDTO handleUpdateCart(Long id, ReqUpdateCartDTO request) {
        Cart cart = cartRepository.findById(id).get();

        if (cart == null) {
            return null;
        }

        // Update quantity
        if (request.getQuantity() != null && request.getQuantity() > 0) {
            cart.setQuantity(request.getQuantity());
        }

        // Update status
        if (request.getStatus() != null) {
            cart.setStatus(request.getStatus());
        }

        // Update cartType
        if (request.getCartType() != null) {
            cart.setCartType(request.getCartType());
        }

        // Update user
        if (request.getUser() != null && request.getUser().getId() != null) {
            User user = userRepository.findById(request.getUser().getId()).orElse(null);
            if (user != null) {
                cart.setUser(user);
            }
        }

        // Update book
        if (request.getBook() != null && request.getBook().getId() != null) {
            Book book = bookRepository.findById(request.getBook().getId()).orElse(null);
            if (book != null) {
                cart.setBook(book);
            }
        }

        // Save sẽ tự động trigger @PreUpdate
        Cart updatedCart = cartRepository.save(cart);

        // Convert sang DTO response
        return convertToResDTO(updatedCart);
    }

    private ResUpdateCartDTO convertToResDTO(Cart cart) {
        ResUpdateCartDTO res = new ResUpdateCartDTO();
        res.setId(cart.getId());
        res.setQuantity(cart.getQuantity());
        res.setStatus(cart.getStatus());
        res.setCartType(cart.getCartType());
        res.setUpdatedAt(cart.getUpdatedAt());
        res.setUpdatedBy(cart.getUpdatedBy());

        if (cart.getUser() != null) {
            res.setUserId(cart.getUser().getId());
            res.setUserName(cart.getUser().getName());
        }

        if (cart.getBook() != null) {
            res.setBookId(cart.getBook().getId());
            res.setBookTitle(cart.getBook().getTitle());
        }

        return res;
    }

        public ResultPaginationDTO fetchAllCart(Specification<Cart> spec, Pageable pageable) {
            Page<Cart> pageCart = this.cartRepository.findAll(spec, pageable);
            ResultPaginationDTO res = new ResultPaginationDTO();
            ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

            meta.setPage(pageable.getPageNumber() + 1);
            meta.setPageSize(pageable.getPageSize());
            meta.setTotal(pageCart.getTotalElements());
            meta.setPages(pageCart.getTotalPages());

            res.setMeta(meta);
            res.setResult(pageCart.getContent());
            return res;
        }

        public void handleDeteleCart(Long id) {
        Optional<Cart> optionalCart = this.cartRepository.findById(id);
        if (optionalCart.isPresent()) {
            Cart cart = optionalCart.get();
            cartRepository.delete(cart);
            }
        }
}
