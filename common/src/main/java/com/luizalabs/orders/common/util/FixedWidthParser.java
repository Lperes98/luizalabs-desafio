package com.luizalabs.orders.common.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FixedWidthParser {

    private static final int EXPECTED_LINE_LENGTH = 95;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final int USER_ID_START = 0;
    private static final int USER_ID_END = 10;

    private static final int USER_NAME_START = 10;
    private static final int USER_NAME_END = 55;

    private static final int ORDER_ID_START = 55;
    private static final int ORDER_ID_END = 65;

    private static final int PRODUCT_ID_START = 65;
    private static final int PRODUCT_ID_END = 75;

    private static final int VALUE_START = 75;
    private static final int VALUE_END = 87;

    private static final int DATE_START = 87;
    private static final int DATE_END = 95;

    public static Long extractUserId(String line) {
        return extractLong(line, USER_ID_START, USER_ID_END, "user_id");
    }

    public static String extractUserName(String line) {
        return extractString(line, USER_NAME_START, USER_NAME_END, "name");
    }

    public static Long extractOrderId(String line) {
        return extractLong(line, ORDER_ID_START, ORDER_ID_END, "order_id");
    }

    public static Long extractProductId(String line) {
        return extractLong(line, PRODUCT_ID_START, PRODUCT_ID_END, "product_id");
    }

    public static BigDecimal extractValue(String line) {
        return extractDecimal(line, VALUE_START, VALUE_END, "value");
    }

    public static LocalDate extractDate(String line) {
        return extractDate(line, DATE_START, DATE_END, "date");
    }

    public static void validateLineLength(String line) {
        if (line == null) {
            throw new IllegalArgumentException("Linha não pode ser nula");
        }
        if (line.length() != EXPECTED_LINE_LENGTH) {
            throw new IllegalArgumentException(
                    String.format(
                            "Linha com tamanho inválido: %d (esperado: %d)",
                            line.length(), EXPECTED_LINE_LENGTH));
        }
    }

    private static Long extractLong(String line, int start, int end, String fieldName) {
        try {
            String value = line.substring(start, end).trim();
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format(
                            "Formato inválido para campo '%s': %s", fieldName, e.getMessage()),
                    e);
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(
                    String.format("Erro ao extrair campo '%s': índice fora do limite", fieldName),
                    e);
        }
    }

    private static String extractString(String line, int start, int end, String fieldName) {
        try {
            return line.substring(start, end).trim();
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(
                    String.format("Erro ao extrair campo '%s': índice fora do limite", fieldName),
                    e);
        }
    }

    private static BigDecimal extractDecimal(String line, int start, int end, String fieldName) {
        try {
            String value = line.substring(start, end).trim();
            return BigDecimal.valueOf(Double.parseDouble(value)).setScale(2);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format(
                            "Formato inválido para campo decimal '%s': %s",
                            fieldName, e.getMessage()),
                    e);
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(
                    String.format("Erro ao extrair campo '%s': índice fora do limite", fieldName),
                    e);
        }
    }

    private static LocalDate extractDate(String line, int start, int end, String fieldName) {
        try {
            String dateStr = line.substring(start, end).trim();
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    String.format("Formato inválido para data '%s': %s", fieldName, e.getMessage()),
                    e);
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(
                    String.format("Erro ao extrair campo '%s': índice fora do limite", fieldName),
                    e);
        }
    }
}
