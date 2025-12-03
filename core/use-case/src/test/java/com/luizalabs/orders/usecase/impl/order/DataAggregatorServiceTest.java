package com.luizalabs.orders.usecase.impl.order;

import static org.assertj.core.api.Assertions.*;

import com.luizalabs.orders.domain.dto.ParsedLine;
import com.luizalabs.orders.domain.entity.Order;
import com.luizalabs.orders.domain.entity.Product;
import com.luizalabs.orders.domain.entity.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DataAggregatorService - Testes Unitários")
class DataAggregatorServiceTest {

    private DataAggregatorService service;

    @BeforeEach
    void setUp() {
        service = new DataAggregatorService();
    }

    @Test
    @DisplayName("Deve retornar lista vazia para entrada nula")
    void shouldReturnEmptyListForNullInput() {
        List<User> result = service.aggregate(null);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar lista vazia para entrada vazia")
    void shouldReturnEmptyListForEmptyInput() {
        List<User> result = service.aggregate(List.of());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve agregar uma única linha corretamente")
    void shouldAggregateSingleLine() {
        ParsedLine line =
                ParsedLine.builder()
                        .userId(1L)
                        .userName("Zarelli")
                        .orderId(123L)
                        .productId(111L)
                        .value(new BigDecimal("512.24"))
                        .date(LocalDate.of(2021, 12, 1))
                        .build();

        List<User> result = service.aggregate(List.of(line));

        assertThat(result).hasSize(1);

        User user = result.get(0);
        assertThat(user.getUserId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("Zarelli");
        assertThat(user.getOrders()).hasSize(1);

        Order order = user.getOrders().get(0);
        assertThat(order.getOrderId()).isEqualTo(123L);
        assertThat(order.getDate()).isEqualTo(LocalDate.of(2021, 12, 1));
        assertThat(order.getProducts()).hasSize(1);
        assertThat(order.getTotal()).isEqualByComparingTo("512.24");

        Product product = order.getProducts().get(0);
        assertThat(product.getProductId()).isEqualTo(111L);
        assertThat(product.getValue()).isEqualByComparingTo("512.24");
    }

    @Test
    @DisplayName("Deve agregar múltiplos produtos no mesmo pedido")
    void shouldAggregateMultipleProductsInSameOrder() {
        ParsedLine line1 =
                createLine(1L, "Zarelli", 123L, 111L, "512.24", LocalDate.of(2021, 12, 1));
        ParsedLine line2 =
                createLine(1L, "Zarelli", 123L, 122L, "256.12", LocalDate.of(2021, 12, 1));

        List<User> result = service.aggregate(List.of(line1, line2));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrders()).hasSize(1);

        Order order = result.get(0).getOrders().get(0);
        assertThat(order.getProducts()).hasSize(2);
        assertThat(order.getTotal()).isEqualByComparingTo("768.36");
    }

    @Test
    @DisplayName("Deve separar pedidos com mesmo ID mas datas diferentes")
    void shouldSeparateOrdersWithSameIdButDifferentDates() {
        ParsedLine line1 =
                createLine(1L, "Zarelli", 123L, 111L, "512.24", LocalDate.of(2021, 12, 1));
        ParsedLine line2 =
                createLine(1L, "Zarelli", 123L, 122L, "256.12", LocalDate.of(2021, 12, 2));

        List<User> result = service.aggregate(List.of(line1, line2));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrders()).hasSize(2);
    }

    @Test
    @DisplayName("Deve agregar múltiplos usuários")
    void shouldAggregateMultipleUsers() {
        ParsedLine line1 =
                createLine(1L, "Zarelli", 123L, 111L, "512.24", LocalDate.of(2021, 12, 1));
        ParsedLine line2 =
                createLine(2L, "Medeiros", 456L, 222L, "256.12", LocalDate.of(2021, 12, 1));

        List<User> result = service.aggregate(List.of(line1, line2));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getUserId).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("Deve agregar cenário complexo: múltiplos usuários, pedidos e produtos")
    void shouldAggregateComplexScenario() {
        List<ParsedLine> lines =
                List.of(
                        createLine(1L, "Zarelli", 123L, 111L, "512.24", LocalDate.of(2021, 12, 1)),
                        createLine(1L, "Zarelli", 123L, 122L, "256.12", LocalDate.of(2021, 12, 1)),
                        createLine(1L, "Zarelli", 456L, 333L, "100.00", LocalDate.of(2021, 12, 2)),
                        createLine(2L, "Medeiros", 789L, 444L, "50.50", LocalDate.of(2021, 12, 1)));

        List<User> result = service.aggregate(lines);

        assertThat(result).hasSize(2);

        User user1 =
                result.stream().filter(u -> u.getUserId().equals(1L)).findFirst().orElseThrow();
        assertThat(user1.getOrders()).hasSize(2);

        Order order1 =
                user1.getOrders().stream()
                        .filter(o -> o.getOrderId().equals(123L))
                        .findFirst()
                        .orElseThrow();
        assertThat(order1.getProducts()).hasSize(2);
        assertThat(order1.getTotal()).isEqualByComparingTo("768.36");

        Order order2 =
                user1.getOrders().stream()
                        .filter(o -> o.getOrderId().equals(456L))
                        .findFirst()
                        .orElseThrow();
        assertThat(order2.getProducts()).hasSize(1);
        assertThat(order2.getTotal()).isEqualByComparingTo("100.00");

        User user2 =
                result.stream().filter(u -> u.getUserId().equals(2L)).findFirst().orElseThrow();
        assertThat(user2.getOrders()).hasSize(1);
    }

    private ParsedLine createLine(
            Long userId,
            String userName,
            Long orderId,
            Long productId,
            String value,
            LocalDate date) {
        return ParsedLine.builder()
                .userId(userId)
                .userName(userName)
                .orderId(orderId)
                .productId(productId)
                .value(new BigDecimal(value))
                .date(date)
                .build();
    }
}
