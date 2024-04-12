package com.migration.object;

import java.util.List;

public abstract class GenericObject {
    public abstract String create();
    public abstract List<GenericObject> getChildren();
    public abstract String getName();
}
