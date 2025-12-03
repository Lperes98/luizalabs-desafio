package com.luizalabs.orders.dataprovider.repository;

import com.luizalabs.orders.dataprovider.table.UserOrderTable;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOrderRepository extends JpaRepository<UserOrderTable, Long> {

    @Query(
            value =
                    """
        SELECT * FROM user_orders
        WHERE orders @> CAST(:orderIdJson AS jsonb)
        """,
            nativeQuery = true)
    List<UserOrderTable> findByOrderId(@Param("orderIdJson") String orderIdJson);

    @Query(
            value =
                    """
        SELECT DISTINCT u.*
        FROM user_orders u,
        jsonb_array_elements(u.orders) AS o
        WHERE (o->>'date')::date BETWEEN :startDate AND :endDate
        """,
            nativeQuery = true)
    List<UserOrderTable> findByOrderDateBetween(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(
            value =
                    """
        SELECT DISTINCT u.*
        FROM user_orders u,
        jsonb_array_elements(u.orders) AS o
        WHERE (o->>'date')::date >= :startDate
        """,
            nativeQuery = true)
    List<UserOrderTable> findByOrderStartDate(@Param("startDate") LocalDate startDate);
}
