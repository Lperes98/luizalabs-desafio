package com.luizalabs.orders.usecase.port;

import com.luizalabs.orders.domain.dto.ProcessingResult;
import org.springframework.web.multipart.MultipartFile;

public interface ProcessFileUseCase {

    ProcessingResult execute(MultipartFile file);
}
