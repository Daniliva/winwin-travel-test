package com.example.authapi.repository;

import com.example.authapi.model.ProcessingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProcessingLogRepository extends JpaRepository<ProcessingLog, UUID> {
}