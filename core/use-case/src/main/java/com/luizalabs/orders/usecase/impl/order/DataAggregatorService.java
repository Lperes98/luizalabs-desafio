package com.luizalabs.orders.usecase.impl.order;

import static java.util.stream.Collectors.toList;

import com.luizalabs.orders.domain.dto.ParsedLine;
import com.luizalabs.orders.domain.entity.Order;
import com.luizalabs.orders.domain.entity.Product;
import com.luizalabs.orders.domain.entity.User;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DataAggregatorService {

    public List<User> aggregate(List<ParsedLine> lines) {
        if (lines == null || lines.isEmpty()) {
            return List.of();
        }

        return lines.stream()
                .collect(Collectors.groupingBy(ParsedLine::getUserId))
                .entrySet()
                .stream()
                .map(this::buildUser)
                .collect(toList());
    }

    private User buildUser(Map.Entry<Long, List<ParsedLine>> userEntry) {
        Long userId = userEntry.getKey();
        List<ParsedLine> userLines = userEntry.getValue();
        String userName = userLines.get(0).getUserName();

        List<Order> orders = groupByOrder(userLines);

        return User.builder().userId(userId).name(userName).orders(orders).build();
    }

    private List<Order> groupByOrder(List<ParsedLine> lines) {
        return lines.stream()
                .collect(
                        Collectors.groupingBy(
                                line -> new OrderKey(line.getOrderId(), line.getDate())))
                .values()
                .stream()
                .map(this::buildOrder)
                .collect(toList());
    }

    private Order buildOrder(List<ParsedLine> orderLines) {
        ParsedLine first = orderLines.get(0);

        List<Product> products =
                orderLines.stream()
                        .map(
                                line ->
                                        Product.builder()
                                                .productId(line.getProductId())
                                                .value(line.getValue())
                                                .build())
                        .collect(toList());

        Order order =
                Order.builder()
                        .orderId(first.getOrderId())
                        .date(first.getDate())
                        .products(products)
                        .build();

        order.setTotal(order.calculateTotal());

        return order;
    }

    private record OrderKey(Long orderId, LocalDate date) {}
}
