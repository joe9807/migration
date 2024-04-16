package com.migration.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.migration.object.FileSystemObject;
import com.migration.object.GenericObject;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@Entity(name="objects")
@NoArgsConstructor
@AllArgsConstructor
public class MigrationObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sourceId;
    private String sourcePath;
    private String targetId;
    private String targetPath;
    @Enumerated(EnumType.STRING)
    private MigrationObjectStatus status;
    private UUID configId;

    @JsonIgnore
    public GenericObject getSourceObject(){
        return FileSystemObject.builder().path(sourcePath).build();
    }
}
