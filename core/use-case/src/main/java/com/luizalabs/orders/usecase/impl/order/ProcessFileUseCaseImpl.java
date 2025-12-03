package com.luizalabs.orders.usecase.impl.order;

import static java.time.LocalDateTime.now;

import com.luizalabs.orders.domain.dto.ProcessingResult;
import com.luizalabs.orders.domain.dto.UserOrderResponseDTO;
import com.luizalabs.orders.domain.entity.User;
import com.luizalabs.orders.domain.enums.ProcessingStatus;
import com.luizalabs.orders.domain.mapper.DomainMapper;
import com.luizalabs.orders.usecase.impl.order.FileParserService.ParseResult;
import com.luizalabs.orders.usecase.port.ProcessFileUseCase;
import com.luizalabs.orders.usecase.port.data.FileProcessingLogPort;
import com.luizalabs.orders.usecase.port.data.OrderDataPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessFileUseCaseImpl implements ProcessFileUseCase {

    private final FileParserService parser;
    private final DataAggregatorService aggregator;
    private final OrderDataPort orderData;
    private final FileProcessingLogPort logData;
    private final DomainMapper mapper = new DomainMapper();

    @Override
    @Transactional
    public ProcessingResult execute(MultipartFile file) {
        log.info("Processando: {}", file.getOriginalFilename());

        FileValidator.validate(file);

        Long logId = logData.createLog(file.getOriginalFilename(), now());

        ParseResult result = parser.parse(file);

        List<UserOrderResponseDTO> data = List.of();

        if (result.hasValidLines()) {
            List<User> users = aggregator.aggregate(result.validLines());
            orderData.saveAll(users);
            data = mapper.toResponseDTO(users);

            log.info(
                    "Processado: {} linhas, {} erros",
                    result.validLines().size(),
                    result.errors().size());
        }

        ProcessingStatus status = calculateStatus(result);

        logData.updateLog(
                logId,
                status,
                result.totalLines(),
                result.validLines().size(),
                result.errors().size(),
                result.errors());

        return ProcessingResult.builder()
                .logId(logId)
                .status(status)
                .totalLines(result.totalLines())
                .processedLines(result.validLines().size())
                .errorLines(result.errors().size())
                .hasErrors(result.hasErrors())
                .data(data)
                .build();
    }

    private ProcessingStatus calculateStatus(ParseResult result) {
        if (!result.hasErrors()) return ProcessingStatus.SUCCESS;
        if (result.hasValidLines()) return ProcessingStatus.PARTIAL_SUCCESS;
        return ProcessingStatus.FAILED;
    }
}
