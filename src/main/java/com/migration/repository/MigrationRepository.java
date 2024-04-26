package com.migration.repository;

import com.migration.entity.MigrationObject;
import com.migration.enums.MigrationObjectStatus;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface MigrationRepository extends R2dbcRepository<MigrationObject, Long> {
    @Query("select * from objects where config_id = :configId and id > :id order by id limit 100000")
    Flux<MigrationObject> findByConfigIdOrderByIdAsc(UUID configId, Long id);

    @Query("select * from objects where status = :status and config_id = :configId limit 100000")
    Flux<MigrationObject> findByStatusAndConfigIdWithLimit(MigrationObjectStatus status, UUID configId);

    @Transactional
    default Mono<Void> capture(List<Long> ids, MigrationObjectStatus status){
        return ids.size() == 0? Mono.empty():update(ids, status);
    }

    @Modifying
    @Query("UPDATE objects set status = :status where id IN (:ids)")
    Mono<Void> update(List<Long> ids, MigrationObjectStatus status);
}
