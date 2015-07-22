package com.jorgen.cmd;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Stateful decoder. Can only decode
 */
public class CommandDecoder extends ByteToMessageDecoder {
    private final static ByteBufProcessor FIND_TAB = new ByteBufProcessor() {
        public boolean process(byte value) throws Exception {
            return value != '\t';
        }
    };

    private enum STATE {

        INITIAL{
            @Override
            STATE decode(ChannelHandlerContext ctx, ByteBuf in, Command out) {
                return in.isReadable() ? CMD : this;
            }
        },
        CMD{
            @Override
            STATE decode(ChannelHandlerContext ctx, ByteBuf in, Command out) {
                String cmd = decodeString(in, FIND_TAB);

                if (cmd == null) {
                    return this;
                }

                out.setCmd(cmd);
                return KEY;
            }
        },
        KEY{
            @Override
            STATE decode(ChannelHandlerContext ctx, ByteBuf in, Command out) {
                String key = decodeString(in, FIND_TAB);

                if (key == null) {
                    return this;
                }

                out.setKey(key);
                return PAYLOAD_SIZE;
            }
        },
        PAYLOAD_SIZE{
            @Override
            STATE decode(ChannelHandlerContext ctx, ByteBuf in, Command out) {
                String payloadSize = decodeString(in, ByteBufProcessor.FIND_LF);

                if (payloadSize == null) {
                    return this;
                }

                final int size = Integer.parseInt(payloadSize);
                out.setPayloadSize(size);

                return size > 0 ? PAYLOAD : FINAL;
            }
        },
        PAYLOAD{
            @Override
            STATE decode(ChannelHandlerContext ctx, ByteBuf in, Command out) {
                Preconditions.checkState(out.getPayloadSize() > 0, "Payload size must be > 0 when trying to read the payload");

                // We must wait for the whole payload before starting to read
                // TODO: Or read slices for performance???
                if (in.readableBytes() < out.getPayloadSize()) {
                    return this;
                }

                byte[] payload = new byte[out.getPayloadSize()];
                in.readBytes(payload);
                out.setPayload(payload);

                return FINAL;
            }
        },
        FINAL{
            @Override
            STATE decode(ChannelHandlerContext ctx, ByteBuf in, Command out) {
                in.retain();
                return INITIAL;
            }
        };

        abstract STATE decode(ChannelHandlerContext ctx, ByteBuf in, Command out);


        private static String decodeString(ByteBuf in, ByteBufProcessor delim) {
            int index = in.forEachByte(delim);
            if (index <= 0) {
                return null;
            }
            int size = index - in.readerIndex(); // forEachByte() is relative buffer, not reader index
            byte[] cmd = new byte[size];

            in.readBytes(cmd); // copy from reader index. forEachByte() and readBytes() behaves a little different
            in.skipBytes(1);

            return new String(cmd, StandardCharsets.UTF_8);
        }
    }


    private STATE current = STATE.INITIAL;
    Command cmd;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//        System.err.println(in.toString(StandardCharsets.UTF_8));

        if (current == STATE.INITIAL) {
            cmd = new Command();
        }

        while (true) {
            STATE next = current.decode(ctx, in, cmd);
            if (next == current) {
                // No state transition => We haven't got all bytes for this field / state
                // Return and next decode call will contain more.
                return;
            }
            if (next == STATE.FINAL) {
                // We're done
                current  = STATE.INITIAL;
                break;
            }

            current = next;
        }

        out.add(cmd);
    }

}
