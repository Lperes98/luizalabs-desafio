package com.luizalabs.orders.usecase.impl.order;

import com.luizalabs.orders.domain.exception.EmptyFileException;
import com.luizalabs.orders.domain.exception.InvalidFileFormatException;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator {

    private static final String[] ALLOWED_EXTENSIONS = {".txt"};

    public static void validate(MultipartFile file) {
        validateNotNull(file);
        validateNotEmpty(file);
        validateExtension(file);
    }

    private static void validateNotNull(MultipartFile file) {
        if (file == null) {
            throw new InvalidFileFormatException("Arquivo não pode ser nulo");
        }
    }

    private static void validateNotEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new EmptyFileException("Arquivo está vazio");
        }
    }

    private static void validateExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new InvalidFileFormatException("Nome do arquivo inválido");
        }

        boolean validExtension = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (fileName.toLowerCase().endsWith(ext)) {
                validExtension = true;
                break;
            }
        }

        if (!validExtension) {
            throw new InvalidFileFormatException(
                    String.format(
                            "Extensão de arquivo inválida. Extensões permitidas: %s",
                            String.join(", ", ALLOWED_EXTENSIONS)));
        }
    }
}
