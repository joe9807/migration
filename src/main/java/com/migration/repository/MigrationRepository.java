package com.migration.repository;

import com.migration.entity.MigrationObject;
import com.migration.entity.MigrationObjectStatus;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface MigrationRepository extends R2dbcRepository<MigrationObject, Long> {
    Flux<MigrationObject> findByStatusAndConfigId(MigrationObjectStatus status, UUID configId);

    default Mono<Void> capture(List<Long> ids, MigrationObjectStatus status){
        return ids.size() == 0? Mono.empty():update(ids, status);
    }

    @Transactional
    @Modifying
    @Query("UPDATE objects set status = :status where id IN (:ids)")
    Mono<Void> update(List<Long> ids, MigrationObjectStatus status);
}
