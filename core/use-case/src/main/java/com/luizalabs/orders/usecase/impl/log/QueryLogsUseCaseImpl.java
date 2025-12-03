package com.luizalabs.orders.usecase.impl.log;

import com.luizalabs.orders.domain.enums.ProcessingStatus;
import com.luizalabs.orders.domain.exception.NotFoundException;
import com.luizalabs.orders.usecase.port.QueryLogsUseCase;
import com.luizalabs.orders.usecase.port.data.FileProcessingLogPort;
import com.luizalabs.orders.usecase.port.data.FileProcessingLogPort.LogInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryLogsUseCaseImpl implements QueryLogsUseCase {

    private final FileProcessingLogPort logData;

    @Override
    @Transactional(readOnly = true)
    public LogInfo findById(Long logId) {
        log.info("Buscando log ID: {}", logId);
        return logData.findById(logId)
                .orElseThrow(() -> new NotFoundException("Log não encontrado: " + logId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogInfo> findAll(int page, int size) {
        log.info("Buscando todos os logs - page: {}, size: {}", page, size);
        return logData.findAll(page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogInfo> findByStatus(ProcessingStatus status, int page, int size) {
        log.info("Buscando logs por status: {} - page: {}, size: {}", status, page, size);
        return logData.findByStatus(status, page, size);
    }
}
