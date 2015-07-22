package com.jorgen.cmd;

import com.google.common.base.Preconditions;

/**
 *
 */
public class Command {
//    private static final int MAX_CMD_SIZE = 6;
    private static final int MAX_KEY_SIZE = 100;
    private static final int MAX_PAYLOAD_SIZE = 1000000;

    private String cmd;
    private String key;
    private byte[] payload;
    private int payloadSize;

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public void setKey(String key) {
        Preconditions.checkArgument(key.length() <= MAX_KEY_SIZE, "Max key size is " + MAX_KEY_SIZE);
        this.key = key;
    }

    public void setPayload(byte[] payload) {
        Preconditions.checkArgument(payload.length <= MAX_PAYLOAD_SIZE, "Max payload size is " + MAX_PAYLOAD_SIZE);
        this.payload = payload;
    }

    public String getCmd() {
        return cmd;
    }

    public String getKey() {
        return key;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayloadSize(int payloadSize) {
        this.payloadSize = payloadSize;
    }

    public int getPayloadSize() {
        return payloadSize;
    }
}
