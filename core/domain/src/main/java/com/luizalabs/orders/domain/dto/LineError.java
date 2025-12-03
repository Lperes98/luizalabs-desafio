package com.luizalabs.orders.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineError {

    private Integer lineNumber;
    private String lineContent;
    private String errorMessage;
}
