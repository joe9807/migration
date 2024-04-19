package com.migration.object;

import java.util.List;

public abstract class GenericObject {
    public abstract GenericObject create();
    public abstract List<GenericObject> getChildren();
    public abstract String getId();
    public abstract String getPath();
    public abstract String getName();
    public abstract String getType();

    public String toString(){
        return getPath();
    }
}
