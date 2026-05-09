package com.example.warehouse.repository;

import com.example.warehouse.model.LineageEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LineageEdgeRepository extends JpaRepository<LineageEdge, Long> {

    List<LineageEdge> findBySourceTableId(Long sourceTableId);

    List<LineageEdge> findByTargetTableId(Long targetTableId);

    List<LineageEdge> findByEtlTaskId(Long etlTaskId);
}
