package com.lulzotron.perf.java7netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Entrypoint for a Netty 4 implementation of the Sequence API (cf. <raml spec>
 * ).
 * </p>
 * 
 * <p>
 * Server listens on port 9004.
 * </p>
 * 
 * @author mdye
 *
 */
public class Serve {
  static final Logger LOG = LoggerFactory.getLogger(Serve.class);

  private final int port;

  Serve(final int port) {
    LOG.info("java7_netty server configured with port {}", port);
    this.port = port;
  }

  void listen() throws InterruptedException {
    // NioEventLoopGroup could take thread count as arg; Netty chooses count
    // given available processors and the defaults are adequate here. For
    // some (now older) tuning advice, cf.
    // http://yahooeng.tumblr.com/post/64758709722/making-storm-fly-with-netty

    // handles incoming connections
    EventLoopGroup connectionGroup = new NioEventLoopGroup();
    // processes requests
    EventLoopGroup processGroup = new NioEventLoopGroup();

    try {
      ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap.group(connectionGroup, processGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new SequenceServiceInitializer());

      ChannelFuture server = bootstrap.bind(port).sync();
      LOG.info("Starting Service");

      server.channel().closeFuture().sync();
    } finally {
      LOG.info("Shutting server down.");
      connectionGroup.shutdownGracefully();
      processGroup.shutdownGracefully();
    }
  }

  public static void main(String... args) {
    int port = 9009; // TODO: make configurable if desirable

    try {
      new Serve(port).listen();
    } catch (InterruptedException ex) {
      LOG.info("Service interrupted.");
    } finally {
      LOG.info("Exiting.");
      System.exit(0);
    }
  }
}