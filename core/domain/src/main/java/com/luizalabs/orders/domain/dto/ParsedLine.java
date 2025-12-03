package com.luizalabs.orders.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedLine {

    private Long userId;
    private String userName;
    private Long orderId;
    private Long productId;
    private BigDecimal value;
    private LocalDate date;
}
