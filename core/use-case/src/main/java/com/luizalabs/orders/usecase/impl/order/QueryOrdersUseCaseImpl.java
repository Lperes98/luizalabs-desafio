package com.luizalabs.orders.usecase.impl.order;

import com.luizalabs.orders.domain.dto.UserOrderResponseDTO;
import com.luizalabs.orders.domain.mapper.DomainMapper;
import com.luizalabs.orders.usecase.port.QueryOrdersUseCase;
import com.luizalabs.orders.usecase.port.data.OrderDataPort;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryOrdersUseCaseImpl implements QueryOrdersUseCase {

    private final OrderDataPort orderData;
    private final DomainMapper mapper = new DomainMapper();

    @Override
    @Transactional(readOnly = true)
    public List<UserOrderResponseDTO> findAll() {
        log.info("Buscando todos");
        return mapper.toResponseDTO(orderData.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserOrderResponseDTO> findByOrderId(Long orderId) {
        log.info("Buscando order_id: {}", orderId);
        return mapper.toResponseDTO(orderData.findByOrderId(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserOrderResponseDTO> findByDateRange(LocalDate start, LocalDate end) {
        log.info("Buscando entre {} e {}", start, end);
        return mapper.toResponseDTO(orderData.findByOrderDateBetween(start, end));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserOrderResponseDTO> findByStartDate(LocalDate start) {
        log.info("Buscando start_date {}", start);
        return mapper.toResponseDTO(orderData.findByOrderStartDate(start));
    }
}
