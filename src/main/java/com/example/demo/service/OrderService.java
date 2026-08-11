package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public Order createOrder(Order order) {
        if (order.getUser() == null || order.getUser().getId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        User user = userRepository.findById(order.getUser().getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + order.getUser().getId()));
        order.setUser(user);
        return orderRepository.save(order);
    }

    public Order updateOrder(Long id, Order orderDetails) {
        Order order = getOrderById(id);
        if (orderDetails.getUser() != null && orderDetails.getUser().getId() != null) {
            User user = userRepository.findById(orderDetails.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + orderDetails.getUser().getId()));
            order.setUser(user);
        }
        order.setTotalAmount(orderDetails.getTotalAmount());
        order.setStatus(orderDetails.getStatus());
        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        Order order = getOrderById(id);
        orderRepository.delete(order);
    }
}
