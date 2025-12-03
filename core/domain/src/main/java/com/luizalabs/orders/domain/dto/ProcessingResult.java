package com.luizalabs.orders.domain.dto;

import com.luizalabs.orders.domain.enums.ProcessingStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingResult {

    private Long logId;
    private ProcessingStatus status;
    private Integer totalLines;
    private Integer processedLines;
    private Integer errorLines;
    private Boolean hasErrors;
    private List<UserOrderResponseDTO> data;
}
