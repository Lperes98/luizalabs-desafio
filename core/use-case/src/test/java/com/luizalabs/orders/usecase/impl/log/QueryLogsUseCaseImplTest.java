package com.luizalabs.orders.usecase.impl.log;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luizalabs.orders.domain.dto.LineError;
import com.luizalabs.orders.domain.enums.ProcessingStatus;
import com.luizalabs.orders.domain.exception.NotFoundException;
import com.luizalabs.orders.usecase.port.data.FileProcessingLogPort;
import com.luizalabs.orders.usecase.port.data.FileProcessingLogPort.LogInfo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("QueryLogsUseCaseImpl - Testes Unitários")
class QueryLogsUseCaseImplTest {

    @Mock private FileProcessingLogPort logData;

    @InjectMocks private QueryLogsUseCaseImpl queryLogsUseCase;

    private LogInfo mockLogInfo;

    @BeforeEach
    void setUp() {
        mockLogInfo =
                new LogInfo(
                        1L,
                        "test.txt",
                        LocalDateTime.now(),
                        ProcessingStatus.SUCCESS,
                        10,
                        10,
                        0,
                        List.of(),
                        1000L);
    }

    @Test
    @DisplayName("Deve buscar log por ID com sucesso")
    void shouldFindLogById() {
        when(logData.findById(anyLong())).thenReturn(Optional.of(mockLogInfo));

        LogInfo result = queryLogsUseCase.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.fileName()).isEqualTo("test.txt");
        assertThat(result.status()).isEqualTo(ProcessingStatus.SUCCESS);
        assertThat(result.totalLines()).isEqualTo(10);
        assertThat(result.processedLines()).isEqualTo(10);
        assertThat(result.errorLines()).isEqualTo(0);
        assertThat(result.processingTimeMs()).isEqualTo(1000L);

