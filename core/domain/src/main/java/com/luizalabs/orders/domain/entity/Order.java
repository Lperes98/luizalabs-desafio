package com.luizalabs.orders.domain.entity;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long orderId;
    private LocalDate date;
    private BigDecimal total;

    @Builder.Default private List<Product> products = new ArrayList<>();

    public void addProduct(Product product) {
        if (this.products == null) {
            this.products = new ArrayList<>();
        }
        this.products.add(product);
    }

    public BigDecimal calculateTotal() {
        if (products == null || products.isEmpty()) {
            return ZERO;
        }
        return products.stream().map(Product::getValue).reduce(ZERO, BigDecimal::add);
    }
}
