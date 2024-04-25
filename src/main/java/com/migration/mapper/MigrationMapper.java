package com.migration.mapper;

import com.migration.object.GenericObject;

public interface MigrationMapper {
    void map(GenericObject target, GenericObject source);
}
