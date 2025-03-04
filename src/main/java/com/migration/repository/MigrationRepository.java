package com.migration.repository;

import com.migration.entity.MigrationObject;
import com.migration.enums.MigrationObjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface MigrationRepository extends JpaRepository<MigrationObject, Long> {
    @Query(value = "from objects where status = :status and configId = :configId")
    Page<MigrationObject> findByStatusAndConfigIdWithLimit(MigrationObjectStatus status, UUID configId, Pageable page);

    @Transactional
    default void capture(List<Long> ids, MigrationObjectStatus status){
        if (ids.size() != 0){
            update(ids, status);
        }
    }

    @Modifying
    @Query(value = "UPDATE objects set status = :status where id IN (:ids)")
    void update(List<Long> ids, MigrationObjectStatus status);

    @Modifying
    @Query(value = "truncate table objects", nativeQuery = true)
    void deleteAll();
}