        verify(logData, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando log não existe")
    void shouldThrowNotFoundExceptionWhenLogNotExists() {
        when(logData.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryLogsUseCase.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Log não encontrado: 999");

        verify(logData, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Deve buscar todos os logs com paginação")
    void shouldFindAllLogsWithPagination() {
        LogInfo log1 =
                new LogInfo(
                        1L,
                        "file1.txt",
                        LocalDateTime.now(),
                        ProcessingStatus.SUCCESS,
                        10,
                        10,
                        0,
                        List.of(),
                        1000L);

        LogInfo log2 =
                new LogInfo(
                        2L,
                        "file2.txt",
                        LocalDateTime.now(),
                        ProcessingStatus.PARTIAL_SUCCESS,
                        5,
                        4,
                        1,
                        List.of(new LineError(3, "invalid", "Error")),
                        500L);

        when(logData.findAll(anyInt(), anyInt())).thenReturn(List.of(log1, log2));

        List<LogInfo> result = queryLogsUseCase.findAll(0, 20);

        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).errorLines()).isEqualTo(1);
        assertThat(result.get(1).errors()).hasSize(1);

        verify(logData, times(1)).findAll(0, 20);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há logs")
    void shouldReturnEmptyListWhenNoLogs() {
        when(logData.findAll(anyInt(), anyInt())).thenReturn(List.of());

        List<LogInfo> result = queryLogsUseCase.findAll(0, 20);

        assertThat(result).isNotNull().isEmpty();
        verify(logData, times(1)).findAll(0, 20);
    }

    @Test
    @DisplayName("Deve buscar logs por status SUCCESS")
    void shouldFindLogsByStatusSuccess() {
        LogInfo successLog =
                new LogInfo(
                        1L,
                        "success.txt",
                        LocalDateTime.now(),
                        ProcessingStatus.SUCCESS,
                        10,
                        10,
                        0,
                        List.of(),
                        1000L);

        when(logData.findByStatus(any(ProcessingStatus.class), anyInt(), anyInt()))
                .thenReturn(List.of(successLog));

        List<LogInfo> result = queryLogsUseCase.findByStatus(ProcessingStatus.SUCCESS, 0, 20);

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(ProcessingStatus.SUCCESS);
        assertThat(result.get(0).errorLines()).isZero();

        verify(logData, times(1)).findByStatus(ProcessingStatus.SUCCESS, 0, 20);
    }

    @Test
    @DisplayName("Deve buscar logs por status FAILED")
    void shouldFindLogsByStatusFailed() {
        LogInfo failedLog =
                new LogInfo(
                        2L,
                        "failed.txt",
                        LocalDateTime.now(),
                        ProcessingStatus.FAILED,
                        5,
                        0,
                        5,
                        List.of(
                                new LineError(1, "line1", "Error 1"),
                                new LineError(2, "line2", "Error 2")),
                        200L);

        when(logData.findByStatus(eq(ProcessingStatus.FAILED), anyInt(), anyInt()))
                .thenReturn(List.of(failedLog));

        List<LogInfo> result = queryLogsUseCase.findByStatus(ProcessingStatus.FAILED, 0, 10);

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(ProcessingStatus.FAILED);
        assertThat(result.get(0).errorLines()).isEqualTo(5);
        assertThat(result.get(0).processedLines()).isZero();
        assertThat(result.get(0).errors()).hasSize(2);

        verify(logData, times(1)).findByStatus(ProcessingStatus.FAILED, 0, 10);
    }

    @Test
    @DisplayName("Deve buscar logs por status PARTIAL_SUCCESS")
    void shouldFindLogsByStatusPartialSuccess() {
        LogInfo partialLog =
                new LogInfo(
                        3L,
                        "partial.txt",
                        LocalDateTime.now(),
                        ProcessingStatus.PARTIAL_SUCCESS,
                        10,
                        8,
                        2,
                        List.of(new LineError(5, "invalid line", "Parse error")),
                        800L);

        when(logData.findByStatus(eq(ProcessingStatus.PARTIAL_SUCCESS), anyInt(), anyInt()))
                .thenReturn(List.of(partialLog));

        List<LogInfo> result =
                queryLogsUseCase.findByStatus(ProcessingStatus.PARTIAL_SUCCESS, 0, 20);

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(ProcessingStatus.PARTIAL_SUCCESS);
        assertThat(result.get(0).errorLines()).isEqualTo(2);
        assertThat(result.get(0).processedLines()).isEqualTo(8);

        verify(logData, times(1)).findByStatus(ProcessingStatus.PARTIAL_SUCCESS, 0, 20);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há logs com status específico")
    void shouldReturnEmptyListWhenNoLogsWithStatus() {
        when(logData.findByStatus(any(ProcessingStatus.class), anyInt(), anyInt()))
                .thenReturn(List.of());

        List<LogInfo> result = queryLogsUseCase.findByStatus(ProcessingStatus.PROCESSING, 0, 20);

        assertThat(result).isNotNull().isEmpty();
        verify(logData, times(1)).findByStatus(ProcessingStatus.PROCESSING, 0, 20);
    }

    @Test
    @DisplayName("Deve buscar logs com diferentes tamanhos de página")
    void shouldFindLogsWithDifferentPageSizes() {
        when(logData.findAll(anyInt(), anyInt())).thenReturn(List.of(mockLogInfo));

        List<LogInfo> result1 = queryLogsUseCase.findAll(0, 10);
        List<LogInfo> result2 = queryLogsUseCase.findAll(1, 50);
        List<LogInfo> result3 = queryLogsUseCase.findAll(2, 100);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result3).isNotNull();

        verify(logData, times(1)).findAll(0, 10);
        verify(logData, times(1)).findAll(1, 50);
        verify(logData, times(1)).findAll(2, 100);
    }

    @Test
    @DisplayName("Deve buscar logs por status com paginação customizada")
    void shouldFindLogsByStatusWithCustomPagination() {
        when(logData.findByStatus(any(ProcessingStatus.class), anyInt(), anyInt()))
                .thenReturn(List.of(mockLogInfo));

        List<LogInfo> result = queryLogsUseCase.findByStatus(ProcessingStatus.SUCCESS, 2, 5);

        assertThat(result).isNotNull();
        verify(logData, times(1)).findByStatus(ProcessingStatus.SUCCESS, 2, 5);
    }
}
