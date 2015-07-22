package com.jorgen;

import org.easetech.easytest.annotation.Parallel;
import org.easetech.easytest.annotation.Repeat;
import org.easetech.easytest.annotation.Report;
import org.easetech.easytest.runner.DataDrivenTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * IT for KvSrv
 */
@RunWith(DataDrivenTestRunner.class)
@Report
@Parallel(threads=50)
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
    @Repeat(times=200)
    public void fullScenario() throws Exception{
        final String key = UUID.randomUUID().toString();
        byte[] buf  = new byte[new Random().nextInt(10000) + 1];
        Arrays.fill(buf, (byte)'a');
        final String value = new String(buf);

        assertEquals("ok", client.set(key, value));
        assertEquals(value, client.get(key));
        assertEquals("ok", client.delete(key));
        assertEquals("key not found: " + key, client.get(key));
    }

    @Test
    @Repeat(times=10)
    public void bigPayload() throws Exception{
        final String key = UUID.randomUUID().toString();
        byte[] buf  = new byte[new Random().nextInt(1000000) + 1];
        Arrays.fill(buf, (byte)'b');
        final String value = new String(buf);

        assertEquals("ok", client.set(key, value));
        assertEquals(value, client.get(key));
        assertEquals("ok", client.delete(key));
        assertEquals("key not found: " + key, client.get(key));
    }
}
