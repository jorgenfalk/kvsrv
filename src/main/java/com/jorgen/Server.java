package com.jorgen;

import com.jorgen.cmd.CommandDecoder;
import com.jorgen.cmd.CommandHandler;
import com.jorgen.store.KvStore;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 *
 */
public class Server {
    private final KvStore kvStore;

    public Server(KvStore kvStore) {
        this.kvStore = kvStore;
    }

    void run(final int port, int threads) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(threads);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new CommandDecoder(),      // Hard coded => hard to test, but only part of server setup.
                                    new CommandHandler(kvStore)
                            );
                        }
                    });

            b.bind(port).sync().channel().closeFuture().await();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            kvStore.close();
        }
    }
}
