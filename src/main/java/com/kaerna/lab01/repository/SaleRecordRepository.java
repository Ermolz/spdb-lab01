package com.kaerna.lab01.repository;

import com.kaerna.lab01.entity.SaleRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRecordRepository extends JpaRepository<SaleRecord, Long> {
}
