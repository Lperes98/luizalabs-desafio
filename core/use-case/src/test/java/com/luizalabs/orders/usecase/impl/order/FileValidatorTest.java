package com.luizalabs.orders.usecase.impl.order;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.luizalabs.orders.domain.exception.EmptyFileException;
import com.luizalabs.orders.domain.exception.InvalidFileFormatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

@DisplayName("FileValidator - Testes Unitários")
class FileValidatorTest {

    @Test
    @DisplayName("Deve validar arquivo válido sem exceções")
    void shouldValidateValidFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("test.txt");

        assertThatCode(() -> FileValidator.validate(file)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar exceção para arquivo nulo")
    void shouldThrowExceptionForNullFile() {
        assertThatThrownBy(() -> FileValidator.validate(null))
                .isInstanceOf(InvalidFileFormatException.class)
                .hasMessageContaining("nulo");
    }

    @Test
    @DisplayName("Deve lançar exceção para arquivo vazio")
    void shouldThrowExceptionForEmptyFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> FileValidator.validate(file))
                .isInstanceOf(EmptyFileException.class)
                .hasMessageContaining("vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção para extensão inválida")
    void shouldThrowExceptionForInvalidExtension() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("test.pdf");

        assertThatThrownBy(() -> FileValidator.validate(file))
                .isInstanceOf(InvalidFileFormatException.class)
                .hasMessageContaining("Extensão");
    }

    @Test
    @DisplayName("Deve lançar exceção para arquivo sem nome")
    void shouldThrowExceptionForFileWithoutName() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn(null);

        assertThatThrownBy(() -> FileValidator.validate(file))
                .isInstanceOf(InvalidFileFormatException.class);
    }

    @Test
    @DisplayName("Deve aceitar arquivo com extensão .txt maiúscula")
    void shouldAcceptUppercaseTxtExtension() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("test.TXT");

        assertThatCode(() -> FileValidator.validate(file)).doesNotThrowAnyException();
    }
}
