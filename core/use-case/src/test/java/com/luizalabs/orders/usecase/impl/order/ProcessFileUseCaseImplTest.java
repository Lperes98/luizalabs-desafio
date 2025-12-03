package com.luizalabs.orders.usecase.impl.order;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.luizalabs.orders.domain.dto.LineError;
import com.luizalabs.orders.domain.dto.ParsedLine;
import com.luizalabs.orders.domain.dto.ProcessingResult;
import com.luizalabs.orders.domain.dto.UserOrderResponseDTO;
import com.luizalabs.orders.domain.entity.User;
import com.luizalabs.orders.domain.enums.ProcessingStatus;
import com.luizalabs.orders.domain.exception.EmptyFileException;
import com.luizalabs.orders.usecase.impl.order.FileParserService.ParseResult;
import com.luizalabs.orders.usecase.port.data.FileProcessingLogPort;
import com.luizalabs.orders.usecase.port.data.OrderDataPort;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessFileUseCaseImpl - Testes Unitários")
class ProcessFileUseCaseImplTest {

    @Mock private FileParserService parser;

    @Mock private DataAggregatorService aggregator;

    @Mock private OrderDataPort orderData;

    @Mock private FileProcessingLogPort logData;

    @Mock private MultipartFile file;

    private ProcessFileUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProcessFileUseCaseImpl(parser, aggregator, orderData, logData);
    }

    @Test
    @DisplayName("Deve processar arquivo com sucesso (sem erros)")
    void shouldProcessFileSuccessfully() {
        Long logId = 1L;
        ParsedLine parsedLine = createParsedLine();
        User user = mock(User.class);
        UserOrderResponseDTO responseDTO = mock(UserOrderResponseDTO.class);

        ParseResult parseResult = new ParseResult(List.of(parsedLine), List.of(), 1);

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.txt");

        when(logData.createLog(anyString(), any())).thenReturn(logId);
        when(parser.parse(file)).thenReturn(parseResult);
        when(aggregator.aggregate(anyList())).thenReturn(List.of(user));

        ProcessingResult result = useCase.execute(file);

        assertThat(result.getLogId()).isEqualTo(logId);
        assertThat(result.getStatus()).isEqualTo(ProcessingStatus.SUCCESS);
        assertThat(result.getTotalLines()).isEqualTo(1);
        assertThat(result.getProcessedLines()).isEqualTo(1);
        assertThat(result.getErrorLines()).isEqualTo(0);
        assertThat(result.getHasErrors()).isFalse();
        assertThat(result.getData()).isNotEmpty();

        verify(orderData).saveAll(anyList());
        verify(logData)
                .updateLog(eq(logId), eq(ProcessingStatus.SUCCESS), eq(1), eq(1), eq(0), anyList());
    }

    @Test
    @DisplayName("Deve processar arquivo com sucesso parcial (com erros)")
    void shouldProcessFilePartially() {
        Long logId = 1L;
        ParsedLine parsedLine = createParsedLine();
        LineError error =
                LineError.builder()
                        .lineNumber(2)
                        .lineContent("invalid")
                        .errorMessage("Erro")
                        .build();

        ParseResult parseResult = new ParseResult(List.of(parsedLine), List.of(error), 2);

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.txt");

        when(logData.createLog(anyString(), any())).thenReturn(logId);
        when(parser.parse(file)).thenReturn(parseResult);
        when(aggregator.aggregate(anyList())).thenReturn(List.of(mock(User.class)));

        ProcessingResult result = useCase.execute(file);

        assertThat(result.getStatus()).isEqualTo(ProcessingStatus.PARTIAL_SUCCESS);
        assertThat(result.getProcessedLines()).isEqualTo(1);
        assertThat(result.getErrorLines()).isEqualTo(1);
        assertThat(result.getHasErrors()).isTrue();

        verify(logData)
                .updateLog(
                        eq(logId),
                        eq(ProcessingStatus.PARTIAL_SUCCESS),
                        eq(2),
                        eq(1),
                        eq(1),
                        anyList());
    }

    @Test
    @DisplayName("Deve falhar processamento quando todas as linhas são inválidas")
    void shouldFailWhenAllLinesAreInvalid() {
        Long logId = 1L;
        LineError error =
                LineError.builder()
                        .lineNumber(1)
                        .lineContent("invalid")
                        .errorMessage("Erro")
                        .build();
        ParseResult parseResult = new ParseResult(List.of(), List.of(error), 1);

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.txt");

        when(logData.createLog(anyString(), any())).thenReturn(logId);
        when(parser.parse(file)).thenReturn(parseResult);

        ProcessingResult result = useCase.execute(file);

        assertThat(result.getStatus()).isEqualTo(ProcessingStatus.FAILED);
        assertThat(result.getProcessedLines()).isEqualTo(0);
        assertThat(result.getData()).isEmpty();

        verify(orderData, never()).saveAll(anyList());
        verify(logData)
                .updateLog(eq(logId), eq(ProcessingStatus.FAILED), eq(1), eq(0), eq(1), anyList());
    }

    @Test
    @DisplayName("Deve validar arquivo antes de processar")
    void shouldValidateFileBeforeProcessing() {
        MultipartFile invalidFile = mock(MultipartFile.class);
        when(invalidFile.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(invalidFile))
                .isInstanceOf(EmptyFileException.class);

        verify(parser, never()).parse(any());
    }

    private ParsedLine createParsedLine() {
        return ParsedLine.builder()
                .userId(1L)
                .userName("Test")
                .orderId(123L)
                .productId(111L)
                .value(new BigDecimal("100.00"))
                .date(LocalDate.now())
                .build();
    }
}
