package com.finsentry.finsentry_ai.repository;

import com.finsentry.finsentry_ai.entity.TransactionInvestigationView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionInvestigationViewRepository  extends JpaRepository<TransactionInvestigationView, Long> {

    List<TransactionInvestigationView> findByNameOrigOrderByStepDesc(String nameOrig);

}
