package com.migration.mapper;

import com.migration.enums.MigrationType;
import com.migration.object.GenericObject;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FSToFakeMapper extends MigrationMapper{
    default void map(@MappingTarget GenericObject target, GenericObject source){
    }

    default MigrationType getMapperKey(){
        return MigrationType.FSToFake;
    }
}
