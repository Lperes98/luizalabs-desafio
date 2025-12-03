package com.luizalabs.orders.usecase.port.data;

import com.luizalabs.orders.domain.entity.User;
import java.time.LocalDate;
import java.util.List;

public interface OrderDataPort {

    void saveAll(List<User> users);

    List<User> findAll();

    List<User> findByOrderId(Long orderId);

    List<User> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);

    List<User> findByOrderStartDate(LocalDate startDate);
}
