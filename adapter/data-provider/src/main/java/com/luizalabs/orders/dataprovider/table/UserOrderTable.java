package com.luizalabs.orders.dataprovider.table;

import static java.time.LocalDateTime.now;
import static org.hibernate.type.SqlTypes.JSON;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_orders")
public class UserOrderTable {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", length = 45, nullable = false)
    private String name;

    @JdbcTypeCode(JSON)
    @Column(name = "orders", columnDefinition = "jsonb", nullable = false)
    private String orders;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = now();
        updatedAt = now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = now();
    }
}
