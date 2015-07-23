package com.jorgen.cmd;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class CommandDecoderTest {

    // Set
    @Test
    public void setScenario() throws Exception {
        final CommandDecoder decoder = new CommandDecoder();
        final List<Object> out = new ArrayList<>();

        String cmdStr = "set\tfoo\t3\nbar";

        decoder.decode(null, Unpooled.wrappedBuffer(cmdStr.getBytes(StandardCharsets.UTF_8)), out);

        final Command cmd = (Command) out.get(0);
        assertEquals("set", cmd.getCmd());
        assertEquals("foo", cmd.getKey());
        assertEquals(3, cmd.getPayloadSize());
        assertEquals("bar", new String(cmd.getPayload(), StandardCharsets.UTF_8));
    }

    @Test
    public void extraPayloadShouldBeIgnored() throws Exception {
        final CommandDecoder decoder = new CommandDecoder();
        final List<Object> out = new ArrayList<>();

        String cmdStr = "set\tfoo\t3\nbarXXXXXXXX";

        decoder.decode(null, Unpooled.wrappedBuffer(cmdStr.getBytes(StandardCharsets.UTF_8)), out);

        final Command cmd = (Command) out.get(0);
        assertEquals("set", cmd.getCmd());
        assertEquals("foo", cmd.getKey());
        assertEquals(3, cmd.getPayloadSize());
        assertEquals("bar", new String(cmd.getPayload(), StandardCharsets.UTF_8));
    }

    @Test
    public void fragmentedDeliveryOfMessageInBuffer() throws Exception {
        final CommandDecoder decoder = new CommandDecoder();
        final List<Object> out = new ArrayList<>();

        String cmdStr = "set\tfoo\t9\nbarbar";
        String cmdStr2 = "bar";

        // Decode fragmented part
        final ByteBuf in = Unpooled.wrappedBuffer(cmdStr.getBytes(StandardCharsets.UTF_8));
        decoder.decode(null, in, out);

        // Create new buffer with the unread bytes ("barbar") and append with "bar"
        final ByteBuf in2 = Unpooled.copiedBuffer(in, Unpooled.wrappedBuffer(cmdStr2.getBytes(StandardCharsets.UTF_8)));
        decoder.decode(null, in2, out);

        final Command cmd = (Command) out.get(0);
        assertEquals("set", cmd.getCmd());
        assertEquals("foo", cmd.getKey());
        assertEquals(9, cmd.getPayloadSize());
        assertEquals("barbarbar", new String(cmd.getPayload(), StandardCharsets.UTF_8));
    }

    @Test (expected = IllegalArgumentException.class)
    public void setPayloadTooBigShouldThrowIAE() throws Exception {
        final CommandDecoder decoder = new CommandDecoder();
        final List<Object> out = new ArrayList<>();

        String cmdStr = "set\tfoo\t1000001\nsome_very_large_payload________";

        decoder.decode(null, Unpooled.wrappedBuffer(cmdStr.getBytes(StandardCharsets.UTF_8)), out);
    }


    // Get
    @Test
    public void getScenario() throws Exception {
        final CommandDecoder decoder = new CommandDecoder();
        final List<Object> out = new ArrayList<>();

        String cmdStr = "get\tfoo\t0\n";

        decoder.decode(null, Unpooled.wrappedBuffer(cmdStr.getBytes(StandardCharsets.UTF_8)), out);

        final Command cmd = (Command) out.get(0);
        assertEquals("get", cmd.getCmd());
        assertEquals("foo", cmd.getKey());
        assertEquals(0, cmd.getPayloadSize());
    }

    // Delete
    @Test
    public void deleteScenario() throws Exception {
        final CommandDecoder decoder = new CommandDecoder();
        final List<Object> out = new ArrayList<>();

        String cmdStr = "delete\tfoo\t0\n";

        decoder.decode(null, Unpooled.wrappedBuffer(cmdStr.getBytes(StandardCharsets.UTF_8)), out);

        final Command cmd = (Command) out.get(0);
        assertEquals("delete", cmd.getCmd());
        assertEquals("foo", cmd.getKey());
        assertEquals(0, cmd.getPayloadSize());
    }

    // Stats
    @Test
    public void statsScenario() throws Exception {
        final CommandDecoder decoder = new CommandDecoder();
        final List<Object> out = new ArrayList<>();

        String cmdStr = "stats\tnum_keys\t0\n";

        decoder.decode(null, Unpooled.wrappedBuffer(cmdStr.getBytes(StandardCharsets.UTF_8)), out);

        final Command cmd = (Command) out.get(0);
        assertEquals("stats", cmd.getCmd());
        assertEquals("num_keys", cmd.getKey());
        assertEquals(0, cmd.getPayloadSize());
    }
}
