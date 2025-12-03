package com.luizalabs.orders.usecase.impl.order;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.luizalabs.orders.domain.dto.LineError;
import com.luizalabs.orders.domain.dto.ParsedLine;
import com.luizalabs.orders.domain.exception.FileProcessingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

@DisplayName("FileParserService - Testes Unitários")
class FileParserServiceTest {

    private FileParserService service;
    private static final String VALID_LINE =
            "0000000070                              Palmer"
                    + " Prosacco0000000753000000000300000018367420210308";

    @BeforeEach
    void setUp() {
        service = new FileParserService();
    }

    @Test
    @DisplayName("Deve parsear arquivo válido com uma linha")
    void shouldParseValidFileWithOneLine() throws IOException {
        MultipartFile file = createFile(VALID_LINE);

        FileParserService.ParseResult result = service.parse(file);

        assertThat(result.validLines()).hasSize(1);
        assertThat(result.errors()).isEmpty();
        assertThat(result.totalLines()).isEqualTo(1);
        assertThat(result.hasValidLines()).isTrue();
        assertThat(result.hasErrors()).isFalse();

        ParsedLine line = result.validLines().get(0);
        assertThat(line.getUserId()).isEqualTo(70L);
        assertThat(line.getUserName()).isEqualTo("Palmer Prosacco");
        assertThat(line.getOrderId()).isEqualTo(753L);
        assertThat(line.getProductId()).isEqualTo(3L);
        assertThat(line.getValue()).isEqualByComparingTo("183674.00");
        assertThat(line.getDate()).isEqualTo(LocalDate.of(2021, 3, 8));
    }

    @Test
    @DisplayName("Deve parsear arquivo com múltiplas linhas válidas")
    void shouldParseFileWithMultipleValidLines() throws IOException {
        String content = VALID_LINE + "\n" + VALID_LINE;
        MultipartFile file = createFile(content);

        FileParserService.ParseResult result = service.parse(file);

        assertThat(result.validLines()).hasSize(2);
        assertThat(result.errors()).isEmpty();
    }

    @Test
    @DisplayName("Deve ignorar linhas vazias")
    void shouldIgnoreEmptyLines() throws IOException {
        String content = VALID_LINE + "\n\n" + VALID_LINE;
        MultipartFile file = createFile(content);

        FileParserService.ParseResult result = service.parse(file);

        assertThat(result.validLines()).hasSize(2);
        assertThat(result.errors()).isEmpty();
    }

    @Test
    @DisplayName("Deve coletar erros de linhas inválidas sem falhar")
    void shouldCollectErrorsFromInvalidLinesWithoutFailing() throws IOException {
        String invalidLine = "INVALID_LINE";
        String content = VALID_LINE + "\n" + invalidLine + "\n" + VALID_LINE;
        MultipartFile file = createFile(content);

        FileParserService.ParseResult result = service.parse(file);

        assertThat(result.validLines()).hasSize(2);
        assertThat(result.errors()).hasSize(1);
        assertThat(result.hasErrors()).isTrue();

        LineError error = result.errors().get(0);
        assertThat(error.getLineNumber()).isEqualTo(2);
        assertThat(error.getLineContent()).contains("INVALID_LINE");
        assertThat(error.getErrorMessage()).isNotBlank();
    }

    @Test
    @DisplayName("Deve truncar conteúdo de linha longa no erro")
    void shouldTruncateLongLineContentInError() throws IOException {
        String longLine = "A".repeat(100);
        MultipartFile file = createFile(longLine);

        FileParserService.ParseResult result = service.parse(file);

        assertThat(result.errors()).hasSize(1);
        LineError error = result.errors().get(0);
        assertThat(error.getLineContent()).hasSizeLessThanOrEqualTo(53);
    }

    @Test
    @DisplayName("Deve lançar exceção para erro de I/O")
    void shouldThrowExceptionForIOError() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException("Erro de leitura"));

        assertThatThrownBy(() -> service.parse(file))
                .isInstanceOf(FileProcessingException.class)
                .hasMessageContaining("Erro ao ler arquivo");
    }

    @Test
    @DisplayName("Deve retornar total de linhas correto")
    void shouldReturnCorrectTotalLines() throws IOException {
        String content = VALID_LINE + "\n" + VALID_LINE + "\n" + VALID_LINE;
        MultipartFile file = createFile(content);

        FileParserService.ParseResult result = service.parse(file);

        assertThat(result.totalLines()).isEqualTo(3);
    }

    private MultipartFile createFile(String content) throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes()));
        when(file.getOriginalFilename()).thenReturn("test.txt");
        return file;
    }
}
