package com.example.warehouse.repository;

import com.example.warehouse.model.ColumnMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColumnMetadataRepository extends JpaRepository<ColumnMetadata, Long> {

    List<ColumnMetadata> findByTableIdOrderByOrdinalPosition(Long tableId);
}
