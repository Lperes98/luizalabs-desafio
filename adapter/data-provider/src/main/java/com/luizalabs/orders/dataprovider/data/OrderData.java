package com.luizalabs.orders.dataprovider.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderData {

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("total")
    private BigDecimal total;

    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("products")
    private List<ProductData> products;
}
