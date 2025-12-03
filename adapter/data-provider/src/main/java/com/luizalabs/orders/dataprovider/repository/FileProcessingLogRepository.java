package com.luizalabs.orders.dataprovider.repository;

import com.luizalabs.orders.dataprovider.table.FileProcessingLogTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileProcessingLogRepository extends JpaRepository<FileProcessingLogTable, Long> {

    Page<FileProcessingLogTable> findByStatus(String status, Pageable pageable);
}
