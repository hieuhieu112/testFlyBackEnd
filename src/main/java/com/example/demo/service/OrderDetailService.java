package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderDetail;
import com.example.demo.entity.Product;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.OrderDetailRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderDetailService {

    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public List<OrderDetail> getAllOrderDetails() {
        return orderDetailRepository.findAll();
    }

    public OrderDetail getOrderDetailById(Long id) {
        return orderDetailRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("OrderDetail not found with id: " + id));
    }

    public OrderDetail createOrderDetail(OrderDetail orderDetail) {
        if (orderDetail.getOrder() == null || orderDetail.getOrder().getId() == null) {
            throw new IllegalArgumentException("Order ID is required");
        }
        if (orderDetail.getProduct() == null || orderDetail.getProduct().getId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        Order order = orderRepository.findById(orderDetail.getOrder().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderDetail.getOrder().getId()));
        Product product = productRepository.findById(orderDetail.getProduct().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + orderDetail.getProduct().getId()));
        
        orderDetail.setOrder(order);
        orderDetail.setProduct(product);
        return orderDetailRepository.save(orderDetail);
    }

    public OrderDetail updateOrderDetail(Long id, OrderDetail orderDetailDetails) {
        OrderDetail orderDetail = getOrderDetailById(id);
        
        if (orderDetailDetails.getOrder() != null && orderDetailDetails.getOrder().getId() != null) {
            Order order = orderRepository.findById(orderDetailDetails.getOrder().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderDetailDetails.getOrder().getId()));
            orderDetail.setOrder(order);
        }
        
        if (orderDetailDetails.getProduct() != null && orderDetailDetails.getProduct().getId() != null) {
            Product product = productRepository.findById(orderDetailDetails.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + orderDetailDetails.getProduct().getId()));
            orderDetail.setProduct(product);
        }
        
        orderDetail.setQuantity(orderDetailDetails.getQuantity());
        orderDetail.setPrice(orderDetailDetails.getPrice());
        
        return orderDetailRepository.save(orderDetail);
    }

    public void deleteOrderDetail(Long id) {
        OrderDetail orderDetail = getOrderDetailById(id);
        orderDetailRepository.delete(orderDetail);
    }
}
