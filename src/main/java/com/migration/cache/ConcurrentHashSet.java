package com.migration.cache;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashSet<K,V> extends ConcurrentHashMap<K,V> {
    private final int limit;
    private final V value;

    ConcurrentHashSet(V value, int limit){
        super();
        this.limit = limit;
        this.value = value;
    }

    public boolean addAllObjects(Collection<K> c) {
        int retain = c.size();
        for (K e : c) {
            if (size() < limit && put(e, value) == null)
                retain--;
        }
        return retain == 0;
    }
}
