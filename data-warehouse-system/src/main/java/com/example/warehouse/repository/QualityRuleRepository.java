package com.example.warehouse.repository;

import com.example.warehouse.model.QualityRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QualityRuleRepository extends JpaRepository<QualityRule, Long> {

    List<QualityRule> findByTableId(Long tableId);

    List<QualityRule> findByEnabled(Boolean enabled);

    List<QualityRule> findByRuleType(QualityRule.RuleType ruleType);
}
