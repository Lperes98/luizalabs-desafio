package com.luizalabs.orders.common.util;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FixedWidthParser - Testes Estendidos para Coverage")
class FixedWidthParserExtendedTest {

    private static final String VALID_LINE =
            "0000000070                              Palmer"
                    + " Prosacco0000000753000000000300000018367420210308";

    @Test
    @DisplayName("Deve lançar exceção para linha nula")
    void shouldThrowExceptionForNullLine() {
        assertThatThrownBy(() -> FixedWidthParser.validateLineLength(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser nula");
    }

    @Test
    @DisplayName("Deve lançar exceção para value decimal inválido")
    void shouldThrowExceptionForInvalidDecimalValue() {
        String invalidLine =
                VALID_LINE.substring(0, 75) + "ABCDEFGHIJKL" + VALID_LINE.substring(87);

        assertThatThrownBy(() -> FixedWidthParser.extractValue(invalidLine))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("value");
    }

    @Test
    @DisplayName("Deve lançar exceção para orderId inválido")
    void shouldThrowExceptionForInvalidOrderId() {
        String invalidLine = VALID_LINE.substring(0, 55) + "ABCDEFGHIJ" + VALID_LINE.substring(65);

        assertThatThrownBy(() -> FixedWidthParser.extractOrderId(invalidLine))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("order_id");
    }

    @Test
    @DisplayName("Deve lançar exceção para productId inválido")
    void shouldThrowExceptionForInvalidProductId() {
        String invalidLine = VALID_LINE.substring(0, 65) + "ABCDEFGHIJ" + VALID_LINE.substring(75);

        assertThatThrownBy(() -> FixedWidthParser.extractProductId(invalidLine))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("product_id");
    }

    @Test
    @DisplayName("Deve extrair userName vazio quando campo está em branco")
    void shouldExtractEmptyUserName() {
        String lineWithBlankName =
                VALID_LINE.substring(0, 10) + " ".repeat(45) + VALID_LINE.substring(55);

        String userName = FixedWidthParser.extractUserName(lineWithBlankName);

        assertThat(userName).isEmpty();
    }

    @Test
    @DisplayName("Deve extrair value com valor zero")
    void shouldExtractZeroValue() {
        String lineWithZeroValue =
                VALID_LINE.substring(0, 75) + "000000000000" + VALID_LINE.substring(87);

        BigDecimal value = FixedWidthParser.extractValue(lineWithZeroValue);

        assertThat(value).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Deve extrair value com valor negativo (se permitido)")
    void shouldExtractNegativeValue() {
        String lineWithNegativeValue =
                VALID_LINE.substring(0, 75) + "-0000001000" + VALID_LINE.substring(87);

        assertThatCode(() -> FixedWidthParser.extractValue(lineWithNegativeValue))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar exceção para StringIndexOutOfBounds em extractOrderId")
    void shouldThrowExceptionForOutOfBoundsInOrderId() {
        String shortLine = "0".repeat(50);

        assertThatThrownBy(() -> FixedWidthParser.extractOrderId(shortLine))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("order_id");
    }

    @Test
    @DisplayName("Deve lançar exceção para StringIndexOutOfBounds em extractProductId")
    void shouldThrowExceptionForOutOfBoundsInProductId() {
        String shortLine = "0".repeat(60);

        assertThatThrownBy(() -> FixedWidthParser.extractProductId(shortLine))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("product_id");
    }

    @Test
    @DisplayName("Deve lançar exceção para StringIndexOutOfBounds em extractValue")
    void shouldThrowExceptionForOutOfBoundsInValue() {
        String shortLine = "0".repeat(70);

        assertThatThrownBy(() -> FixedWidthParser.extractValue(shortLine))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("value");
    }

    @Test
    @DisplayName("Deve lançar exceção para StringIndexOutOfBounds em extractDate")
    void shouldThrowExceptionForOutOfBoundsInDate() {
        String shortLine = "0".repeat(80);

        assertThatThrownBy(() -> FixedWidthParser.extractDate(shortLine))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("date");
    }

    @Test
    @DisplayName("Deve extrair todos os campos de uma linha válida completa")
    void shouldExtractAllFieldsFromValidLine() {
        StringBuilder line = new StringBuilder();
        line.append("0000000001");
        line.append(String.format("%-45s", "User Name"));
        line.append("0000000010");
        line.append("0000000001");
        line.append("000000012345");
        line.append("20210101");

        String validLine = line.toString();

        FixedWidthParser.validateLineLength(validLine);
        Long userId = FixedWidthParser.extractUserId(validLine);
        String userName = FixedWidthParser.extractUserName(validLine);
        Long orderId = FixedWidthParser.extractOrderId(validLine);
        Long productId = FixedWidthParser.extractProductId(validLine);
        BigDecimal value = FixedWidthParser.extractValue(validLine);
        var date = FixedWidthParser.extractDate(validLine);

        assertThat(userId).isEqualTo(1L);
        assertThat(userName).isEqualTo("User Name");
        assertThat(orderId).isEqualTo(10L);
        assertThat(productId).isEqualTo(1L);
        assertThat(value).isEqualByComparingTo("12345.00");
        assertThat(date).isEqualTo("2021-01-01");
    }

    @Test
    @DisplayName("Deve extrair userId com zeros à esquerda")
    void shouldExtractUserIdWithLeadingZeros() {
        String line = "0000000001" + " ".repeat(85);
        Long userId = FixedWidthParser.extractUserId(line);
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve extrair value com decimais corretos")
    void shouldExtractValueWithCorrectDecimals() {
        String lineWithDecimal =
                VALID_LINE.substring(0, 75) + "000000012345" + VALID_LINE.substring(87);

        BigDecimal value = FixedWidthParser.extractValue(lineWithDecimal);

        assertThat(value).isEqualByComparingTo("12345.00");
        assertThat(value.scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve extrair data com diferentes formatos válidos")
    void shouldExtractDateWithDifferentValidFormats() {
        String line1 = VALID_LINE.substring(0, 87) + "20211231";
        String line2 = VALID_LINE.substring(0, 87) + "20210101";
        String line3 = VALID_LINE.substring(0, 87) + "20210630";

        assertThatCode(() -> FixedWidthParser.extractDate(line1)).doesNotThrowAnyException();
        assertThatCode(() -> FixedWidthParser.extractDate(line2)).doesNotThrowAnyException();
        assertThatCode(() -> FixedWidthParser.extractDate(line3)).doesNotThrowAnyException();
    }
}
