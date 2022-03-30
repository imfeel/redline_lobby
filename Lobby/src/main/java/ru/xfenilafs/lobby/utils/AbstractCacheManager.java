package ru.xfenilafs.lobby.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractCacheManager<T> {

    protected final Map<String, T> cacheMap = new HashMap<>();

    protected void cacheData(String dataName, T cache) {
        cacheMap.put(dataName.toLowerCase(), cache);
    }

    protected T getCache(String dataName) {
        return cacheMap.get(dataName.toLowerCase());
    }

    protected T getComputeCache(String dataName, Function<? super String, ? extends T> mappingFunction) {
        return cacheMap.computeIfAbsent(dataName.toLowerCase(), mappingFunction);
    }

    public interface Applicable<T> {

        void apply(T t);
    }
}