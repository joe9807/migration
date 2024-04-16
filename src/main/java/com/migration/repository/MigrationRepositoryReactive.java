package com.migration.repository;

import com.migration.entity.MigrationObject;
import com.migration.entity.MigrationObjectStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface MigrationRepository extends CrudRepository<MigrationObject, Long> {
    List<MigrationObject> findByStatusAndConfigId(MigrationObjectStatus status, UUID configId);

    @Transactional
    @Modifying
    @Query("update com.migration.entity.MigrationObject o set o.status = (?2) where o.id in (?1)")
    void capture(List<Long> ids, MigrationObjectStatus status);
}
