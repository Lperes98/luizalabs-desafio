package com.luizalabs.orders.dataprovider.adapter;

import com.luizalabs.orders.dataprovider.mapper.UserOrderDataMapper;
import com.luizalabs.orders.dataprovider.repository.UserOrderRepository;
import com.luizalabs.orders.domain.entity.User;
import com.luizalabs.orders.usecase.port.data.OrderDataPort;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDataAdapter implements OrderDataPort {

    private final UserOrderRepository repository;
    private final UserOrderDataMapper mapper = new UserOrderDataMapper();

    @Override
    public void saveAll(List<User> users) {
        log.info("Salvando {} usuários", users.size());

        var tables = users.stream().map(mapper::toTable).toList();
        repository.saveAll(tables);

        log.info("Salvo com sucesso");
    }

    @Override
    public List<User> findAll() {
        log.info("Buscando todos os pedidos");
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<User> findByOrderId(Long orderId) {
        log.info("Buscando por order_id: {}", orderId);

        String json = String.format("[{\"order_id\": %d}]", orderId);
        return repository.findByOrderId(json).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<User> findByOrderDateBetween(LocalDate start, LocalDate end) {
        log.info("Buscando entre {} e {}", start, end);
        return repository.findByOrderDateBetween(start, end).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<User> findByOrderStartDate(LocalDate start) {
        log.info("Buscando start_date {}", start);
        return repository.findByOrderStartDate(start).stream().map(mapper::toDomain).toList();
    }
}
