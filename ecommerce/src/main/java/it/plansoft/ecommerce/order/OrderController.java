package it.plansoft.ecommerce.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody OrderValue orderValue) {
        Order order = orderService.createOrder(orderValue);
        return new ResponseEntity(order, HttpStatus.CREATED);
    }

    @PatchMapping("/{orderId}/checkout")
    public ResponseEntity<Order> checkout(@PathVariable Long orderId) {
        Order order = orderService.checkoutOrder(orderId);
        return new ResponseEntity(order, HttpStatus.OK);
    }
}
