package com.migration.mapper;

import com.migration.enums.MigrationType;
import com.migration.object.FakeObject;
import com.migration.object.GenericObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FakeToFakeMapper extends MigrationMapper {
    @Mapping(target = "path", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "context", ignore = true)
    void map(@MappingTarget FakeObject target, FakeObject source);

    default void map(@MappingTarget GenericObject target, GenericObject source){
         map((FakeObject) target, (FakeObject) source);
    }

    default MigrationType getMapperKey(){
        return MigrationType.FakeToFake;
    }
}
