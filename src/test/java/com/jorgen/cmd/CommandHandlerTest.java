package com.jorgen.cmd;

import com.jorgen.store.KvStore;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class CommandHandlerTest {
    @Test
    public void getScenario() throws ExecutionException, InterruptedException {
        final KvStore store = mock(KvStore.class);
        final CommandHandler handler = new CommandHandler(store);
        final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        final byte[] value = "bar".getBytes(StandardCharsets.UTF_8);
        final Command command = new Command();
        command.setCmd("get");
        command.setKey("foo");
        command.setPayloadSize(0);

        when(store.get("foo")).thenReturn(value);

        handler.channelRead(ctx, command);

        String resp = "ok" + '\t' + Integer.toString(value.length) + '\n' + new String(value, StandardCharsets.UTF_8);

        verify(ctx).write(Unpooled.copiedBuffer(resp.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void setScenario() throws ExecutionException, InterruptedException {
        final KvStore store = mock(KvStore.class);
        final CommandHandler handler = new CommandHandler(store);
        final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        final Command command = new Command();
        final byte[] value = "bar".getBytes(StandardCharsets.UTF_8);

        command.setCmd("set");
        command.setKey("foo");
        command.setPayloadSize(3);
        command.setPayload(value);

        handler.channelRead(ctx, command);

        String resp = "ok" + '\t' + 0 + '\n';

        verify(store).set("foo", value);
        verify(ctx).write(Unpooled.copiedBuffer(resp.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void deleteScenario() throws ExecutionException, InterruptedException {
        final KvStore store = mock(KvStore.class);
        final CommandHandler handler = new CommandHandler(store);
        final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        final Command command = new Command();

        command.setCmd("delete");
        command.setKey("foo");
        command.setPayloadSize(0);

        handler.channelRead(ctx, command);

        String resp = "ok" + '\t' + 0 + '\n';

        verify(store).delete("foo");
        verify(ctx).write(Unpooled.copiedBuffer(resp.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void statsScenario() throws ExecutionException, InterruptedException {
        final KvStore store = mock(KvStore.class);
        final CommandHandler handler = new CommandHandler(store);
        final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        final Command command = new Command();

        command.setCmd("stats");
        command.setKey("num_keys");
        command.setPayloadSize(0);

        when(store.stats("num_keys")).thenReturn("42");

        handler.channelRead(ctx, command);

        String resp = "ok" + '\t' + "2" + '\n' + "42";

        verify(ctx).write(Unpooled.copiedBuffer(resp.getBytes(StandardCharsets.UTF_8)));
    }
}
