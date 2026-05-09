package com.example.warehouse.repository;

import com.example.warehouse.model.DwLayer;
import com.example.warehouse.model.TableMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableMetadataRepository extends JpaRepository<TableMetadata, Long> {

    List<TableMetadata> findByDwLayer(DwLayer dwLayer);

    Optional<TableMetadata> findByTableName(String tableName);

    List<TableMetadata> findByOwner(String owner);

    List<TableMetadata> findByDatabaseName(String databaseName);
}
