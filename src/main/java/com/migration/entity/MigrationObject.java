package com.migration.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.migration.context.GenericContext;
import com.migration.object.GenericObject;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@Entity
@Table(name="objects")
@NoArgsConstructor
@AllArgsConstructor
public class MigrationObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String sourceId;
    private String sourcePath;
    private String targetId;
    private String targetPath;
    @Enumerated(EnumType.STRING)
    private MigrationObjectStatus status;
    private UUID configId;

    @JsonIgnore
    @Transient
    private GenericContext sourceContext;

    @JsonIgnore
    @Transient
    private GenericContext targetContext;

    @JsonIgnore
    public GenericObject getSourceObject(){
        return sourceContext.getObject(sourceId, sourcePath, type);
    }

    @JsonIgnore
    public GenericObject getTargetObject(){
        GenericObject targetObject = targetContext.getObject(targetId, targetPath, type);
        return targetObject;
    }
}
