package com.luizalabs.orders.usecase.port.data;

import com.luizalabs.orders.domain.dto.LineError;
import com.luizalabs.orders.domain.enums.ProcessingStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileProcessingLogPort {

    Long createLog(String fileName, LocalDateTime uploadedAt);

    void updateLog(
            Long logId,
            ProcessingStatus status,
            Integer totalLines,
            Integer processedLines,
            Integer errorLines,
            List<LineError> errors);

    Optional<LogInfo> findById(Long logId);

    List<LogInfo> findAll(int page, int size);

    List<LogInfo> findByStatus(ProcessingStatus status, int page, int size);

    record LogInfo(
            Long id,
            String fileName,
            LocalDateTime uploadedAt,
            ProcessingStatus status,
            Integer totalLines,
            Integer processedLines,
            Integer errorLines,
            List<LineError> errors,
            Long processingTimeMs) {}
}
