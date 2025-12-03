package com.luizalabs.orders.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("total")
    private String total;

    @JsonProperty("date")
    private String date;

    @JsonProperty("products")
    private List<ProductDTO> products;
}
