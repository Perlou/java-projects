package com.example.warehouse.repository;

import com.example.warehouse.model.EtlTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtlTaskRepository extends JpaRepository<EtlTask, Long> {

    List<EtlTask> findByStatus(EtlTask.TaskStatus status);

    List<EtlTask> findByTargetTable(String targetTable);
}
