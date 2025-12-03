package com.luizalabs.orders.api.order;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import com.luizalabs.orders.api.order.doc.OrderControllerDoc;
import com.luizalabs.orders.domain.dto.ProcessingResult;
import com.luizalabs.orders.domain.dto.UserOrderResponseDTO;
import com.luizalabs.orders.domain.enums.ProcessingStatus;
import com.luizalabs.orders.usecase.port.ProcessFileUseCase;
import com.luizalabs.orders.usecase.port.QueryLogsUseCase;
import com.luizalabs.orders.usecase.port.QueryOrdersUseCase;
import com.luizalabs.orders.usecase.port.data.FileProcessingLogPort.LogInfo;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("v1/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerDoc {

    private final ProcessFileUseCase processFileUseCase;
    private final QueryOrdersUseCase queryOrdersUseCase;
    private final QueryLogsUseCase queryLogsUseCase;

    @Override
    @PostMapping(value = "/upload", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProcessingResult> upload(@RequestParam("file") MultipartFile file) {
        log.info("Upload: {}", file.getOriginalFilename());

        ProcessingResult result = processFileUseCase.execute(file);
        HttpStatus status = determineHttpStatus(result.getStatus());

        log.info(
                "Processado: {} linhas, {} erros",
                result.getProcessedLines(),
                result.getErrorLines());

        return ResponseEntity.status(status).body(result);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<UserOrderResponseDTO>> query(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate endDate) {
        List<UserOrderResponseDTO> result;

        if (orderId != null) {
            result = queryOrdersUseCase.findByOrderId(orderId);
        } else if (startDate != null && endDate != null) {
            result = queryOrdersUseCase.findByDateRange(startDate, endDate);
        } else {
            result = queryOrdersUseCase.findAll();
        }

        log.info("Retornando {} resultados", result.size());
        return ResponseEntity.ok(result);
    }

    @Override
    @GetMapping("/logs/{id}")
    public ResponseEntity<LogInfo> getLogById(@PathVariable Long id) {
        log.info("Buscando log ID: {}", id);
        LogInfo logInfo = queryLogsUseCase.findById(id);
        return ResponseEntity.ok(logInfo);
    }

    @Override
    @GetMapping("/logs")
    public ResponseEntity<List<LogInfo>> getLogs(
            @RequestParam(required = false) ProcessingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Listando logs - status: {}, page: {}, size: {}", status, page, size);

        List<LogInfo> logs =
                (status != null)
                        ? queryLogsUseCase.findByStatus(status, page, size)
                        : queryLogsUseCase.findAll(page, size);

        log.info("Retornando {} logs", logs.size());
        return ResponseEntity.ok(logs);
    }

    private HttpStatus determineHttpStatus(ProcessingStatus status) {
        return switch (status) {
            case SUCCESS -> OK;
            case PARTIAL_SUCCESS -> MULTI_STATUS;
            case FAILED -> UNPROCESSABLE_ENTITY;
            default -> ACCEPTED;
        };
    }
}
