package com.jorgen.cmd;

import com.jorgen.store.KvStore;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

// TODO: Is this @Shareable? kvStore is the only state.
public class CommandHandler extends ChannelHandlerAdapter {

    private static final Log LOG = LogFactory.getLog(CommandHandler.class);
    private static final String OK = "ok";
    private final KvStore kvStore;

    public CommandHandler(KvStore kvStore) {
        this.kvStore = kvStore;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws ExecutionException, InterruptedException {

        Command cmd = (Command) msg;

        // TODO: A bit poor error handling. Don't catch 'em all, unrecoverable error should make server crash.
        // TODO: Not OCP. Impl abstract CommandFactory
        switch (cmd.getCmd().toLowerCase()) {
            case "set":
                kvStore.set(cmd.getKey(), cmd.getPayload());
                ctx.write(createResponse(OK));
                break;
            case "get":
                try {
                    final byte[] payload = kvStore.get(cmd.getKey());
                    ctx.write(createResponse(OK, payload));
                } catch (Exception e) {
                    ctx.write(createResponse(e.getMessage()));
                }
                break;
            case "delete":
                try {
                    kvStore.delete(cmd.getKey());
                    ctx.write(createResponse(OK));
                } catch (Exception e) {
                    ctx.write(createResponse(e.getMessage()));
                }
                break;
            case "stats":
                try {
                    final String stat;
                    final String key = cmd.getKey();

                    if (key.equals("num_connections")){
                        // TODO: Not Implemented. CommandHandler need ref to some (?) Netty bootstrap or Listener.
                        ctx.write(createResponse("Not implemented"));
                    } else {
                        stat = kvStore.stats(key);
                        ctx.write(createResponse(OK, stat.getBytes(StandardCharsets.UTF_8)));
                    }
                } catch (Exception e) {
                    ctx.write(createResponse(e.getMessage()));
                }
                break;
            default:
                ctx.write(createResponse("No such command: " + cmd.getCmd()));
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error(cause);

        ctx.writeAndFlush(createResponse(cause.getMessage()));

        ctx.close();
    }

    // TODO: Create Response Encoder
    // <status><TAB><payload size><NEWLINE><payload bytes>
    private ByteBuf createResponse(String msg, byte[] payload) {

        String resp;
        if (payload == null) {
            resp = msg + '\t' + "0" + '\n';
        } else {
            resp = msg + '\t' + Integer.toString(payload.length) + '\n' + new String(payload, StandardCharsets.UTF_8);
        }

        return Unpooled.copiedBuffer(resp.getBytes());
    }


    // <message><TAB>0<NEWLINE>
    private ByteBuf createResponse(String msg) {
        return createResponse(msg, null);
    }

}