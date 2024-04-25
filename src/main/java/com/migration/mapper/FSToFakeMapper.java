package com.migration.mapper;

import com.migration.object.GenericObject;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FSToFakeMapper extends MigrationMapper{
    default void map(@MappingTarget GenericObject target, GenericObject source){
    }
}
