package com.jorgen.store;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkState;

/**
 *  KvStore Implementation based on MapDB.
 *
 *  TODO: Naive impl. Need to investigate tx, atomicity, compacting etc in MapDB. The naive impl is based on that operations like
 *        map.put() and map.remove() is thread safe and within its own tx. Since we have a small window between db
 *        operations, we need at least a simple Lock.
 *        Note: A better solution would be to replace the whole thing (KvServer and Store) with a real KV database like Cassandra
 *        But that's not within the rules of this assignment ...
 *  TODO: According to the manual, db.commit() shouldn't be necessary. But without commit(), it seems that MapDB will not persist to disk
 *        directly but wait until we do a graceful shutdown.
 *
 */
public class KvStoreMapDB implements KvStore {

    private final DB db;
    private final HTreeMap<Object, Object> map;
    private final Lock lock = new ReentrantLock();
    private final File file; // Needed to be able to return db size.

    public KvStoreMapDB(String dbName) {
        this.file = new File(dbName);
        db = DBMaker.newFileDB(file)
                .closeOnJvmShutdown()
                .make();

        map = db.getHashMap("theStore");
    }

    @Override
    public void close() {
        db.close();
    }

    @Override
    public void set(final String key, final byte[] payload) {
        tryLock(new LockExecute<Void>() {
            @Override
            public Void execute() {
                map.put(key, payload);
                db.commit();

                return null;
            }
        });
    }

    @Override
    public byte[] get(final String key) {
        return (byte[])tryLock(new LockExecute<Object>() {
            @Override
            public Object execute() {
                if (!map.containsKey(key)) {
                    throw new NoSuchElementException("key not found: " + key);
                }

                return map.get(key);
            }
        });
    }

    @Override
    public void delete(final String key) {
        tryLock(new LockExecute<Void>() {
            @Override
            public Void execute() {
                if (!map.containsKey(key)) {
                    throw new NoSuchElementException("key not found: " + key);
                }

                map.remove(key);
                db.commit();

                return null;
            }
        });
    }

    @Override
    public String stats(String key) {
        switch (key) {
            case "num_keys":
                return Integer.toString(map.size());

            case "db_size":
                return Long.toString(file.length());

            default:
                throw new IllegalArgumentException("No such stats metric: " + key);
        }
    }

    private <T> T tryLock(LockExecute<T> exec) {
        try {
            checkState(lock.tryLock(10, TimeUnit.SECONDS), "Could not acquire DB lock. Timeout while waiting");
            return exec.execute();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Could not acquire DB lock", e);
        } finally {
            lock.unlock();
        }
    }

    private interface LockExecute<T> {
        T execute();
    }

}
