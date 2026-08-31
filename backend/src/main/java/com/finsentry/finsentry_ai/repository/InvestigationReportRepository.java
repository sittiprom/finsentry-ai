package com.finsentry.finsentry_ai.repository;

import com.finsentry.finsentry_ai.entity.InvestigationReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvestigationReportRepository extends JpaRepository<InvestigationReportEntity, Long> {
    Optional<InvestigationReportEntity> findByCaseId(Long caseId);
}
