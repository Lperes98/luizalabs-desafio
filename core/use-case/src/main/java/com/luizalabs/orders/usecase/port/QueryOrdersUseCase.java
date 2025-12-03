package com.luizalabs.orders.usecase.port;

import com.luizalabs.orders.domain.dto.UserOrderResponseDTO;
import java.time.LocalDate;
import java.util.List;

public interface QueryOrdersUseCase {

    List<UserOrderResponseDTO> findAll();

    List<UserOrderResponseDTO> findByOrderId(Long orderId);

    List<UserOrderResponseDTO> findByDateRange(LocalDate startDate, LocalDate endDate);

    List<UserOrderResponseDTO> findByStartDate(LocalDate startDate);
}
