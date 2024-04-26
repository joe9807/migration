package com.migration.entity;

import com.migration.enums.MigrationObjectStatus;
import com.migration.enums.MigrationObjectType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@Entity
@Table(name="objects")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class MigrationObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MigrationObjectType type;

    private String sourceId;

    private String sourcePath;

    private String targetId;

    private String targetPath;

    @Enumerated(EnumType.STRING)
    private MigrationObjectStatus status;

    private UUID configId;
}
