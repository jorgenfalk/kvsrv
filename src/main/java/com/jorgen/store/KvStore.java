package com.jorgen.store;

/**
 *
 */
public interface KvStore {

    void set(String key, byte[] payload);
    byte[] get(String key);
    void delete(String key);
    String stats(String key);

    void close();
}
