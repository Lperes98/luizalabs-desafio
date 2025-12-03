package com.luizalabs.orders.dataprovider.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.luizalabs.orders.dataprovider.data.OrderData;
import com.luizalabs.orders.dataprovider.data.ProductData;
import com.luizalabs.orders.dataprovider.table.UserOrderTable;
import com.luizalabs.orders.domain.entity.Order;
import com.luizalabs.orders.domain.entity.Product;
import com.luizalabs.orders.domain.entity.User;
import com.luizalabs.orders.domain.exception.FileProcessingException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserOrderDataMapper {

    private static final ObjectMapper json =
            new ObjectMapper().registerModule(new JavaTimeModule());

    public UserOrderTable toTable(User user) {
        try {
            var ordersData = user.getOrders().stream().map(this::toOrderData).toList();
            String ordersJson = json.writeValueAsString(ordersData);

            return UserOrderTable.builder()
                    .userId(user.getUserId())
                    .name(user.getName())
                    .orders(ordersJson)
                    .build();

        } catch (JsonProcessingException e) {
            throw new FileProcessingException("Erro ao serializar JSON", e);
        }
    }

    public User toDomain(UserOrderTable table) {
        try {
            OrderData[] ordersArray = json.readValue(table.getOrders(), OrderData[].class);
            List<Order> orders = List.of(ordersArray).stream().map(this::toOrder).toList();

            return User.builder()
                    .userId(table.getUserId())
                    .name(table.getName())
                    .orders(orders)
                    .build();

        } catch (JsonProcessingException e) {
            throw new FileProcessingException("Erro ao desserializar JSON", e);
        }
    }

    private OrderData toOrderData(Order order) {
        return OrderData.builder()
                .orderId(order.getOrderId())
                .total(order.getTotal())
                .date(order.getDate())
                .products(order.getProducts().stream().map(this::toProductData).toList())
                .build();
    }

    private ProductData toProductData(Product product) {
        return ProductData.builder()
                .productId(product.getProductId())
                .value(product.getValue())
                .build();
    }

    private Order toOrder(OrderData data) {
        return Order.builder()
                .orderId(data.getOrderId())
                .total(data.getTotal())
                .date(data.getDate())
                .products(data.getProducts().stream().map(this::toProduct).toList())
                .build();
    }

    private Product toProduct(ProductData data) {
        return Product.builder().productId(data.getProductId()).value(data.getValue()).build();
    }
}
