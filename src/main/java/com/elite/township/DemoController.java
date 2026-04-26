package com.elite.township;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/demo")
@RequiredArgsConstructor
public class DemoController {
//    Logger logger = LogManager.getLogger(SplunkDemoController.class);
    @Autowired
    private final OrderService orderService;

        @RequestMapping("/orders")
        public ResponseEntity<List<Order>> allOrders() {
            return ResponseEntity.ok(orderService.getAllOrders());
        }

    @RequestMapping("/orders/{id}")
    public ResponseEntity<Order> getOrderById(int id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @RequestMapping( path = "/orders/add" , method = RequestMethod.POST)
    public ResponseEntity<Order> addOrder(Order order) {
            Order addedOrder = orderService.addOrder(order);
        return ResponseEntity.ok(addedOrder);
    }

}
