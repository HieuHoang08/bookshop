package com.hh.Job.service;

import com.hh.Job.domain.Book;
import com.hh.Job.domain.Cart;
import com.hh.Job.domain.CartDetail;
import com.hh.Job.domain.User;
import com.hh.Job.domain.constant.CartEnum;
import com.hh.Job.domain.request.ReqUpdateCartDTO;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.cart.CartDTO;
import com.hh.Job.domain.response.cart.CartDetailDTO;
import com.hh.Job.domain.response.cart.ResCreateCart;
import com.hh.Job.domain.response.cart.ResUpdateCartDTO;
import com.hh.Job.repository.BookRepository;
import com.hh.Job.repository.CartDetailRepository;
import com.hh.Job.repository.CartRepository;
import com.hh.Job.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final CartDetailRepository cartDetailRepository;


    public CartService(CartRepository cartRepository,
                       UserRepository userRepository,
                       BookRepository bookRepository,
                       CartDetailRepository cartDetailRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.cartDetailRepository = cartDetailRepository;
    }

    public CartDTO createCart(ResCreateCart request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Book mainBook = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Main book not found"));

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setBook(mainBook);
        cart.setQuantity(request.getQuantity());
        cart.setStatus(request.getStatus());
        cart.setCartType(request.getCartType());

        List<CartDetail> details = new ArrayList<>();
        for (ResCreateCart.ResCreateCartDetail detailRequest : request.getCartDetails()) {
            Book book = bookRepository.findById(detailRequest.getBookId())
                    .orElseThrow(() -> new RuntimeException("Book not found in detail"));

            CartDetail detail = new CartDetail();
            detail.setCart(cart);
            detail.setBook(book);
            detail.setQuantity(detailRequest.getQuantity());
            details.add(detail);
        }

        cart.setCartDetails(details);
        cartRepository.save(cart);
        cartDetailRepository.saveAll(details);

        return toCartDto(cart);
    }

    public CartDTO toCartDto(Cart cart) {
        List<CartDetailDTO> detailDtos = cart.getCartDetails().stream()
                .map(this::toCartDetailDto)
                .collect(Collectors.toList());

        return new CartDTO(
                cart.getId(),
                cart.getQuantity(),
                cart.getStatus(),
                cart.getCartType(),
                cart.getUser().getId(),
                cart.getUser().getName(),
                detailDtos,
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }

    private CartDetailDTO toCartDetailDto(CartDetail detail) {
        Book book = detail.getBook();
        return new CartDetailDTO(
                detail.getId(),
                detail.getQuantity(),
                book.getId(),
                book.getTitle(),
                book.getPrice(),
                detail.getCreatedAt(),
                detail.getUpdatedAt()
        );
    }


    public Optional<Cart> handleGetCart(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return cartRepository.findById(id);
    }

    public ResUpdateCartDTO handleUpdateCart(Long id, ReqUpdateCartDTO request) {
        Optional<Cart> optionalCart = cartRepository.findById(id);
        if (optionalCart.isEmpty()) {
            return null;
        }

        Cart cart = optionalCart.get();

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
            userRepository.findById(request.getUser().getId()).ifPresent(cart::setUser);
        }

        // Update book
        if (request.getBook() != null && request.getBook().getId() != null) {
            bookRepository.findById(request.getBook().getId()).ifPresent(cart::setBook);
        }

        // Save sẽ tự động trigger @PreUpdate
        Cart updatedCart = cartRepository.save(cart);

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
        Page<Cart> pageCart = cartRepository.findAll(spec, pageable);

        ResultPaginationDTO res = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(pageCart.getTotalElements());
        meta.setPages(pageCart.getTotalPages());
        res.setMeta(meta);

        // ✅ Chuyển sang DTO để tránh vòng lặp
        List<CartDTO> cartDtos = pageCart.getContent().stream()
                .map(this::toCartDto) // dùng phương thức đã có
                .collect(Collectors.toList());

        res.setResult(cartDtos);
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