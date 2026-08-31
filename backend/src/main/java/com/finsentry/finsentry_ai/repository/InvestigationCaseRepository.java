package com.finsentry.finsentry_ai.repository;

import com.finsentry.finsentry_ai.entity.InvestigationCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestigationCaseRepository extends JpaRepository<InvestigationCase,Long> {

}
