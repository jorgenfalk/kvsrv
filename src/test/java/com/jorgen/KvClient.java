package com.jorgen;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 *
 */
public class KvClient {
    private final String host;
    private final int port;
    private EventLoopGroup group;
    private Bootstrap b;
    private Socket socket;

    public KvClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
    }

    public void close() throws IOException {
        socket.close();
    }

    private String send(final String cmd) throws IOException {
        // request = command + "\t" + key + "\t" + str(len(payload)) + "\n" + payload
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        dos.writeBytes(cmd);
        dos.flush();


        final String header = reader.readLine();
        final String[] split = header.split("\t");

        final String resp = split[0];
        final int size = Integer.parseInt(split[1]);

        if (size == 0){
            return resp;
        }

        char[] payload = new char[size];

        reader.read(payload);

        return new String(payload);
    }

    public String set(String key, String value) throws IOException {
        return send("set" + "\t" + key + "\t" + value.length() + "\n" + value);
    }
    public String get(String key) throws IOException {
        return send("get" + "\t" + key + "\t" + "0" + "\n");
    }
    public String delete(String key) throws IOException {
        return send("delete" + "\t" + key + "\t" + "0"+ "\n");
    }
}
