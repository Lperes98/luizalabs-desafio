package com.luizalabs.orders.usecase.port;

import com.luizalabs.orders.domain.enums.ProcessingStatus;
import com.luizalabs.orders.usecase.port.data.FileProcessingLogPort.LogInfo;
import java.util.List;

public interface QueryLogsUseCase {

    LogInfo findById(Long logId);

    List<LogInfo> findAll(int page, int size);

    List<LogInfo> findByStatus(ProcessingStatus status, int page, int size);
}
