package com.luizalabs.orders.domain.mapper;

import static java.math.RoundingMode.HALF_UP;
import static java.util.stream.Collectors.toList;

import com.luizalabs.orders.domain.dto.OrderDTO;
import com.luizalabs.orders.domain.dto.ProductDTO;
import com.luizalabs.orders.domain.dto.UserOrderResponseDTO;
import com.luizalabs.orders.domain.entity.Order;
import com.luizalabs.orders.domain.entity.Product;
import com.luizalabs.orders.domain.entity.User;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DomainMapper {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<UserOrderResponseDTO> toResponseDTO(List<User> users) {
        if (users == null) {
            return List.of();
        }
        return users.stream().map(this::toResponseDTO).collect(toList());
    }

    public UserOrderResponseDTO toResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserOrderResponseDTO.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .orders(toOrderDTOs(user.getOrders()))
                .build();
    }

    private List<OrderDTO> toOrderDTOs(List<Order> orders) {
        if (orders == null) {
            return List.of();
        }
        return orders.stream().map(this::toOrderDTO).collect(toList());
    }

    private OrderDTO toOrderDTO(Order order) {
        if (order == null) {
            return null;
        }

        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .total(formatDecimal(order.getTotal()))
                .date(formatDate(order.getDate()))
                .products(toProductDTOs(order.getProducts()))
                .build();
    }

    private List<ProductDTO> toProductDTOs(List<Product> products) {
        if (products == null) {
            return List.of();
        }
        return products.stream().map(this::toProductDTO).collect(toList());
    }

    private ProductDTO toProductDTO(Product product) {
        if (product == null) {
            return null;
        }

        return ProductDTO.builder()
                .productId(product.getProductId())
                .value(formatDecimal(product.getValue()))
                .build();
    }

    private String formatDecimal(java.math.BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, HALF_UP).toString();
    }

    private String formatDate(java.time.LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }
}
