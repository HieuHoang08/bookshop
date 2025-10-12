package com.hh.Job.controller;


import com.hh.Job.domain.Cart;
import com.hh.Job.domain.Order;
import com.hh.Job.domain.request.ReqOrderDTO;
import com.hh.Job.domain.request.ReqUpdateOrderDTO;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.order.ResOrderDTO;
import com.hh.Job.service.OrderService;
import com.hh.Job.util.annotation.APImessage;
import com.hh.Job.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    @APImessage("Create a order")
    public ResponseEntity<ResOrderDTO> createOrder(@RequestBody ReqOrderDTO reqOrderDTO)
            throws IdInvalidException {
        Order savedOrder = orderService.createOrder(reqOrderDTO);
        ResOrderDTO resOrderDTO = orderService.toResOrderDTO(savedOrder);
        return ResponseEntity.ok(resOrderDTO);
    }

    @GetMapping("/orders/{id}")
    @APImessage("Get order by ID")
    public ResponseEntity<ResOrderDTO> getOrderById(@PathVariable Long id)
            throws IdInvalidException {
        Order order = orderService.getOrderById(id);
        ResOrderDTO resOrderDTO = orderService.toResOrderDTO(order);
        return ResponseEntity.ok(resOrderDTO);
    }

    @GetMapping("/orders/user/{userId}")
    @APImessage("Get all orders by user ID")
    public ResponseEntity<ResultPaginationDTO<ResOrderDTO>> getAllOrdersByUser(
            @PathVariable Long userId,
            @Filter Specification<Order> specification,
            Pageable pageable
    ) throws IdInvalidException {

        ResultPaginationDTO<ResOrderDTO> result = orderService.getAllOrdersByUserId(userId, specification, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/orders")
    @APImessage("fetch all order")
    public ResponseEntity<ResultPaginationDTO> getAllOrders(
            @Filter Specification<Order> spec,
            Pageable pageable
    ){
        return ResponseEntity.status(HttpStatus.OK).body(this.orderService.fetchAllOrder(spec, pageable));
    }

    @DeleteMapping("/orders/{id}")
    @APImessage("delete an orders")
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") Long id) throws IdInvalidException {
        this.orderService.handleDeleteOrder(id);
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("orders/users/{userId}")
    @APImessage("Đã xóa tất cả đơn hàng của user ID nay")
    public ResponseEntity<Void> deleteOrdersByUserId(@PathVariable Long userId) throws IdInvalidException {
        orderService.deleteOrdersByUserId(userId);
        return ResponseEntity.ok(null);
    }

    @PutMapping("/orders/{id}")
    @APImessage("Update  a orders ")
    public ResponseEntity<ResOrderDTO> updateOrder(
            @PathVariable Long id,
            @RequestBody ReqUpdateOrderDTO reqUpdateOrderDTO) throws IdInvalidException {

        ResOrderDTO updatedOrder = orderService.handleUpdateOrder(id, reqUpdateOrderDTO);
        return ResponseEntity.ok(updatedOrder);
    }
}