package com.jorgen.store;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import java.io.File;
import java.util.NoSuchElementException;

/**
 *
 */
public class KvStoreMapDB implements KvStore {

    private final DB db;
    private final HTreeMap<Object, Object> map;

    public KvStoreMapDB(String file) {
        db = DBMaker.newFileDB(new File(file))
                .closeOnJvmShutdown()
                .make();

        map = db.getHashMap("theStore");
    }

    @Override
    public void set(String key, byte[] payload) {
        map.put(key, payload);
//        db.commit();
    }

    @Override
    public void close() {
        db.close();
    }

    @Override
    public byte[] get(String key) {
        if (!map.containsKey(key)) {
            throw new NoSuchElementException("key not found: " + key);
        }

        return (byte[])map.get(key);
    }

    @Override
    public void delete(String key) {
        if (!map.containsKey(key)) {
            throw new NoSuchElementException("key not found: " + key);
        }

        map.remove(key);
//        db.commit();
    }


}
