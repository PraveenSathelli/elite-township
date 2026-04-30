package com.elite.township;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/demo")
@RequiredArgsConstructor
public class DemoController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> allOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable int id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping("/orders/add")
    public ResponseEntity<Order> addOrder(@RequestBody Order order) {
        return ResponseEntity.ok(orderService.addOrder(order));
    }

    @PostMapping("/orders/addMultiple")
    public ResponseEntity<List<Order>> addMultipleOrder(@RequestBody Order order) {
        int added = 0;
        List<Order> orders = new ArrayList<>();
        while(added < 10) {
            try
            {
                Thread.sleep(200); // Simulate delay
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
            order.setId( orderService.getAllOrders().size() + 1); // Simple way to generate unique ID
            order.setDescription(order.getDescription() + " #" + (LocalDateTime.now())); // Append number to description for uniqueness
            orders.add( orderService.addOrder(order));
            added++;
        }

        return ResponseEntity.ok(orders);
    }
}