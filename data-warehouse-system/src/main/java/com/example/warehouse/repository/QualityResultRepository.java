package com.example.warehouse.repository;

import com.example.warehouse.model.QualityResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QualityResultRepository extends JpaRepository<QualityResult, Long> {

    List<QualityResult> findByTableId(Long tableId);

    List<QualityResult> findByRuleId(Long ruleId);

    List<QualityResult> findByStatus(QualityResult.CheckStatus status);
}
