package com.jorgen;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: No proper IT. Should initiate clean DB from Maven Failsafe (pre-it)
 *
 * This IT does not test odd edge cases. This is mainly done in the unit test suite. To be able to do fine grained tests
 * at this level, a full mock setup should be available, otherwise testing becomes very hard.
 */
public class KvSrvIT {

    private KvClient client;

    @Before
    public void setup() throws IOException {
        String host = "localhost";
        int port = 3434;

        client = new KvClient(host, port);

        client.connect();
    }


    @After
    public void teardown() throws IOException {
        client.close();
    }

    @Test
    public void fullScenario() throws Exception{
        String key = "foo";
        String value = "bar";

        // Init
        assertEquals("ok", client.set(key, "some_value")); // Make sure key is not in DB.
        assertEquals("ok", client.delete(key));
        int keys = Integer.parseInt(client.stats("num_keys")); // Get initial key count

        // Set & Get
        assertEquals("ok", client.set(key, "old_value")); //
        assertEquals("ok", client.set(key, value));       // Overwrite old_value
        assertEquals("Should have added only one element", keys + 1, Integer.parseInt(client.stats("num_keys")));

        // DB size
        // This is hard to test! Due to compactation, pre allocation etc in DB, the size is not guaranteed to increase
        // for such a small insert.
        assertTrue("DB should have some size", Long.parseLong(client.stats("db_size")) > 0);

        // Delete
        assertEquals(value, client.get(key));
        assertEquals("ok", client.delete(key));
        assertEquals("key not found: " + key, client.get(key));

        // Num keys
        assertEquals(keys, Integer.parseInt(client.stats("num_keys"))); // Back to the original amount of keys

        // Num connections
        // TODO: impl num_connections stats
        assertEquals("Not implemented", client.stats("num_connections"));
    }

    @Test
    @Ignore
    public void manualTestOfPersistentStore() throws Exception{
//        assertEquals("ok", client.set("foo", "bar"));
        assertEquals("bar", client.get("foo"));
    }

}
