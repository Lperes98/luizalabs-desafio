package com.luizalabs.orders.usecase.impl.order;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.luizalabs.orders.common.util.FixedWidthParser;
import com.luizalabs.orders.domain.dto.LineError;
import com.luizalabs.orders.domain.dto.ParsedLine;
import com.luizalabs.orders.domain.exception.FileProcessingException;
import com.luizalabs.orders.domain.exception.InvalidLineFormatException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FileParserService {

    public ParseResult parse(MultipartFile file) {
        List<ParsedLine> valid = new ArrayList<>();
        List<LineError> errors = new ArrayList<>();
        int total = 0;

        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(file.getInputStream(), UTF_8))) {

            String line;
            int number = 1;

            while ((line = reader.readLine()) != null) {
                total = number;

                if (line.trim().isEmpty()) {
                    number++;
                    continue;
                }

                try {
                    valid.add(parseLine(line));
                } catch (Exception e) {
                    errors.add(createError(number, line, e));
                    log.warn("Erro linha {}: {}", number, e.getMessage());
                }

                number++;
            }

        } catch (IOException e) {
            throw new FileProcessingException("Erro ao ler arquivo: " + e.getMessage(), e);
        }

        return new ParseResult(valid, errors, total);
    }

    private ParsedLine parseLine(String line) {
        try {
            FixedWidthParser.validateLineLength(line);

            return ParsedLine.builder()
                    .userId(FixedWidthParser.extractUserId(line))
                    .userName(FixedWidthParser.extractUserName(line))
                    .orderId(FixedWidthParser.extractOrderId(line))
                    .productId(FixedWidthParser.extractProductId(line))
                    .value(FixedWidthParser.extractValue(line))
                    .date(FixedWidthParser.extractDate(line))
                    .build();
        } catch (IllegalArgumentException e) {
            throw new InvalidLineFormatException(e.getMessage(), e);
        }
    }

    private LineError createError(int number, String line, Exception e) {
        String content = line.length() > 50 ? line.substring(0, 50) + "..." : line;

        return LineError.builder()
                .lineNumber(number)
                .lineContent(content)
                .errorMessage(e.getMessage())
                .build();
    }

    public record ParseResult(List<ParsedLine> validLines, List<LineError> errors, int totalLines) {
        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public boolean hasValidLines() {
            return !validLines.isEmpty();
        }
    }
}
