package com.empresa.demo.service;

import org.springframework.stereotype.Service;

import com.empresa.demo.model.OrderEntity;
import com.empresa.demo.repository.OrderRepository;

@Service
public class OrderService {
    
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderEntity buscaPedidoPorId(Long id) {
        if (orderRepository.findById(id).isPresent())
        {
            return orderRepository.findById(id).get();
        }
        return null;
    }
}
