package com.luizalabs.orders.dataprovider.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luizalabs.orders.dataprovider.repository.FileProcessingLogRepository;
import com.luizalabs.orders.dataprovider.table.FileProcessingLogTable;
import com.luizalabs.orders.domain.dto.LineError;
import com.luizalabs.orders.domain.enums.ProcessingStatus;
import com.luizalabs.orders.domain.exception.FileProcessingException;
import com.luizalabs.orders.usecase.port.data.FileProcessingLogPort;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileProcessingLogAdapter implements FileProcessingLogPort {

    private final FileProcessingLogRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Long createLog(String fileName, LocalDateTime uploadedAt) {
        log.info("Criando log de processamento para arquivo: {}", fileName);

        FileProcessingLogTable logTable =
                FileProcessingLogTable.builder()
                        .fileName(fileName)
                        .uploadedAt(uploadedAt)
                        .status(ProcessingStatus.PROCESSING.name())
                        .totalLines(0)
                        .processedLines(0)
                        .errorLines(0)
                        .build();

        FileProcessingLogTable saved = repository.save(logTable);

        log.info("Log criado com ID: {}", saved.getId());
        return saved.getId();
    }

    @Override
    public void updateLog(
            Long logId,
            ProcessingStatus status,
            Integer totalLines,
            Integer processedLines,
            Integer errorLines,
            List<LineError> errors) {
        log.info(
                "Atualizando log ID {}: status={}, totalLines={}, processedLines={}, errorLines={}",
                logId,
                status,
                totalLines,
                processedLines,
                errorLines);

        FileProcessingLogTable logTable =
                repository
                        .findById(logId)
                        .orElseThrow(
                                () -> new FileProcessingException("Log não encontrado: " + logId));

        logTable.setStatus(status.name());
        logTable.setTotalLines(totalLines);
        logTable.setProcessedLines(processedLines);
        logTable.setErrorLines(errorLines);

        if (errors != null && !errors.isEmpty()) {
            try {
                String errorsJson = objectMapper.writeValueAsString(errors);
                logTable.setErrors(errorsJson);
            } catch (JsonProcessingException e) {
                log.error("Erro ao serializar erros para JSON", e);
                throw new FileProcessingException("Erro ao converter erros para JSON", e);
            }
        }

        long processingTime =
                Duration.between(logTable.getUploadedAt(), LocalDateTime.now()).toMillis();
        logTable.setProcessingTimeMs(processingTime);

        repository.save(logTable);

        log.info("Log atualizado com sucesso");
    }

    @Override
    public Optional<LogInfo> findById(Long logId) {
        log.info("Buscando log por ID: {}", logId);
        return repository.findById(logId).map(this::toLogInfo);
    }

    @Override
    public List<LogInfo> findAll(int page, int size) {
        log.info("Buscando todos os logs - page: {}, size: {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        return repository.findAll(pageRequest).stream().map(this::toLogInfo).toList();
    }

    @Override
    public List<LogInfo> findByStatus(ProcessingStatus status, int page, int size) {
        log.info("Buscando logs por status: {} - page: {}, size: {}", status, page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        return repository.findByStatus(status.name(), pageRequest).stream()
                .map(this::toLogInfo)
                .toList();
    }

    private LogInfo toLogInfo(FileProcessingLogTable table) {
        List<LineError> errors = parseErrors(table.getErrors());

        return new LogInfo(
                table.getId(),
                table.getFileName(),
                table.getUploadedAt(),
                ProcessingStatus.valueOf(table.getStatus()),
                table.getTotalLines(),
                table.getProcessedLines(),
                table.getErrorLines(),
                errors,
                table.getProcessingTimeMs());
    }

    private List<LineError> parseErrors(String errorsJson) {
        if (errorsJson == null || errorsJson.isEmpty()) {
            return List.of();
        }

        try {
            return List.of(objectMapper.readValue(errorsJson, LineError[].class));
        } catch (JsonProcessingException e) {
            log.error("Erro ao desserializar erros do JSON", e);
            return List.of();
        }
    }
}
