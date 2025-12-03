package com.luizalabs.orders.dataprovider.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductData {

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("value")
    private BigDecimal value;
}
