package com.luizalabs.orders.usecase.impl.order;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.luizalabs.orders.domain.dto.UserOrderResponseDTO;
import com.luizalabs.orders.domain.entity.User;
import com.luizalabs.orders.usecase.port.data.OrderDataPort;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("QueryOrdersUseCaseImpl - Testes Unitários")
class QueryOrdersUseCaseImplTest {

    @Mock private OrderDataPort orderData;

    private QueryOrdersUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new QueryOrdersUseCaseImpl(orderData);
    }

    @Test
    @DisplayName("Deve buscar todos os pedidos")
    void shouldFindAllOrders() {
        List<User> users = List.of(mock(User.class));

        when(orderData.findAll()).thenReturn(users);

        List<UserOrderResponseDTO> result = useCase.findAll();

        assertThat(result).isNotEmpty();
        verify(orderData).findAll();
    }

    @Test
    @DisplayName("Deve buscar pedidos por orderId")
    void shouldFindByOrderId() {
        Long orderId = 123L;
        List<User> users = List.of(mock(User.class));

        when(orderData.findByOrderId(orderId)).thenReturn(users);

        List<UserOrderResponseDTO> result = useCase.findByOrderId(orderId);

        assertThat(result).isNotEmpty();
        verify(orderData).findByOrderId(orderId);
    }

    @Test
    @DisplayName("Deve buscar pedidos por intervalo de datas")
    void shouldFindByDateRange() {
        LocalDate start = LocalDate.of(2021, 1, 1);
        LocalDate end = LocalDate.of(2021, 12, 31);
        List<User> users = List.of(mock(User.class));

        when(orderData.findByOrderDateBetween(start, end)).thenReturn(users);

        List<UserOrderResponseDTO> result = useCase.findByDateRange(start, end);

        assertThat(result).isNotEmpty();
        verify(orderData).findByOrderDateBetween(start, end);
    }

    @Test
    @DisplayName("Deve buscar pedidos por data de início")
    void shouldFindByStartDate() {
        LocalDate startDate = LocalDate.of(2021, 6, 1);
        List<User> users = List.of(mock(User.class));

        when(orderData.findByOrderStartDate(startDate)).thenReturn(users);

        List<UserOrderResponseDTO> result = useCase.findByStartDate(startDate);

        assertThat(result).isNotEmpty();
        verify(orderData).findByOrderStartDate(startDate);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não encontrar pedidos")
    void shouldReturnEmptyListWhenNoOrdersFound() {
        when(orderData.findAll()).thenReturn(List.of());

        List<UserOrderResponseDTO> result = useCase.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não encontrar pedidos por data de início")
    void shouldReturnEmptyListWhenNoOrdersFoundByStartDate() {
        LocalDate startDate = LocalDate.of(2021, 1, 1);
        when(orderData.findByOrderStartDate(startDate)).thenReturn(List.of());

        List<UserOrderResponseDTO> result = useCase.findByStartDate(startDate);

        assertThat(result).isEmpty();
        verify(orderData).findByOrderStartDate(startDate);
    }
}
