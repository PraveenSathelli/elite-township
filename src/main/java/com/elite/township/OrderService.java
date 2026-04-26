package com.elite.township;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private List<Order > orders = new ArrayList<>();

    public  Order addOrder(Order order){
        System.out.println("Adding order method called");
        orders.add(order);
        return order;
    }

    public List<Order> getAllOrders() {
        System.out.println("Getting all orders method called");
        return orders;
    }

    public Order getOrderById(int id) {
        System.out.println("Getting order by id method called with id: " + id);
        return orders.stream()
                .filter(order -> order.getId() == id)
                .findFirst()
                .orElse(null);
    }

}
