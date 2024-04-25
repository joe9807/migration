package com.migration.object;

import com.migration.enums.MigrationObjectType;
import lombok.Data;

import java.util.List;

@Data
public abstract class GenericObject {
    protected MigrationObjectType type;

    public abstract GenericObject create();
    public abstract List<GenericObject> getChildren();
    public abstract String getId();
    public abstract String getPath();
    public abstract String getName();
    public MigrationObjectType getType(){
        return type;
    }

    public String toString(){
        return getPath();
    }
}
